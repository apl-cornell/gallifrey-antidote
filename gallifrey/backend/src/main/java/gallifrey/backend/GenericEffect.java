package gallifrey.backend;

import gallifrey.core.GenericFunction;
import gallifrey.core.VectorClock;
import gallifrey.core.MergeComparator;

public class GenericEffect extends Effect {
    private static final long serialVersionUID = 11L;
    public final GenericFunction func;

    public GenericEffect(GenericFunction func, VectorClock time) {
        super(time);
        this.func = func;
    }

    public MergeComparator get_merge_strategy(){
	return func.merge_strategy;
    }
}
