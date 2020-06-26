package gallifrey.backend;

import gallifrey.core.GenericFunction;
import gallifrey.core.VectorClock;

public class GenericEffect extends Effect {
    private static final long serialVersionUID = 11L;
    GenericFunction func;

    public GenericEffect(GenericFunction func, VectorClock time) {
        super(time);
        this.func = func;
    }

    public MergeComparator<GenericFunction> get_merge_strategy(){
	return func.merge_strategy();
    }
}
