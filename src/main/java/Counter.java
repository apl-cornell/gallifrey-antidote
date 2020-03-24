import java.util.Random;

/**
 * Counter
 */
public class Counter extends CRDT {
    private static final long serialVersionUID = new Random().nextLong();
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
}