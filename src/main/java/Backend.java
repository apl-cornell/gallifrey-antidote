import java.util.Hashtable;
import java.util.Map;
import java.util.Random;

import com.ericsson.otp.erlang.OtpErlangBinary;
import com.ericsson.otp.erlang.OtpErlangAtom;
import com.ericsson.otp.erlang.OtpErlangPid;
import com.ericsson.otp.erlang.OtpErlangTuple;
import com.ericsson.otp.erlang.OtpErlangObject;

public class Backend extends AntidoteBackend {

    Map<OtpErlangBinary, CRDT> ObjectTable = new Hashtable<>();

    public Backend(String NodeName, String MailBox, String cookie) {
        super(NodeName, MailBox, cookie);
    }

    public OtpErlangBinary value(OtpErlangBinary JavaObjectId) throws NoSuchObjectException {
        // assert that object is in table else request it
        CRDT crdt_object = ObjectTable.get(JavaObjectId);
        if (crdt_object == null) {
            throw new NoSuchObjectException();
        }
        return new OtpErlangBinary(crdt_object.read());
    }

    // Should this be split up into two, one for a crdt binary and one for a
    // GenericFunction binary?
    public OtpErlangBinary update(OtpErlangBinary JavaObjectId, OtpErlangBinary binary) throws NoSuchObjectException {
        if (!ObjectTable.containsKey(JavaObjectId)) {
            CRDT thing;
            try {
                thing = (CRDT) binary.getObject();
            } catch (ClassCastException e) {
                throw new NoSuchObjectException();
            }
            ObjectTable.put(JavaObjectId, thing);
        } else {
            try {
                // assert that object is in table else request it
                CRDT crdt_object = ObjectTable.get(JavaObjectId);
                GenericFunction func = (GenericFunction) binary.getObject();
                crdt_object.invoke(func);
            } catch (ClassCastException e) {
                // Check that we currently have a valid object in the table
                System.out.println("Did this already");
            }
        }

        return JavaObjectId;
    }

    // Should this be split up into two, one for a crdt binary and one for a
    // GenericFunction binary?
    public OtpErlangBinary downstream(OtpErlangBinary JavaObjectId, OtpErlangBinary binary) {
        // TBD what if anything we want to do here otherwise something like this
        return binary;
    }

    public OtpErlangTuple snapshot(OtpErlangBinary JavaObjectId) throws NoSuchObjectException {
        // assert that object is in table else request it
        CRDT crdt_object = ObjectTable.get(JavaObjectId);
        if (crdt_object == null) {
            throw new NoSuchObjectException();
        }

        OtpErlangBinary new_key = newJavaObjectId();
        // ObjectTable.remove(ERLObjectId);
        ObjectTable.put(new_key, crdt_object);
        OtpErlangObject[] emptypayload = new OtpErlangObject[2];
        emptypayload[0] = new_key;
        emptypayload[1] = new OtpErlangBinary(crdt_object);
        OtpErlangTuple new_snapshot = new OtpErlangTuple(emptypayload);
        return new_snapshot;
    }

    public OtpErlangBinary newJavaObjectId() {
        byte[] b = new byte[20];
        new Random().nextBytes(b);
        return new OtpErlangBinary(b);
    }

    // The methods below are used in testing

    private OtpErlangTuple makeErlangMessage(int objid, String status, String functionName, Object argument) {
        assert status.equals("invoke");

        OtpErlangBinary id = new OtpErlangBinary(objid);
        OtpErlangAtom atom = new OtpErlangAtom(status);
        GenericFunction func = new GenericFunction(functionName, argument);
        OtpErlangBinary bin = new OtpErlangBinary(func);
        System.out.println(bin);

        OtpErlangObject[] emptypayload = new OtpErlangObject[3];
        emptypayload[0] = id;
        emptypayload[1] = atom;
        emptypayload[2] = bin;
        OtpErlangTuple payload = new OtpErlangTuple(emptypayload);

        OtpErlangObject[] msg = new OtpErlangObject[2];
        msg[0] = myOtpMbox.self();
        msg[1] = payload;
        OtpErlangTuple tuple = new OtpErlangTuple(msg);
        return tuple;
    }

    private OtpErlangTuple makeErlangMessage(int objid, String status) {
        assert status.equals("read");

        OtpErlangBinary id = new OtpErlangBinary(objid);
        OtpErlangAtom atom = new OtpErlangAtom(status);

        OtpErlangObject[] emptypayload = new OtpErlangObject[2];
        emptypayload[0] = id;
        emptypayload[1] = atom;
        OtpErlangTuple payload = new OtpErlangTuple(emptypayload);

        OtpErlangObject[] msg = new OtpErlangObject[2];
        msg[0] = myOtpMbox.self();
        msg[1] = payload;
        OtpErlangTuple tuple = new OtpErlangTuple(msg);
        return tuple;
    }

