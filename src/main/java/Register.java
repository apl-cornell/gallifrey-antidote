/**
 * Register
 */
public class Register<T> extends CRDT{
    private static final long serialVersionUID = 13L;
    private T value;

    public final Class<?>[] assign;

    public Register(Class<T> cls, T value) {
        super();
        assign = new Class[] { cls };
        this.value = value;
    }

    public T value() {
        return value;
    }

    public void assign(T value) {
        this.value = value;
    }
}