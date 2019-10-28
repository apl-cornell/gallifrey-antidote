package main;

import java.lang.reflect.Method;
import java.util.Hashtable;
import java.util.Map;

import com.ericsson.otp.erlang.OtpErlangBinary;
import com.ericsson.otp.erlang.OtpErlangAtom;
import com.ericsson.otp.erlang.OtpErlangPid;
import com.ericsson.otp.erlang.OtpErlangString;
import com.ericsson.otp.erlang.OtpErlangTuple;
import com.ericsson.otp.erlang.OtpMbox;
import com.ericsson.otp.erlang.OtpNode;

public class Backend {
    OtpMbox myOtpMbox = null;
    OtpNode myOtpNode = null;
    OtpErlangPid last_pid;

    Map<Integer, Object> ObjectTable = new Hashtable<>();

    public Backend(String NodeName, String MailBox) {
        try {
            myOtpNode = new OtpNode(NodeName);
            System.out.println(myOtpNode);
            // myOtpNode.setCookie("secret");
            myOtpMbox = myOtpNode.createMbox();
            myOtpMbox.registerName(MailBox);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void run() {
        while (true) {
            try {
                if (myOtpNode.ping("ErlNode", 2000)) {
                    System.out.println("remote is up");
                } else {
                    System.out.println("remote is not up");
                    System.exit(1);
                }

                OtpErlangTuple tuple = (OtpErlangTuple) myOtpMbox.receive();
                last_pid = (OtpErlangPid) tuple.elementAt(0);
                OtpErlangTuple payload = (OtpErlangTuple) tuple.elementAt(1);

                OtpErlangBinary ERLObjectId = (OtpErlangBinary) payload.elementAt(0);
                Integer ObjectId = Integer.parseInt(ERLObjectId.toString());

                GenericFunction func = (GenericFunction) ((OtpErlangBinary)payload.elementAt(1)).getObject();
                if (func.getFunctionName() == "Counter"){
                    Object c = Class.forName(func.getFunctionName());
                    ObjectTable.put(ObjectId, c);

                }
                else {
                    Object c = ObjectTable.get(ObjectId);
                    Method method = c.getClass().getMethod(func.getFunctionName());
                    method.invoke(c, func.getArgument());
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

    public static void main(String[] args) {
        Backend backend = new Backend("JavaNode", "javamailbox");
        backend.run();

        /*
         * GenericFunction func = new GenericFunction(0, "hello", 1); OtpErlangBinary
         * bin = new OtpErlangBinary(func); System.out.println(bin); GenericFunction
         * func2 = (GenericFunction) (bin.getObject());
         * System.out.println(func2.getObjectId());
         */
    }
}