    private OtpErlangTuple makeErlangMessage(int objid, String status, OtpErlangBinary bin) {
        assert status.equals("invoke");

        OtpErlangBinary id = new OtpErlangBinary(objid);
        OtpErlangAtom atom = new OtpErlangAtom(status);
        System.out.println(bin);

        OtpErlangObject[] emptypayload = new OtpErlangObject[3];
        emptypayload[0] = id;
        emptypayload[1] = atom;
        emptypayload[2] = bin;
        OtpErlangTuple payload = new OtpErlangTuple(emptypayload);

        OtpErlangObject[] msg = new OtpErlangObject[2];
        msg[0] = myOtpMbox.self();
        msg[1] = payload;
        OtpErlangTuple tuple = new OtpErlangTuple(msg);
        return tuple;
    }

    private void decodeErlangMessage(int objid, OtpErlangBinary ERLObjectId) {
        System.out.println(ERLObjectId.getObject());
        int ObjectId = (int) ERLObjectId.getObject();
        System.out.println(ObjectId);
        if (objid == ObjectId) {
            System.out.println("Init success");
        } else {
            System.out.println("Init failure");
            System.exit(1);
        }
    }

    private void decodeErlangMessage(OtpErlangBinary read_result) {
        System.out.println(read_result.getObject());
        int ObjectId = (int) read_result.getObject();
        System.out.println(ObjectId);
    }

    private void test(String mailbox, String target) {
        while (true) {
            try {
                if (myOtpNode.ping(target, 2000)) {
                    System.out.println("remote is up");
                    OtpErlangTuple msg_init = makeErlangMessage(0, "invoke", new OtpErlangBinary(new Counter(0)));
                    System.out.println("sending message");
                    myOtpMbox.send(mailbox, target, msg_init);

                    System.out.println("Recieving message");
                    OtpErlangBinary ErlObjectId = (OtpErlangBinary) myOtpMbox.receive();
                    decodeErlangMessage(0, ErlObjectId);
                    break;
                } else {
                    System.out.println("remote is not up");
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
        while (true) {
            try {
                OtpErlangTuple msg_invoke = makeErlangMessage(0, "invoke", "increment", 2);
                System.out.println("sending invoke message");
                myOtpMbox.send(mailbox, target, msg_invoke);
                System.out.println("Recieving message");
                OtpErlangBinary ErlObjectId2 = (OtpErlangBinary) myOtpMbox.receive();
                decodeErlangMessage(0, ErlObjectId2);

                OtpErlangTuple msg_invoke2 = makeErlangMessage(0, "invoke", "decrement", 1);
                System.out.println("sending invoke message");
                myOtpMbox.send(mailbox, target, msg_invoke2);
                System.out.println("Recieving message");
                OtpErlangBinary ErlObjectId3 = (OtpErlangBinary) myOtpMbox.receive();
                decodeErlangMessage(0, ErlObjectId3);

                OtpErlangTuple msg_invoke3 = makeErlangMessage(0, "read");
                System.out.println("sending invoke message");
                myOtpMbox.send(mailbox, target, msg_invoke3);
                System.out.println("Recieving message");
                OtpErlangBinary ErlObjectId4 = (OtpErlangBinary) myOtpMbox.receive();
                decodeErlangMessage(ErlObjectId4);
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
    }

    // Function to help get the erlang binary representation of the blob for testing
    private void sendbin(OtpErlangBinary bin) {
        try {
            OtpErlangTuple tuple = (OtpErlangTuple) myOtpMbox.receive();
            System.out.println("Recieved message");

            last_pid = (OtpErlangPid) tuple.elementAt(0);
            System.out.println("sending message");
            myOtpMbox.send(last_pid, bin);
        } catch (Exception e) {
            e.printStackTrace();
            OtpErlangAtom atom = new OtpErlangAtom("error");
            myOtpMbox.send(last_pid, atom);
        }
    }

    public static void main(String[] args) {
        // For when I need to get the full binary of a java object for testing
        try {
            boolean send_binary_test_message = Boolean.parseBoolean(args[2]);
            if (send_binary_test_message) {
                Backend backend = new Backend("JavaNode", "javamailbox", "antidote");
                OtpErlangBinary bin = new OtpErlangBinary(new Counter(0));
                // OtpErlangBinary bin = new OtpErlangBinary(new GenericFunction("increment",
                // 1));

                /*
                 * Counter c = new Counter(0); c.increment(1); c.decrement(1);
                 * System.out.println(c.value()); OtpErlangBinary bin = new
                 * OtpErlangBinary(c.value());
                 */
                backend.sendbin(bin);
                System.exit(0);
            }
        } catch (Exception e) {
        }

        String nodename;
        if (args.length >= 1) {
            nodename = args[0];
        } else {
            nodename = "JavaNode@127.0.0.1";
        }
        String target;
        if (args.length >= 2) {
            target = args[1];
        } else {
            target = "antidote@127.0.0.1";
        }
        Backend backend = new Backend(nodename, "javamailbox", "antidote");
        backend.run();

        // For testing by pretending this backend is antidote
        // Backend backend = new Backend("antidote@127.0.0.1", "erlmailbox",
        // "antidote");
        // backend.test("javamailbox", "JavaNode@127.0.0.1");
    }
}
