import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CounterTest {
    @Test
    public void test() {
        Counter testCounter = new Counter(0);
        assertEquals(0, (int) testCounter.value());
        GenericFunction func1 = new GenericFunction("increment", 2);
        testCounter.invoke(func1);
        assertEquals(2, (int) testCounter.value());
        testCounter.invoke(func1);
        assertEquals(4, (int) testCounter.value());
        GenericFunction func2 = new GenericFunction("decrement", 1);
        testCounter.invoke(func2);
        assertEquals(3, (int) testCounter.value());
    }
}