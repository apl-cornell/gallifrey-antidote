import org.junit.Test;
import static org.junit.Assert.assertEquals;


public class PositiveCounterTest {
    @Test
    public void test() {
        PositiveCounter testCounter = new PositiveCounter(0);
        assertEquals(0, (int) testCounter.value());
        GenericFunction func1 = new GenericFunction("increment", 2);
        testCounter.invoke(func1);
        assertEquals(2, (int) testCounter.value());
        testCounter.invoke(func1);
        assertEquals(4, (int) testCounter.value());
        GenericFunction func2 = new GenericFunction("decrement", 1);
        testCounter.invoke(func2);
        assertEquals(3, (int) testCounter.value());
        GenericFunction func3 = new GenericFunction("decrement", 10);
        testCounter.invoke(func3);
        assertEquals(3, (int) testCounter.value());
    }
}