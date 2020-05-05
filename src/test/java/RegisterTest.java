import org.junit.Test;
import static org.junit.Assert.assertEquals;

/* import java.util.HashSet; */

public class RegisterTest {
    @Test
    public void test() {
        Register<String> testRegister1 = new Register<String>("hello");
        assertEquals("hello", (String) testRegister1.value());
        GenericFunction func1 = new GenericFunction("assign", "goodbye");
        testRegister1.invoke(func1);
        assertEquals("goodbye", (String) testRegister1.value());


        // Issue that needs to be addressed at the compiler level
/*         HashSet<Integer> testSet = new HashSet<Integer>();
        testSet.add(5);
        Register<HashSet<Integer>> testRegister2 = new Register<HashSet<Integer>>(testSet);
        testRegister2.invoke(func1);
        assertEquals(testSet, (HashSet<Integer>) testRegister2.value());
        testSet.add(42);
        GenericFunction func2 = new GenericFunction("assign", testSet);
        testRegister2.invoke(func2);
        assertEquals(testSet, (HashSet<Integer>) testRegister2.value()); */
    }
}