import java.io.Serializable;

interface Antidote_interface extends Serializable {
    // update the crdt with obj(A Tuple of function name and args)
    Object invoke(GenericFunction obj);

    // read the current value of the CRDT
    // We no longer use this since we give antidote a dummy read value and read the
    // value of the object ourselves via rmi
    // Object value();
}