package gallifrey.frontend;

import java.io.Serializable;

public class SharedLinkedList<T> implements Serializable {
    private static final long serialVersionUID = 18L;
    public T data;
    public SharedObject next;

    public final Class<?>[] getData = new Class[] {};
    public final Class<?>[] setData = new Class[] { Object.class };
    public final Class<?>[] getNext = new Class[] {};
    public final Class<?>[] setNext = new Class[] { SharedObject.class };

    public SharedLinkedList(T data) {
        this.data = data;
    }

    public T getData() {
        return this.data;
    }

    public T setData(T data) {
        return this.data = data;
    }

    public SharedObject getNext() {
        return this.next;
    }

    public void setNext(SharedObject next) {
        this.next = next;
    }
}