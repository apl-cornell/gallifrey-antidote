package gallifrey.core;

import java.io.Serializable;

/**
 * Register
 */
public class Register<T> implements Serializable{
    private static final long serialVersionUID = 13L;
    private T val;

    public final Class<?>[] value = new Class[] { };
    public final Class<?>[] assign = {Object.class};

    public Register(T val) {
        this.val = val;
    }

    public T value() {
        return val;
    }

    public void assign(T val) {
        this.val = val;
    }
}