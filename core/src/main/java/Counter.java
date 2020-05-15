import java.io.Serializable;

/**
 * Counter
 */
public class Counter implements Serializable {
    private static final long serialVersionUID = 1L;
    int count;

    public final Class<?>[] value = new Class[] { };
    public final Class<?>[] increment = new Class[] { Integer.class };
    public final Class<?>[] decrement = new Class[] { Integer.class };

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