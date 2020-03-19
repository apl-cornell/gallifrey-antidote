/**
 * Counter
 */
public class Counter extends CRDT {
    private static final long serialVersionUID = 1L;
    int count;

    public Class<?>[] increment = new Class[] { Integer.class };
    public Class<?>[] decrement = new Class[] { Integer.class };

    public Counter(int val) {
        count = val;
    }

    public Integer value() {
        return count;
    }

    public void increment(Integer val) {
        count += val;
    }

    public void decrement(Integer val) {
        count -= val;
    }

    public static void main(String[] args) {
        Counter testCounter = new Counter(0);
        int val = (int) testCounter.value();
        System.out.println(val);
        GenericFunction func1 = new GenericFunction("increment", 2);
        testCounter.invoke(func1);
        int val2 = (int) testCounter.value();
        System.out.println(val2);
        testCounter.invoke(func1);
        int val3 = (int) testCounter.value();
        System.out.println(val3);
        GenericFunction func2 = new GenericFunction("decrement", 1);
        testCounter.invoke(func2);
        int val4 = (int) testCounter.value();
        System.out.println(val4);
    }
}