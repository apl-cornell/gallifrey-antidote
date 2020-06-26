package gallifrey.core;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;

public class RWSetTest {
    @Test
    public void test() {
        RWSet<Integer> testSet = new RWSet<Integer>(Integer.class);
        CRDT crdt = new CRDT(testSet);
        GenericFunction readfunc = new GenericFunction("value",null);
        HashSet<Integer> expectedSet = new HashSet<Integer>();
        assertEquals(expectedSet, (Set<Integer>) crdt.invoke(readfunc));
        GenericFunction func1 = new GenericFunction("add", null, 2);
        crdt.invoke(func1);
        expectedSet.add(2);
        assertEquals(expectedSet, (Set<Integer>) crdt.invoke(readfunc));
        GenericFunction func2 = new GenericFunction("remove",null, 2);
        crdt.invoke(func2);
        expectedSet.remove(2);
        assertEquals(expectedSet, (Set<Integer>) crdt.invoke(readfunc));

        Set<Integer> smallSet = new HashSet<Integer>();
        smallSet.add(4);
        smallSet.add(5);

        GenericFunction func3 = new GenericFunction("addSet",null, smallSet);
        crdt.invoke(func3);
        for (Integer elem : new ArrayList<Integer>(smallSet)) {
            expectedSet.add(elem);
        }
        assertEquals(expectedSet, (Set<Integer>) crdt.invoke(readfunc));
    }
}
