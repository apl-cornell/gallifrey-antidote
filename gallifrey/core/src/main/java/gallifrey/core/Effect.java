package gallifrey.core;

import java.io.Serializable;

// Antidote calls the processed update after downstream an "effect" that can
// then be applied to the crdt so that's the terminology I'm using for now
abstract class Effect implements Serializable, Comparable<Effect> {
    private static final long serialVersionUID = 9L;
    public VectorClock time;

    public Effect(VectorClock time) {
        this.time = time;
    }

    @Override
    public int compareTo(Effect e) {
        return this.time.compareTo(e.time);
    }
}