package gallifrey.core;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

import java.util.HashSet;

/* import java.util.HashSet; */

public class RegisterTest {
    @Test
    public void test() {
        Register<String> testRegister1 = new Register<String>("hello");
        CRDT crdt = new CRDT(testRegister1);
        GenericFunction readfunc = new GenericFunction("value",null);
        assertEquals("hello", (String) crdt.invoke(readfunc));
        GenericFunction func1 = new GenericFunction("assign", null, "goodbye");
        crdt.invoke(func1);
        assertEquals("goodbye", (String) crdt.invoke(readfunc));

        // Issue that needs to be addressed at the compiler level
        HashSet<Integer> testSet = new HashSet<Integer>();
        testSet.add(5);

        Register<HashSet<Integer>> testRegister2 = new Register<HashSet<Integer>>(testSet);
        CRDT crdt2 = new CRDT(testRegister2);
        assertEquals(testSet, (HashSet<Integer>) crdt2.invoke(readfunc));
        testSet.add(42);
        GenericFunction func2 = new GenericFunction("assign", null, testSet);
        crdt2.invoke(func2);
        assertEquals(testSet, (HashSet<Integer>) crdt2.invoke(readfunc));
    }
}
