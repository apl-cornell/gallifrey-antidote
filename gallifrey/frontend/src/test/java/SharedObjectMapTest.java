package gallifrey.frontend;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import gallifrey.core.SharedMap;

public class SharedObjectMapTest {
    @Test
    public void test() {
        SharedMap<String, Integer> map = new SharedMap<String, Integer>(10);
        SharedObject obj = new SharedObject(map);

        List<Object> args1 = new ArrayList<Object>();
        args1.add("key1");
        args1.add(1);
        obj.void_call("put", args1);
        List<Object> args2 = new ArrayList<Object>();
        args2.add("key1");
        assertEquals(true, obj.const_call("containsKey", args2));
        assertEquals(1, obj.const_call("get", args2));

        HashMap<String, Integer> map2 = (HashMap<String, Integer>) obj.const_call("value");

        SharedMap<String, Integer> map3 = new SharedMap<String, Integer>(map2);
        SharedObject obj2 = new SharedObject(map3);
        assertEquals(true, obj2.const_call("containsKey", args2));
        assertEquals(1, obj2.const_call("get", args2));
    }
}