package gallifrey.backend;

import java.io.Serializable;

import gallifrey.core.CRDT;
import gallifrey.core.VectorClock;

public class Snapshot implements Serializable {
    private static final long serialVersionUID = 12L;
    public CRDT crdt;
    public MergeSortedSet effectbuffer;
    public VectorClock lastUpdateTime;

    public Snapshot(CRDT crdt, MergeSortedSet sortedEffectSet, VectorClock lastUpdateTime) {
        this.crdt = crdt;
        this.effectbuffer = sortedEffectSet;
        this.lastUpdateTime = lastUpdateTime;
    }
}