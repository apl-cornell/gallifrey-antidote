/**
 * An always Positive Counter
 */
public class PositiveCounter extends CRDT {
    private static final long serialVersionUID = 6L;
    int count;

    public final Class<?>[] increment = new Class[] { Integer.class };
    public final Class<?>[] decrement = new Class[] { Integer.class };

    public PositiveCounter(int val) {
        super();
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