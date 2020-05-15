import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class CounterTest {
    @Test
    public void test() {
        Counter testCounter = new Counter(0);
        CRDT crdt = new CRDT(testCounter);
        GenericFunction readfunc = new GenericFunction("value");
        assertEquals(0, (int) crdt.invoke(readfunc));
        GenericFunction func1 = new GenericFunction("increment", 2);
        crdt.invoke(func1);
        assertEquals(2, (int) crdt.invoke(readfunc));
        crdt.invoke(func1);
        assertEquals(4, (int) crdt.invoke(readfunc));
        GenericFunction func2 = new GenericFunction("decrement", 1);
        crdt.invoke(func2);
        assertEquals(3, (int) crdt.invoke(readfunc));
    }
}