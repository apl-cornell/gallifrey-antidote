import java.io.Serializable;

interface Antidote_interface extends Serializable {
    // update the crdt with obj(A Tuple of function name and args)
    Object invoke(GenericFunction obj);
}