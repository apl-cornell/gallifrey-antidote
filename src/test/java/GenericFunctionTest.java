import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.*;
import java.util.List;
import java.util.ArrayList;

public class GenericFunctionTest {
    @Test
    public void test() {
        try {
            GenericFunction func = new GenericFunction("dothings", 1);
            FileOutputStream fout = new FileOutputStream("file.txt");
            ObjectOutputStream oos = new ObjectOutputStream(fout);
            oos.writeObject(func);
            oos.close();
            fout.close();


            FileInputStream fin = new FileInputStream("file.txt");
            ObjectInputStream ois = new ObjectInputStream(fin);
            GenericFunction func2 = (GenericFunction) ois.readObject();
            assertEquals("dothings", func.getFunctionName());
            List<Object> argList = new ArrayList<Object>(1);
            argList.add(1);
            assertEquals(argList, func2.getArguments());
            ois.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}