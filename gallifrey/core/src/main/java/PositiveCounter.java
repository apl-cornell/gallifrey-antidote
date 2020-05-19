package gallifrey.core;

import java.io.Serializable;

/**
 * An always Positive Counter
 */
public class PositiveCounter implements Serializable {
    private static final long serialVersionUID = 6L;
    int count;

    public final Class<?>[] value = new Class[] { };
    public final Class<?>[] increment = new Class[] { Integer.class };
    public final Class<?>[] decrement = new Class[] { Integer.class };

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