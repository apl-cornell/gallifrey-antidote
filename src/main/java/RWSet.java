import java.util.Set;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * Remove Wins Set
 */
public class RWSet<T extends Object> extends CRDT {
    private static final long serialVersionUID = 1L;
    private Set<T> addset;
    private Set<T> removeset;

    public RWSet() {
        addset = new HashSet<T>();
        removeset = new HashSet<T>();
    }

    public Set<T> value() {
        Set<T> currentset = new HashSet<T>();
        for (T elem : new ArrayList<T>(addset)) {
            if (!removeset.contains(elem)) {
                currentset.add(elem);
            }
        }
        return currentset;
    }

    public void add(T elem) {
        addset.add(elem);
        System.out.println("did add");
    }

    public void addSet(Set<T> elems) {
        for (T elem : new ArrayList<T>(elems)) {
            addset.add(elem);
        }
    }

    public void remove(T elem) {
        removeset.add(elem);
        System.out.println("did remove");
    }

    public static void main(String[] args) {
        RWSet<Integer> testSet = new RWSet<Integer>();
        Set<Integer> val = testSet.value();
        System.out.println(val);
        GenericFunction func1 = new GenericFunction("add", 2);
        testSet.invoke(func1);
        Set<Integer> val2 = (Set<Integer>) testSet.value();
        System.out.println(val2);
        GenericFunction func2 = new GenericFunction("remove", 2);
        testSet.invoke(func2);
        Set<Integer> val3 = (Set<Integer>) testSet.value();
        System.out.println(val3);

        Set<Integer> smallSet = new HashSet<Integer>();
        smallSet.add(4);
        smallSet.add(5);

        GenericFunction func3 = new GenericFunction("addSet", smallSet);
        testSet.invoke(func3);
        Set<Integer> val4 = (Set<Integer>) testSet.value();
        System.out.println(val4);

    }
}