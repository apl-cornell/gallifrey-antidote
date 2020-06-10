package gallifrey.core;

import java.util.Map.Entry;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.Set;
import java.io.Serializable;

import com.ericsson.otp.erlang.OtpErlangObject;
import com.ericsson.otp.erlang.OtpErlangMap;
import com.ericsson.otp.erlang.OtpErlangLong;
import com.ericsson.otp.erlang.OtpErlangTuple;

public class VectorClock implements Serializable, Comparable<VectorClock> {
    private static final long serialVersionUID = 8L;
    // https://github.com/AntidoteDB/vectorclock/blob/master/src/vectorclock.erl

    public HashMap<String, Long> vectorclock;

    public VectorClock() {
        vectorclock = new HashMap<String, Long>();
    }

    // Takes the antidote vectorclock implementation and converts the map to java
    // for use here
    public VectorClock(OtpErlangMap clock) {
        vectorclock = new HashMap<String, Long>();

        for (Entry<OtpErlangObject, OtpErlangObject> entry : clock.entrySet()) {
            // Each entry is {NodeName, Meta info} => positive integer
            // I think the Meta info is Key, Type, Bucket but I'm not positive... See
            // Bound_object() in antidote.hrl
            String key = ((OtpErlangTuple) entry.getKey()).elementAt(0).toString();
            Long value = ((OtpErlangLong) entry.getValue()).longValue(); // this will wrap by complement of long
            vectorclock.put(key, value);
        }
    }

    public Long get(String key) {
        Long val = vectorclock.get(key);
        if (val == null) {
            return Long.valueOf(0); // Provide 0 as a default value for a key
        }
        return val;
    }

    public void updateClock(VectorClock c) {
        for (String key : c.vectorclock.keySet()) {
            if (c.get(key).compareTo(this.get(key)) == 1) { // greater than
                this.vectorclock.put(key, c.get(key));
            }
        }
    }

    public boolean lessthan(VectorClock c) {
        if (c.vectorclock.keySet().isEmpty() && this.vectorclock.keySet().isEmpty())
            return false;
        for (String key : c.vectorclock.keySet()) {
            if (this.get(key).compareTo(c.get(key)) != -1) { // greater than or equal to
                return false;
            }
        }
        return true;
    }

    public boolean isEmpty() {
        return this.vectorclock.isEmpty();
    }

    @Override
    public int compareTo(VectorClock c) {
        if (this.lessthan(c))
            return -1;
        if (c.lessthan(this))
            return 1; // greater than
        // Some kind of concurrent clock so use ordered keys to provide a total ordering
        Set<String> allkeys = new TreeSet<String>();
        allkeys.addAll(this.vectorclock.keySet());
        allkeys.addAll(c.vectorclock.keySet());
        // Try and find a key that is not equal in both clocks and use that to order
        for (String key : allkeys) {
            int comparison = this.get(key).compareTo(c.get(key));
            if (comparison != 0)
                return comparison;
        }
        return 0;
    }
}