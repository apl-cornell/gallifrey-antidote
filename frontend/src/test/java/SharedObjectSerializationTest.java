import org.junit.Test;
import static org.junit.Assert.assertEquals;

import java.io.*;
import java.util.Random;

import com.google.protobuf.ByteString;

public class SharedObjectSerializationTest {
    @Test
    public void test() {
        try {
            Random rd = new Random();
            byte[] random_bytes = new byte[10];
            rd.nextBytes(random_bytes);
            ByteString random_key = ByteString.copyFrom(random_bytes);
            SharedObject obj = new SharedObject(new Counter(0), random_key);
            FileOutputStream fout = new FileOutputStream("file.txt");
            ObjectOutputStream oos = new ObjectOutputStream(fout);
            oos.writeObject(obj);
            oos.close();
            fout.close();

            FileInputStream fin = new FileInputStream("file.txt");
            ObjectInputStream ois = new ObjectInputStream(fin);
            SharedObject obj2 = (SharedObject) ois.readObject();
            assertEquals(random_key, obj2.key.getKey());
            ois.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            assert (false);
        }
    }
}