package gallifrey.backend;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

import java.io.*;
import java.util.ArrayList;
import java.util.TreeSet;

import com.ericsson.otp.erlang.OtpErlangAtom;
import com.ericsson.otp.erlang.OtpErlangLong;
import com.ericsson.otp.erlang.OtpErlangMap;
import com.ericsson.otp.erlang.OtpErlangObject;
import com.ericsson.otp.erlang.OtpErlangTuple;

import gallifrey.core.*;

public class SnapshotSerializationTest {
    @Test
    public void test() {
        try {
            Counter testCounter = new Counter(0);
            CRDT crdt = new CRDT(testCounter);

            GenericFunction func = new GenericFunction("dothings", 1);

            OtpErlangObject[] keys = new OtpErlangObject[1];
            OtpErlangObject[] name = new OtpErlangAtom[1];
            name[0] = new OtpErlangAtom("antidote@127.0.0.1");
            keys[0] = new OtpErlangTuple(name);
            OtpErlangObject[] values = new OtpErlangObject[1];
            values[0] = new OtpErlangLong(1);

            OtpErlangMap map = new OtpErlangMap(keys, values);
            VectorClock time = new VectorClock(map);
            GenericEffect eff = new GenericEffect(func, time);
            TreeSet<GenericEffect> sortedEffectSet = new TreeSet<GenericEffect>();
            sortedEffectSet.add(eff);
            Snapshot obj = new Snapshot(crdt, sortedEffectSet);
            FileOutputStream fout = new FileOutputStream("file.txt");
            ObjectOutputStream oos = new ObjectOutputStream(fout);
            oos.writeObject(obj);
            oos.close();
            fout.close();

            FileInputStream fin = new FileInputStream("file.txt");
            ObjectInputStream ois = new ObjectInputStream(fin);
            Snapshot obj2 = (Snapshot) ois.readObject();

            assertEquals(obj.crdt.key, obj2.crdt.key);
            assertEquals(((Counter) obj.crdt.shared_object).value(), ((Counter) obj2.crdt.shared_object).value());

            assertEquals(obj.effectbuffer.size(), obj2.effectbuffer.size());

            ArrayList<GenericEffect> effectSet1 = new ArrayList<GenericEffect>(obj.effectbuffer);
            ArrayList<GenericEffect> effectSet2 = new ArrayList<GenericEffect>(obj2.effectbuffer);

            for (int i = 0; i < obj.effectbuffer.size(); i = i + 1){
                GenericEffect effect1 = effectSet1.get(i);
                GenericEffect effect2 = effectSet2.get(i);

                assertEquals(effect1.func.getFunctionName(), effect2.func.getFunctionName());
                assertEquals(effect1.func.getArguments(), effect2.func.getArguments());
                VectorClock v1 = effect1.time;
                VectorClock v2 = effect2.time;
                assertEquals(v1.compareTo(v2), 0);
            }

            ois.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            assert (false);
        }
    }
}