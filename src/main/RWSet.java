package main;

import java.util.Set;
import java.util.HashSet;

/**
 * Remove Wins Set
 */
public class RWSet implements CRDT {
    private static final long serialVersionUID = 1L;
    Set<Object> addset = new HashSet<Object>();
    Set<Object> removeset = new HashSet<Object>();

    public RWSet() {
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

    public void remove(Object elem) {
        removeset.add(elem);
        System.out.println("did remove");
    }

    @Override
    public void invoke(String func, Object args) {
        switch (func) {
        case "add":
            add(args);
            break;
        case "remove":
            remove(args);
            break;

        default:
            throw new IllegalArgumentException(func + " is not a function for Counter");
        }
    }

    @Override
    public Object read() {
        return value();
    }

    public static void main(String[] args) {
        RWSet testSet = new RWSet();
        Set<Integer> val = (Set<Integer>)testSet.read();
        System.out.println(val);
        testSet.invoke("add", 2);
        Set<Integer> val2 = (Set<Integer>)testSet.read();
        System.out.println(val2);
        testSet.invoke("remove", 2);
        Set<Integer> val3 = (Set<Integer>)testSet.read();
        System.out.println(val3);

    }
}