package gallifrey.core;

import gallifrey.core.CRDT;
import gallifrey.core.Effect;
import gallifrey.core.VectorClock;

public class CRDTEffect extends Effect {
    private static final long serialVersionUID = 10L;
    public CRDT crdt;

    public CRDTEffect(CRDT crdt, VectorClock time) {
        super(time);
        this.crdt = crdt;
    }
}