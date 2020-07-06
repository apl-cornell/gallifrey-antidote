package gallifrey.core;

import java.io.Serializable;

interface AntidoteInterface extends Serializable {
    // update the crdt with obj(A Tuple of function name and args)
    Object invoke(GenericFunction obj);
}