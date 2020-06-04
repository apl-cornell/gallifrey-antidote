package gallifrey.backend;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

import java.io.*;

import com.ericsson.otp.erlang.OtpErlangAtom;
import com.ericsson.otp.erlang.OtpErlangLong;
import com.ericsson.otp.erlang.OtpErlangMap;
import com.ericsson.otp.erlang.OtpErlangObject;
import com.ericsson.otp.erlang.OtpErlangTuple;

import gallifrey.core.*;

public class CRDTEffectSerializationTest {
    @Test
    public void test() {
        try {
            Counter testCounter = new Counter(0);
            CRDT crdt = new CRDT(testCounter);
            OtpErlangObject[] keys = new OtpErlangObject[1];
            OtpErlangObject[] name = new OtpErlangAtom[1];
            name[0] = new OtpErlangAtom("antidote@127.0.0.1");
            keys[0] = new OtpErlangTuple(name);

            OtpErlangObject[] values = new OtpErlangObject[1];
            values[0] = new OtpErlangLong(1);
            OtpErlangMap map = new OtpErlangMap(keys, values);
            VectorClock time = new VectorClock(map);
            CRDTEffect obj = new CRDTEffect(crdt, time);
            FileOutputStream fout = new FileOutputStream("file.txt");
            ObjectOutputStream oos = new ObjectOutputStream(fout);
            oos.writeObject(obj);
            oos.close();
            fout.close();

            FileInputStream fin = new FileInputStream("file.txt");
            ObjectInputStream ois = new ObjectInputStream(fin);
            CRDTEffect obj2 = (CRDTEffect) ois.readObject();
            assertEquals(obj.crdt.key, obj2.crdt.key);
            assertEquals(((Counter) obj.crdt.shared_object).value(), ((Counter) obj2.crdt.shared_object).value());
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