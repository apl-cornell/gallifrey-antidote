package gallifrey.frontend;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import gallifrey.core.*;

public class SharedObjectSimpleTest {
    @Test
    public void test() {
        Counter counter = new Counter(0);
        SharedObject obj = new SharedObject(counter);
        for (int i = 1; i <= 10; i++) {
            List<Object> args1 = new ArrayList<Object>();
            args1.add(2);
            obj.void_call("increment", args1);
            assertEquals(i + 1, obj.const_call("value"));
            List<Object> args3 = new ArrayList<Object>();
            args3.add(1);
            obj.void_call("decrement", args3);
            assertEquals(i, obj.const_call("value"));
        }
    }
}