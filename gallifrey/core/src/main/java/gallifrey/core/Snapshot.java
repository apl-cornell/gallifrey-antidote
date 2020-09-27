package gallifrey.core;

import java.io.Serializable;

import gallifrey.core.CRDT;

public class Snapshot implements Serializable {
    private static final long serialVersionUID = 12L;
    public CRDT crdt;
    public MergeSortedSet effectbuffer;

    public Snapshot(CRDT crdt, MergeSortedSet sortedEffectSet) {
        this.crdt = crdt;
        this.effectbuffer = sortedEffectSet;
    }
}