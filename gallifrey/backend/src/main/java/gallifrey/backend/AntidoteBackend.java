package gallifrey.backend;

import java.io.IOException;

import com.ericsson.otp.erlang.OtpErlangBinary;
import com.ericsson.otp.erlang.OtpErlangDecodeException;
import com.ericsson.otp.erlang.OtpErlangExit;
import com.ericsson.otp.erlang.OtpErlangAtom;
import com.ericsson.otp.erlang.OtpErlangMap;
import com.ericsson.otp.erlang.OtpErlangPid;
import com.ericsson.otp.erlang.OtpErlangTuple;
import com.ericsson.otp.erlang.OtpErlangObject;
import com.ericsson.otp.erlang.OtpErlangLong;
import com.ericsson.otp.erlang.OtpErlangRangeException;
import com.ericsson.otp.erlang.OtpMbox;
import com.ericsson.otp.erlang.OtpNode;

import eu.antidotedb.client.GenericKey;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.Vector;

import gallifrey.core.*;

abstract class AntidoteBackend extends UnicastRemoteObject implements Runnable, RMIInterface {
    private static final long serialVersionUID = 15L;
    final OtpMbox myOtpMbox;
    final OtpNode myOtpNode;
    OtpErlangPid last_pid;

    public enum Status {
        read, update, downstream, snapshot, newjavaid, loadsnapshot
    }

    public OtpErlangTuple createBackendMessage(OtpErlangBinary bin) {
        OtpErlangObject[] backendMessage = new OtpErlangObject[2];
        backendMessage[0] = new OtpErlangAtom("javaBackendMessage");
        backendMessage[1] = bin;
        return new OtpErlangTuple(backendMessage);
    }

    public OtpErlangTuple createBackendMessage(OtpErlangAtom atom) {
        OtpErlangObject[] backendMessage = new OtpErlangObject[2];
        backendMessage[0] = new OtpErlangAtom("javaBackendMessage");
        backendMessage[1] = atom;
        return new OtpErlangTuple(backendMessage);
    }

    public OtpErlangTuple createBackendMessage(OtpErlangTuple tuple) {
        OtpErlangObject[] backendMessage = new OtpErlangObject[2];
        backendMessage[0] = new OtpErlangAtom("javaBackendMessage");
        backendMessage[1] = tuple;
        return new OtpErlangTuple(backendMessage);
    }

    public AntidoteBackend() throws RemoteException {
        try {
            myOtpNode = new OtpNode("JavaNode");
            myOtpNode.setCookie("antidote");
            myOtpMbox = myOtpNode.createMbox();
            myOtpMbox.registerName("javamailbox");
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public AntidoteBackend(String NodeName, String MailBox, String cookie) throws RemoteException {
        try {
            // KEEP THIS
            // If the next line raises an exception, you need to get empd running
            // Do something like `erl -sname whatever` to check and get it running if not
            // KEEP THIS
            myOtpNode = new OtpNode(NodeName);
            myOtpNode.setCookie(cookie);
            myOtpMbox = myOtpNode.createMbox();
            myOtpMbox.registerName(MailBox);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public abstract OtpErlangBinary value(OtpErlangBinary JavaObjectId) throws NoSuchObjectException;

    public abstract OtpErlangBinary update(OtpErlangBinary JavaObjectId, OtpErlangBinary binary)
            throws NoSuchObjectException;

    // If we want to do any analysis on the operation based on current state before
    // it becomes a valid operation.
    public abstract OtpErlangBinary downstream(OtpErlangBinary JavaObjectId, OtpErlangBinary binary, OtpErlangMap time,
            OtpErlangMap global_time) throws NoSuchObjectException;

    public abstract OtpErlangTuple snapshot(OtpErlangBinary JavaObjectId) throws NoSuchObjectException;

    // For when erlang wants to instantiate a new erlang object for a to be created
    // java object and needs a corresponding id.
    // Must be 20 bytes or things will go poorly
    public abstract OtpErlangBinary newJavaObjectId();

    // When we request an snapshot via NoSuchObjectException we need a function to
    // handle the incoming snapshot appropriately because an crdt initialization may
    // not be equal to an crdt snapshot
    public abstract OtpErlangBinary loadSnapshot(OtpErlangBinary JavaObjectId, OtpErlangBinary binary);

    public abstract Snapshot rmiOperation(GenericKey k, List<VectorClock> frontier, OtpErlangBinary objectid)
            throws RemoteException, BackendRequiresFlushException;

    public abstract Snapshot rmiOperation(GenericKey k, List<VectorClock> frontier, OtpErlangBinary objectid,
            VectorClock v) throws RemoteException;

    public void run() {
        System.out.println(myOtpNode);
        Status[] status_enum_map = Status.values();
        while (true) {
            try {
                OtpErlangTuple tuple = (OtpErlangTuple) myOtpMbox.receive();

                last_pid = (OtpErlangPid) tuple.elementAt(0);

                OtpErlangTuple payload = (OtpErlangTuple) tuple.elementAt(1);

                // Antidote sends the JavaId, operation, and binary args
                OtpErlangBinary JavaObjectId = (OtpErlangBinary) payload.elementAt(0);
                int status_enum = ((OtpErlangLong) payload.elementAt(1)).intValue();
                // Could be null depending on message type(see status_enum)
                OtpErlangBinary binary = (OtpErlangBinary) payload.elementAt(2);

                switch (status_enum_map[status_enum]) {
                    case read:
                        myOtpMbox.send(last_pid, createBackendMessage(value(JavaObjectId)));
                        break;

                    case update:
                        myOtpMbox.send(last_pid, createBackendMessage(update(JavaObjectId, binary)));
                        break;

                    case snapshot:
                        myOtpMbox.send(last_pid, createBackendMessage(snapshot(JavaObjectId)));
                        break;

                    case downstream:
                        OtpErlangMap transaction_clock = (OtpErlangMap) payload.elementAt(3);
                        OtpErlangMap global_clock = (OtpErlangMap) payload.elementAt(4);
                        myOtpMbox.send(last_pid, createBackendMessage(
                                downstream(JavaObjectId, binary, transaction_clock, global_clock)));
                        break;

                    case newjavaid:
                        myOtpMbox.send(last_pid, createBackendMessage(newJavaObjectId()));
                        break;

                    case loadsnapshot:
                        myOtpMbox.send(last_pid, createBackendMessage(loadSnapshot(JavaObjectId, binary)));
                        break;
                }
            } catch (NoSuchObjectException e) {
                OtpErlangAtom atom = new OtpErlangAtom("getobject");
                myOtpMbox.send(last_pid, createBackendMessage(atom));
            } catch (RuntimeException | OtpErlangDecodeException e) {
                // Some error occured in the underlying method we tried to invoke
                // The message we recieved was so malformed it's not readable as an erlang
                // message
                e.printStackTrace();
                OtpErlangAtom atom = new OtpErlangAtom("error");
                myOtpMbox.send(last_pid, createBackendMessage(atom));
                // Maybe we should quit here
            } catch (OtpErlangRangeException e) {
                // Invalid enum status so some macro got messed up on the antidote_crdt side
                OtpErlangAtom atom = new OtpErlangAtom("error");
                myOtpMbox.send(last_pid, createBackendMessage(atom));
                e.printStackTrace();
                System.exit(42);
            } catch (OtpErlangExit e) {
                // The antidote side has gone down (process died) and then we recieved a
                // message. There is nothing to respond to
                e.printStackTrace();
                System.exit(42);
            }
        }
    }

    // A helper more than anything necessary
    public Boolean check(String target) {
        return myOtpNode.ping(target, 2000);
    }
}