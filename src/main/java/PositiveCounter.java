/**
 * An always Positive Counter
 */
public class PositiveCounter extends CRDT {
    private static final long serialVersionUID = 1L;
    int count;

    public Class<?>[] increment = new Class[] { Integer.class };
    public Class<?>[] decrement = new Class[] { Integer.class };

    public PositiveCounter(int val) {
        count = val;
    }

    public Integer value() {
        return count;
    }

    public void increment(Integer val) {
        count += val;
    }

    public void decrement(Integer val) {
        if (count > val) {
            count -= val;
        }
    }

    /** Testing */
    /*
     * public static void main(String[] args) { Counter testCounter = new
     * Counter(0); int val = (int) testCounter.read(); System.out.println(val);
     * GenericFunction func1 = new GenericFunction("increment", 2);
     * testCounter.invoke(func1); int val2 = (int) testCounter.read();
     * System.out.println(val2); testCounter.invoke(func1); int val3 = (int)
     * testCounter.read(); System.out.println(val3); GenericFunction func2 = new
     * GenericFunction("decrement", 1); testCounter.invoke(func2); int val4 = (int)
     * testCounter.read(); System.out.println(val4); GenericFunction func3 = new
     * GenericFunction("decrement", 10); testCounter.invoke(func3); int val5 = (int)
     * testCounter.read(); System.out.println(val5); }
     */
}