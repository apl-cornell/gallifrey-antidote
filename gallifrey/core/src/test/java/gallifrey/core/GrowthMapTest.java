package gallifrey.core;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

public class GrowthMapTest {
    @Test
    @SuppressWarnings("unchecked")
    public void test() {
        GrowthMap<String, Integer> testMap = new GrowthMap<String, Integer>();
        CRDT crdt = new CRDT(testMap);
        Map<String, Integer> expectedMap = new HashMap<String, Integer>();
        GenericFunction readfunc = new GenericFunction("value", null);
        assertEquals(expectedMap, (Map<String, Integer>) crdt.invoke(readfunc));

        List<Object> args1 = new ArrayList<Object>();
        args1.add("two");
        args1.add(2);
        GenericFunction func1 = new GenericFunction("add", null, args1);
        crdt.invoke(func1);
        expectedMap.put("two", 2);
        assertEquals(expectedMap, (Map<String, Integer>) crdt.invoke(readfunc));

        GrowthMap<String, Integer> testMap2 = new GrowthMap<String, Integer>();
        CRDT crdt2 = new CRDT(testMap2);
        Map<String, Integer> expectedMap2 = new HashMap<String, Integer>();
        List<Object> args2 = new ArrayList<Object>();
        args2.add("three");
        args2.add(3);
        GenericFunction func2 = new GenericFunction("add", null, args2);
        crdt2.invoke(func2);
        expectedMap2.put("three", 3);
        assertEquals(expectedMap2, (Map<String, Integer>) crdt2.invoke(readfunc));

        GenericFunction func3 = new GenericFunction("addMap", null, expectedMap2);
        crdt.invoke(func3);
        expectedMap.putAll(expectedMap2);
        assertEquals(expectedMap, (Map<String, Integer>) crdt.invoke(readfunc));
    }
}
