package gallifrey.core;

import java.io.Serializable;
import java.util.List;

import com.ericsson.otp.erlang.OtpErlangBinary;
import gallifrey.core.CRDT;

public class Snapshot implements Serializable {
    private static final long serialVersionUID = 12L;
    public CRDT crdt;
    public OtpErlangBinary objectid;
    public MergeSortedSet effectbuffer;

    public Snapshot(CRDT crdt, MergeSortedSet sortedEffectSet, OtpErlangBinary objectid) {
        this.crdt = crdt;
        this.effectbuffer = sortedEffectSet;
        this.objectid = objectid;
    }

    public Snapshot(CRDT crdt, MergeSortedSet sortedEffectSet) {
        this.crdt = crdt;
        this.effectbuffer = sortedEffectSet;
    }

    public void addEffects(List<GenericEffect> effectList) {
        for (GenericEffect effect : effectList) {
            this.effectbuffer.add(effect);
        }
    }
}