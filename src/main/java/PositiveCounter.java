import java.util.Random;

/**
 * An always Positive Counter
 */
public class PositiveCounter extends CRDT {
    private static final long serialVersionUID = new Random().nextLong();
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
}