package main;

import java.util.Hashtable;
import java.util.Map;
import java.util.Random;

import com.ericsson.otp.erlang.OtpErlangBinary;
import com.ericsson.otp.erlang.OtpErlangAtom;
import com.ericsson.otp.erlang.OtpErlangPid;
import com.ericsson.otp.erlang.OtpErlangString;
import com.ericsson.otp.erlang.OtpErlangTuple;
import com.ericsson.otp.erlang.OtpErlangObject;
import com.ericsson.otp.erlang.OtpMbox;
import com.ericsson.otp.erlang.OtpNode;

public class Backend {
    OtpMbox myOtpMbox = null;
    OtpNode myOtpNode = null;
    OtpErlangPid last_pid;

    Map<OtpErlangBinary, CRDT> ObjectTable = new Hashtable<>();

    public Backend(String NodeName, String MailBox) {
        try {
            myOtpNode = new OtpNode(NodeName);
            System.out.println(myOtpNode);
            myOtpMbox = myOtpNode.createMbox();
            myOtpMbox.registerName(MailBox);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Backend(String NodeName, String MailBox, String cookie) {
        try {
            // KEEP THIS
            // If the next line raises an exception, you need to get empd running
            // Do something like `erl -sname whatever` to check and get it running if not
            // KEEP THIS
            myOtpNode = new OtpNode(NodeName);
            System.out.println(myOtpNode);
            myOtpNode.setCookie(cookie);
            myOtpMbox = myOtpNode.createMbox();
            myOtpMbox.registerName(MailBox);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void run(String target) {
        if (myOtpNode.ping(target, 2000)) {
            System.out.println("remote is up");
        } else {
            System.out.println("remote is not up");
            // System.exit(1);
        }
        while (true) {
            try {
                OtpErlangTuple tuple = (OtpErlangTuple) myOtpMbox.receive();
                System.out.println("Recieved message");

                last_pid = (OtpErlangPid) tuple.elementAt(0);
                System.out.println("process pid acquired");

                OtpErlangTuple payload = (OtpErlangTuple) tuple.elementAt(1);
                System.out.println("got payload");

                OtpErlangBinary ERLObjectId = (OtpErlangBinary) payload.elementAt(0);
                System.out.println(ERLObjectId);
                System.out.println("got object id");

                String status = ((OtpErlangAtom) payload.elementAt(1)).atomValue();
                System.out.println("got atom status for call");

                System.out.println(status);

                OtpErlangBinary binary = (OtpErlangBinary) payload.elementAt(2);

                if (!ObjectTable.containsKey(ERLObjectId)) {
                    System.out.println("new crdt object");
                    assert status.equals("invoke") : "trying to read an object that isn't made yet";
                    try {
                        ObjectTable.put(ERLObjectId, (CRDT) binary.getObject());
                    } catch (NullPointerException e) {
                        // See if we can get the object resent here
                        e.printStackTrace();
                    }
                    myOtpMbox.send(last_pid, ERLObjectId);
                } else {
                    CRDT crdt_object = ObjectTable.get(ERLObjectId);
                    switch (status) {
                    case "read":
                        System.out.println("Doing read call");
                        myOtpMbox.send(last_pid, new OtpErlangBinary(crdt_object.read()));
                        break;

                    case "invoke":
                        System.out.println("Doing invoke call");
                        try {
                            GenericFunction func = (GenericFunction) binary.getObject();
                            System.out.println("Doing invoke");
                            crdt_object.invoke(func);
                        } catch (ClassCastException e) {
                            System.out.println("Did this already");
                        }
                        myOtpMbox.send(last_pid, ERLObjectId);
                        break;

                    case "snapshot":
                        System.out.println("Doing antidote snapshot update");
                        crdt_object.snapshot();
                        byte[] b = new byte[20];
			new Random().nextBytes(b);
                        OtpErlangBinary new_key = new OtpErlangBinary(b);
                        ObjectTable.remove(ERLObjectId);
                        ObjectTable.put(new_key, crdt_object);
                        OtpErlangObject[] emptypayload = new OtpErlangObject[2];
                        emptypayload[0] = new_key;
                        emptypayload[1] = new OtpErlangBinary(crdt_object);
                        OtpErlangTuple new_snapshopt = new OtpErlangTuple(emptypayload);
                        myOtpMbox.send(last_pid, new_snapshopt);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                // return some failure
                OtpErlangString errormsg = new OtpErlangString("hi");
                // myOtpMbox.send(last_pid, errormsg);
                OtpErlangAtom atom = new OtpErlangAtom("error");
                myOtpMbox.send(last_pid, atom);
            }
        }
    }

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
        boolean run;
        try {
            run = Boolean.parseBoolean(args[0]);
        } catch (Exception e) {
            run = true;
        }
        try {
            boolean send_binary_test_message = Boolean.parseBoolean(args[1]);
            if (send_binary_test_message) {
                Backend backend = new Backend("JavaNode", "javamailbox");
                // OtpErlangBinary bin = new OtpErlangBinary(new Counter(0));
                // OtpErlangBinary bin = new OtpErlangBinary(new GenericFunction("decrement",
                // 1));
                Counter c = new Counter(0);
                c.increment(1);
                c.decrement(1);
                System.out.println(c.value());
                OtpErlangBinary bin = new OtpErlangBinary(c.value());
                backend.sendbin(bin);
                System.exit(0);
            }
        } catch (Exception e) {
        }
        if (run) {
            Backend backend = new Backend("JavaNode@127.0.0.1", "javamailbox", "antidote");
            backend.run("antidote@127.0.0.1");
        } else {
            Backend backend = new Backend("antidote@127.0.0.1", "erlmailbox", "antidote");
            backend.test("javamailbox", "JavaNode@127.0.0.1");
        }
    }
}
