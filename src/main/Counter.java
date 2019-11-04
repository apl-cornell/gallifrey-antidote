package main;

/**
 * Counter
 */
public class Counter implements CRDT {
    private static final long serialVersionUID = 1L;
    int count;

    public Counter(int val) {
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
    public void invoke(String func, Object args) {
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

    public static void main(String[] args) {
        Counter testCounter = new Counter(0);
        int val = (int) testCounter.read();
        System.out.println(val);
        testCounter.invoke("increment", 2);
        int val2 = (int) testCounter.read();
        System.out.println(val2);
        testCounter.invoke("decrement", 1);
        int val3 = (int) testCounter.read();
        System.out.println(val3);

    }
}