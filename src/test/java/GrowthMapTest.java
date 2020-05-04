import org.junit.Test;
import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

public class GrowthMapTest {
    @Test
    public void test() {
        GrowthMap<String, Integer> testMap = new GrowthMap<String, Integer>();
        Map<String, Integer> expectedMap = new HashMap<String, Integer>();
        assertEquals(expectedMap, (Map<String, Integer>) testMap.value());

        List<Object> args1 = new ArrayList<Object>();
        args1.add("two");
        args1.add(2);
        GenericFunction func1 = new GenericFunction("add", args1);
        testMap.invoke(func1);
        expectedMap.put("two", 2);
        assertEquals(expectedMap, (Map<String, Integer>) testMap.value());

        GrowthMap<String, Integer> testMap2 = new GrowthMap<String, Integer>();
        Map<String, Integer> expectedMap2 = new HashMap<String, Integer>();
        List<Object> args2 = new ArrayList<Object>();
        args2.add("three");
        args2.add(3);
        GenericFunction func2 = new GenericFunction("add", args2);
        testMap2.invoke(func2);
        expectedMap2.put("three", 3);
        assertEquals(expectedMap2, (Map<String, Integer>) testMap2.value());

        GenericFunction func3 = new GenericFunction("addMap", expectedMap2);
        testMap.invoke(func3);
        expectedMap.putAll(expectedMap2);
        assertEquals(expectedMap, (Map<String, Integer>) testMap.value());
    }
}