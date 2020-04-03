import java.io.Serializable;

interface Antidote_interface extends Serializable {
    // update the crdt with obj(A Tuple of function name and args)
    void invoke(GenericFunction obj);

    // read the current value of the CRDT
    Object value();
}