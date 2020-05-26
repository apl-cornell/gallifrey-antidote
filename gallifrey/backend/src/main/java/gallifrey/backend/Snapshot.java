package gallifrey.backend;

import java.io.Serializable;
import java.util.TreeSet;

import gallifrey.core.CRDT;

public class Snapshot implements Serializable {
    private static final long serialVersionUID = 12L;
    public CRDT crdt;
    public TreeSet<GenericEffect> effectbuffer;

    public Snapshot(CRDT crdt, TreeSet<GenericEffect> sortedEffectSet) {
        this.crdt = crdt;
        this.effectbuffer = sortedEffectSet;
    }
}