package gallifrey.backend;

import org.junit.Test;

import eu.antidotedb.client.GenericKey;
import eu.antidotedb.client.Key;

import static org.junit.Assert.assertEquals;

import java.io.*;
import com.ericsson.otp.erlang.OtpErlangBinary;
import com.google.protobuf.ByteString;

public class BidirectionalMapSerializationTest {
    @Test
    public void test() {
        try {
            BidirectionalMap<GenericKey, OtpErlangBinary> obj = new BidirectionalMap<>();

            byte[] bytes = { (byte) 204, (byte) 29, (byte) 207, (byte) 217, (byte) 204, (byte) 29, (byte) 207,
                    (byte) 217, (byte) 204, (byte) 29 };

            ByteString byte_key = ByteString.copyFrom(bytes);
            GenericKey key = Key.generic(byte_key);
            String s = "Hello";
            OtpErlangBinary value = new OtpErlangBinary(s);
            if (key == null) {
                throw new RuntimeException();
            }
            obj.put(key, value);

            FileOutputStream fout = new FileOutputStream("file.txt");
            ObjectOutputStream oos = new ObjectOutputStream(fout);
            oos.writeObject(obj);
            oos.close();
            fout.close();

            FileInputStream fin = new FileInputStream("file.txt");
            ObjectInputStream ois = new ObjectInputStream(fin);
            @SuppressWarnings("unchecked")
            BidirectionalMap<GenericKey, OtpErlangBinary> obj2 = (BidirectionalMap<GenericKey, OtpErlangBinary>) ois
                    .readObject();

            assertEquals(obj.getValue(key), obj2.getValue(key));
            assertEquals(obj.getKey(value), obj2.getKey(value));

            ois.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            assert (false);
        }
    }
}
