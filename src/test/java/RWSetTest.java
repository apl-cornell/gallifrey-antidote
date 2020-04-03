import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;

public class RWSetTest {
    @Test
    public void test() {
        RWSet<Integer> testSet = new RWSet<Integer>();
        HashSet<Integer> expectedSet = new HashSet<Integer>();
        assertEquals(expectedSet, (Set<Integer>) testSet.value());
        GenericFunction func1 = new GenericFunction("add", 2);
        testSet.invoke(func1);
        expectedSet.add(2);
        assertEquals(expectedSet, (Set<Integer>) testSet.value());
        GenericFunction func2 = new GenericFunction("remove", 2);
        testSet.invoke(func2);
        expectedSet.remove(2);
        assertEquals(expectedSet, (Set<Integer>) testSet.value());

        Set<Integer> smallSet = new HashSet<Integer>();
        smallSet.add(4);
        smallSet.add(5);

        GenericFunction func3 = new GenericFunction("addSet", smallSet);
        testSet.invoke(func3);
        for (Integer elem : new ArrayList<Integer>(smallSet)) {
            expectedSet.add(elem);
        }
        assertEquals(expectedSet, (Set<Integer>) testSet.value());
    }
}