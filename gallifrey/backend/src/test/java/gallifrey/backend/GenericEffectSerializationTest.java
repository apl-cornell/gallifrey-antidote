package gallifrey.backend;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import com.ericsson.otp.erlang.OtpErlangAtom;
import com.ericsson.otp.erlang.OtpErlangLong;
import com.ericsson.otp.erlang.OtpErlangMap;
import com.ericsson.otp.erlang.OtpErlangObject;
import com.ericsson.otp.erlang.OtpErlangTuple;

import gallifrey.core.*;

public class GenericEffectSerializationTest {
    @Test
    public void test() {
        try {
            GenericFunction func = new GenericFunction("dothings", null, 1);
            OtpErlangObject[] keys = new OtpErlangObject[1];
            OtpErlangObject[] name = new OtpErlangAtom[1];
            name[0] = new OtpErlangAtom("antidote@127.0.0.1");
            keys[0] = new OtpErlangTuple(name);

            OtpErlangObject[] values = new OtpErlangObject[1];
            values[0] = new OtpErlangLong(1);
            OtpErlangMap map = new OtpErlangMap(keys, values);
            VectorClock time = new VectorClock(map);
            GenericEffect obj = new GenericEffect(func, time);
            FileOutputStream fout = new FileOutputStream("file.txt");
            ObjectOutputStream oos = new ObjectOutputStream(fout);
            oos.writeObject(obj);
            oos.close();
            fout.close();

            FileInputStream fin = new FileInputStream("file.txt");
            ObjectInputStream ois = new ObjectInputStream(fin);
            GenericEffect obj2 = (GenericEffect) ois.readObject();
            assertEquals("dothings", obj2.func.getFunctionName());
            List<Object> argList = new ArrayList<Object>(1);
            argList.add(1);
            assertEquals(argList, obj2.func.getArguments());
            VectorClock v1 = obj.time;
            VectorClock v2 = obj2.time;
            assertEquals(v1.compareTo(v2), 0);

            ois.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            assert (false);
        }
    }
}
