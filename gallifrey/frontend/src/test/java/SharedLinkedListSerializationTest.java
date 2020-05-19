package gallifrey.frontend;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

import java.io.*;

public class SharedLinkedListSerializationTest {
    @Test
    public void test() {
        try {
            SharedLinkedList<String> obj = new SharedLinkedList<String>("bye");

            SharedLinkedList<String> temp = new SharedLinkedList<String>("hello");
            SharedObject s = new SharedObject(temp);
            obj.setNext(s);
            FileOutputStream fout = new FileOutputStream("file.txt");
            ObjectOutputStream oos = new ObjectOutputStream(fout);
            oos.writeObject(obj);
            oos.close();
            fout.close();

            FileInputStream fin = new FileInputStream("file.txt");
            ObjectInputStream ois = new ObjectInputStream(fin);
            SharedLinkedList<String> obj2 = (SharedLinkedList<String>) ois.readObject();
            assertEquals(obj.getData(), "bye");
            assertEquals(obj.getData(), obj2.getData());
            assertEquals(s.key, obj2.getNext().key);
            assertEquals(obj.getNext().key, obj2.getNext().key);
            ois.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            assert (false);
        }
    }
}