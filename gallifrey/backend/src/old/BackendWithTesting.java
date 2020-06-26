import com.ericsson.otp.erlang.OtpErlangBinary;
import com.ericsson.otp.erlang.OtpErlangLong;
import com.ericsson.otp.erlang.OtpErlangAtom;
import com.ericsson.otp.erlang.OtpErlangPid;
import com.ericsson.otp.erlang.OtpErlangTuple;
import com.ericsson.otp.erlang.OtpErlangObject;

public class BackendWithTesting extends Backend {
    public BackendWithTesting(String NodeName, String MailBox, String cookie) {
        super(NodeName, MailBox, cookie);
    }

    public enum Status {
        read, update, downstream, snapshot, newjavaid
    }

    private OtpErlangTuple makeErlangMessage(int objid, Status status, String functionName, Object argument) {
        assert status.equals(Status.update);

        OtpErlangBinary id = new OtpErlangBinary(objid);
        OtpErlangLong atom = new OtpErlangLong(status.ordinal());
        GenericFunction func = new GenericFunction(functionName, null, argument);
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

    private OtpErlangTuple makeErlangMessage(int objid, Status status) {
        assert status.equals(Status.read);

        OtpErlangBinary id = new OtpErlangBinary(objid);
        OtpErlangLong atom = new OtpErlangLong(status.ordinal());

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

    private OtpErlangTuple makeErlangMessage(int objid, Status status, OtpErlangBinary bin) {
        assert status.equals(Status.update);

        OtpErlangBinary id = new OtpErlangBinary(objid);
        OtpErlangLong atom = new OtpErlangLong(status.ordinal());
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
                    OtpErlangTuple msg_init = makeErlangMessage(0, Status.update, new OtpErlangBinary(new Counter(0)));
                    System.out.println("sending message");
                    myOtpMbox.send(mailbox, target, msg_init);

                    System.out.println("Receiving message");
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
                OtpErlangTuple msg_invoke = makeErlangMessage(0, Status.update, "increment", 2);
                System.out.println("sending invoke message");
                myOtpMbox.send(mailbox, target, msg_invoke);
                System.out.println("Receiving message");
                OtpErlangBinary ErlObjectId2 = (OtpErlangBinary) myOtpMbox.receive();
                decodeErlangMessage(0, ErlObjectId2);

                OtpErlangTuple msg_invoke2 = makeErlangMessage(0, Status.update, "decrement", 1);
                System.out.println("sending invoke message");
                myOtpMbox.send(mailbox, target, msg_invoke2);
                System.out.println("Receiving message");
                OtpErlangBinary ErlObjectId3 = (OtpErlangBinary) myOtpMbox.receive();
                decodeErlangMessage(0, ErlObjectId3);

                OtpErlangTuple msg_invoke3 = makeErlangMessage(0, Status.read);
                System.out.println("sending invoke message");
                myOtpMbox.send(mailbox, target, msg_invoke3);
                System.out.println("Receiving message");
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
            System.out.println("Received message");

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
            boolean send_binary_test_message = Boolean.parseBoolean(args[0]);
            if (send_binary_test_message) {
                BackendWithTesting backend = new BackendWithTesting("JavaNode", "javamailbox", "antidote");
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

        // For testing by pretending this backend is antidote
        BackendWithTesting backend = new BackendWithTesting("antidote@127.0.0.1", "erlmailbox", "antidote");
        backend.test("javamailbox", "JavaNode@127.0.0.1");
    }
}
