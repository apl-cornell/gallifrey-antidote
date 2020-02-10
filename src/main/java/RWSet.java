import java.util.Set;
import java.util.HashSet;

/**
 * Remove Wins Set
 */
public class RWSet implements CRDT {
    private static final long serialVersionUID = 1L;
    private Set<Object> addset;
    private Set<Object> removeset;
    private Set<Integer> IdSet;

    public RWSet() {
        addset = new HashSet<Object>();
        removeset = new HashSet<Object>();
        IdSet = new HashSet<>();
    }

    public Set<Object> value() {
        Set<Object> currentset= new HashSet<Object>();
        for (Object elem: addset.toArray()){
            if (!removeset.contains(elem)){
                currentset.add(elem);
            }
        }
        return currentset;
    }

    public void add(Object elem) {
        addset.add(elem);
        System.out.println("did add");
    }
    public void addSet(Set<Object> elems) {
        for (Object elem: elems.toArray()){
            addset.add(elem);
        }
    }

    public void remove(Object elem) {
        removeset.add(elem);
        System.out.println("did remove");
    }

    @Override
    public void invoke(GenericFunction obj) {
        String func = obj.getFunctionName();
        Object args = obj.getArgument();
        Integer id = obj.getId();
        if (IdSet.contains(id)) {
            System.out.println("We have already done this");
            return;
        } else {
            IdSet.add(id);
        }
        switch (func) {
        case "add":
            add(args);
            break;
        case "remove":
            remove(args);
            break;
        case "addSet":
            addSet((Set<Object>)args);
            break;

        default:
            throw new IllegalArgumentException(func + " is not a function for Counter");
        }
    }

    @Override
    public Object read() {
        return value();
    }

    @Override
    public void snapshot() {
        IdSet = new HashSet<>();
    }

    public static void main(String[] args) {
        RWSet testSet = new RWSet();
        Set<Integer> val = (Set<Integer>)testSet.read();
        System.out.println(val);
        GenericFunction func1 = new GenericFunction("add", 2);
        testSet.invoke(func1);
        Set<Integer> val2 = (Set<Integer>)testSet.read();
        System.out.println(val2);
        GenericFunction func2 = new GenericFunction("remove", 2);
        testSet.invoke(func2);
        Set<Integer> val3 = (Set<Integer>)testSet.read();
        System.out.println(val3);

        Set<Integer> smallSet = new HashSet<Integer>();
        smallSet.add(4);
        smallSet.add(5);

        GenericFunction func3 = new GenericFunction("addSet", smallSet);
        testSet.invoke(func3);
        Set<Integer> val4 = (Set<Integer>) testSet.read();
        System.out.println(val4);

    }
}