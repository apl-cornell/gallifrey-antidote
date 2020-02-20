package main;

import com.ericsson.otp.erlang.OtpErlangBinary;
import com.ericsson.otp.erlang.OtpErlangAtom;
import com.ericsson.otp.erlang.OtpErlangPid;
import com.ericsson.otp.erlang.OtpErlangTuple;
import com.ericsson.otp.erlang.OtpMbox;
import com.ericsson.otp.erlang.OtpNode;

abstract class AntidoteBackend {
    OtpMbox myOtpMbox = null;
    OtpNode myOtpNode = null;
    OtpErlangPid last_pid;

    public AntidoteBackend() {
        try {
            myOtpNode = new OtpNode("JavaNode");
            myOtpNode.setCookie("antidote");
            myOtpMbox = myOtpNode.createMbox();
            myOtpMbox.registerName("javamailbox");
        } catch (

        Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public AntidoteBackend(String NodeName, String MailBox, String cookie) {
        try {
            // KEEP THIS
            // If the next line raises an exception, you need to get empd running
            // Do something like `erl -sname whatever` to check and get it running if not
            // KEEP THIS
            myOtpNode = new OtpNode(NodeName);
            myOtpNode.setCookie(cookie);
            myOtpMbox = myOtpNode.createMbox();
            myOtpMbox.registerName(MailBox);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public abstract OtpErlangBinary value(OtpErlangBinary JavaObjectId);

    // Should this be split up into two, one for a crdt binary and one for a
    // GenericFunction binary?
    public abstract OtpErlangBinary update(OtpErlangBinary JavaObjectId, OtpErlangBinary binary);

    // Should this be split up into two, one for a crdt binary and one for a
    // GenericFunction binary?
    // public abstract OtpErlangBinary downstream(OtpErlangBinary JavaObjectId, OtpErlangBinary binary);

    public abstract OtpErlangBinary snapshot(OtpErlangBinary JavaObjectId);

    // Antidote has given us a JavaId for an object we don't have so we need to
    // request it from antidote.
    // Should this be a special `status` that antidote can send to us or just stick
    // with antidote calliing invoke with JavaObject
    public void requestUpdateInvocationWithObject() {
        OtpErlangAtom atom = new OtpErlangAtom("getobject");
        myOtpMbox.send(last_pid, atom);
    }

    // TBD if this adds value/ is something that can be used
    // public abstract void new();
    // Get JavaId

    public void run() {
        while (true) {
            try {
                OtpErlangTuple tuple = (OtpErlangTuple) myOtpMbox.receive();

                last_pid = (OtpErlangPid) tuple.elementAt(0);

                OtpErlangTuple payload = (OtpErlangTuple) tuple.elementAt(1);

                // Antidote sends the JavaId, operation, and binary args
                OtpErlangBinary JavaObjectId = (OtpErlangBinary) payload.elementAt(0);
                String status = ((OtpErlangAtom) payload.elementAt(1)).atomValue();
                OtpErlangBinary binary = (OtpErlangBinary) payload.elementAt(2);

                switch (status) {
                    case "read":
                        myOtpMbox.send(last_pid, value(JavaObjectId));
                        break;

                    // maybe change invoke to update?
                    case "invoke":
                        myOtpMbox.send(last_pid, update(JavaObjectId, binary));
                        break;

                    case "snapshot":
                        myOtpMbox.send(last_pid, snapshot(JavaObjectId));

                        // Not currently sent while this is thought out
                    //case "downstream":
                    //    myOtpMbox.send(last_pid, downstream(JavaObjectId, binary));
                }
            } catch (Exception e) {
                e.printStackTrace();
                // return some failure
                OtpErlangAtom atom = new OtpErlangAtom("error");
                myOtpMbox.send(last_pid, atom);
            }
        }
    }

    // A helper more than anything necessary
    public Boolean check(String target) {
        return myOtpNode.ping(target, 2000);
    }
}