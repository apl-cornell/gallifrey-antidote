package gallifrey.backend;

import gallifrey.core.CRDT;

public class CRDTEffect extends Effect {
    private static final long serialVersionUID = 10L;
    CRDT crdt;

    public CRDTEffect(CRDT crdt, VectorClock time) {
        super(time);
        this.crdt = crdt;
    }
}