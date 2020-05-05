/**
 * Register
 */
public class Register<T> extends CRDT{
    private static final long serialVersionUID = 13L;
    private T value;

    public Class<?>[] assign = new Class[] { Object.class };

    public Register(T value) {
        this.value = value;
    }

    public T value() {
        return value;
    }

    public void assign(T value) {
        this.value = value;
    }
}