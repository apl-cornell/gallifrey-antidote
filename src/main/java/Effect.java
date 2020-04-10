import java.io.Serializable;

import com.ericsson.otp.erlang.OtpErlangBinary;

// Antidote calls the processed update after downstream an "effect" that can
// then be applied to the crdt so that's the terminology I'm using for now
public class Effect implements Serializable, Comparable<Effect> {
    private static final long serialVersionUID = 9L;
    OtpErlangBinary func; // Could be a Generic Function or a CRDT Object
    VectorClock time;

    public Effect(OtpErlangBinary func, VectorClock time) {
        this.func = func;
        this.time = time;
    }

    @Override
    public int compareTo(Effect e) {
        return this.time.compareTo(e.time);
    }
}