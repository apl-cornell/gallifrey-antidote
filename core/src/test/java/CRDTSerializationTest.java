import org.junit.Test;
import static org.junit.Assert.assertEquals;

import java.io.*;

public class CRDTSerializationTest {
    @Test
    public void test() {
        try {
            Counter testCounter = new Counter(0);
            CRDT crdt = new CRDT(testCounter);
            FileOutputStream fout = new FileOutputStream("file.txt");
            ObjectOutputStream oos = new ObjectOutputStream(fout);
            oos.writeObject(crdt);
            oos.close();
            fout.close();

            FileInputStream fin = new FileInputStream("file.txt");
            ObjectInputStream ois = new ObjectInputStream(fin);
            CRDT crdt2 = (CRDT) ois.readObject();
            assertEquals(crdt.key, crdt2.key);
            assertEquals(((Counter) crdt.shared_object).value(), ((Counter) crdt2.shared_object).value());
            ois.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            assert (false);
        }
    }
}