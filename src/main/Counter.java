package main;

import java.util.HashSet;
import java.util.Set;

/**
 * Counter
 */
public class Counter implements CRDT {
    private static final long serialVersionUID = 1L;
    private Set<Integer> IdSet;

    int count;

    public Counter(int val) {
        IdSet = new HashSet<>();
        count = val;
    }

    public int value() {
        System.out.println("did read");
        return count;
    }

    public void increment(int val) {
        System.out.println("did increment");
        count += val;
    }

    public void decrement(int val) {
        System.out.println("did decrement");
        count -= val;
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
        case "increment":
            increment((int) args);
            break;
        case "decrement":
            decrement((int) args);
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
        //IdSet = new HashSet<>();
    }

    public static void main(String[] args) {
        Counter testCounter = new Counter(0);
        int val = (int) testCounter.read();
        System.out.println(val);
        GenericFunction func1 = new GenericFunction("increment", 2);
        testCounter.invoke(func1);
        int val2 = (int) testCounter.read();
        System.out.println(val2);
        testCounter.invoke(func1);
        int val3 = (int) testCounter.read();
        System.out.println(val3);
        GenericFunction func2 = new GenericFunction("decrement", 1);
        testCounter.invoke(func2);
        int val4 = (int) testCounter.read();
        System.out.println(val4);

    }
}