package main;

import java.lang.reflect.Method;
import java.util.Hashtable;
import java.util.Map;

import java.nio.ByteBuffer;

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

    Map<Integer, Pair<String, Object>> ObjectTable = new Hashtable<>();

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
            myOtpNode = new OtpNode(NodeName);
            System.out.println(myOtpNode);
            myOtpNode.setCookie(cookie);
            myOtpMbox = myOtpNode.createMbox();
            myOtpMbox.registerName(MailBox);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private <T> T castObject(Class<T> clazz, Object object) {
        return (T) object;
    }

    public void run(String target) {
        while (true) {
            try {
                if (myOtpNode.ping(target, 2000)) {
                    System.out.println("remote is up");
                } else {
                    System.out.println("remote is not up");
                    //System.exit(1);
                }

                OtpErlangTuple tuple = (OtpErlangTuple) myOtpMbox.receive();

                System.out.println("Recieved message");

                last_pid = (OtpErlangPid) tuple.elementAt(0);
                System.out.println("processed pid");
                OtpErlangTuple payload = (OtpErlangTuple) tuple.elementAt(1);
                System.out.println("got payload");
                OtpErlangBinary ERLObjectId = (OtpErlangBinary) payload.elementAt(0);
                Integer ObjectId = ByteBuffer.wrap(ERLObjectId.binaryValue()).getInt();
                System.out.println("got object id");
                GenericFunction func = (GenericFunction) ((OtpErlangBinary)payload.elementAt(1)).getObject();
                System.out.println("got generic function");
                System.out.println(func.getObjectId());
                System.out.println(func.getFunctionName());
                System.out.println(func.getArgument());
                if (func.getFunctionName().equals("Counter")){
                    System.out.println("Initted counter");
                    // Object c = Class.forName("main." + func.getFunctionName());
                    Counter c = new Counter();
                    ObjectTable.put(ObjectId, new Pair(func.getFunctionName(), c));
                    myOtpMbox.send(last_pid, ERLObjectId);
                }
                else {
                    System.out.println("Did invoke call");
                    Pair <String, Object> p = ObjectTable.get(ObjectId);
                    String class_name = p.getFirst();
                    Object c = p.getSecond();
                    if (c instanceof Counter){
                        System.out.println("Doing function");
                        Counter counter = (Counter) c;
                        System.out.println(counter.value());
                        Method method = (counter.getClass()).getMethod(func.getFunctionName(), int.class);
                        method.invoke(c, func.getArgument());
                        System.out.println(counter.value());
                    }
                    else {
                        System.out.println("unknown class");
                        System.exit(1);
                    }
                    //Class<?> class_type = Class.forName("main." + p.getFirst());
                    //Method method = (class_type).getMethod(func.getFunctionName(), int.class);
                    //method.invoke(class_type.cast(c), func.getArgument());
                    myOtpMbox.send(last_pid, ERLObjectId);
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

    private OtpErlangTuple makeErlangMessage(int objid, String functionName, int argument){
        OtpErlangBinary id = new OtpErlangBinary(objid);

        GenericFunction func = new GenericFunction(objid, functionName, argument);
        OtpErlangBinary bin = new OtpErlangBinary(func);
        System.out.println(bin);

        OtpErlangObject[] emptypayload = new OtpErlangObject[2];
        emptypayload[0] = id;
        emptypayload[1] = bin;
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
        }
        else {
            System.out.println("Init failure");
            System.exit(1);
        }
    }

    private void test(String mailbox, String target) {
        while (true) {
            try {
                if (myOtpNode.ping("JavaNode", 2000)) {
                    System.out.println("remote is up");
                    OtpErlangTuple msg_init = makeErlangMessage(0, "Counter", 1);
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
                    OtpErlangTuple msg_invoke = makeErlangMessage(0, "increment", 2);
                    System.out.println("sending invoke message");
                    myOtpMbox.send(mailbox, target, msg_invoke);
                    System.out.println("Recieving message");
                    OtpErlangBinary ErlObjectId2 = (OtpErlangBinary) myOtpMbox.receive();
                    decodeErlangMessage(0, ErlObjectId2);
                    OtpErlangTuple msg_invoke2 = makeErlangMessage(0, "decrement", 1);
                    System.out.println("sending invoke message");
                    myOtpMbox.send(mailbox, target, msg_invoke2);
                    System.out.println("Recieving message");
                    OtpErlangBinary ErlObjectId3 = (OtpErlangBinary) myOtpMbox.receive();
                    decodeErlangMessage(0, ErlObjectId3);
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
    }

    public static void main(String[] args) {
        boolean run = true;
        if (run){
            Backend backend = new Backend("JavaNode", "javamailbox", "antidote");
            backend.run("antidote@127.0.0.1");
            // Testing
            //Backend backend = new Backend("JavaNode", "javamailbox");
            //backend.run("antidote");
        }
        else {
            Backend backend = new Backend("antidote", "erlmailbox");
            backend.test("javamailbox", "JavaNode@Abookwihnopages");
        }
    }

    public class Pair<A, B> {
        private A first;
        private B second;

        public Pair(A first, B second) {
            super();
            this.first = first;
            this.second = second;
        }

        public A getFirst() {
            return first;
        }

        public B getSecond() {
            return second;
        }
    }
}