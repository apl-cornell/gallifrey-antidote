import java.math.BigInteger;

import java.util.Map.Entry;
import java.util.HashMap;

import java.io.Serializable;

import com.ericsson.otp.erlang.OtpErlangObject;
import com.ericsson.otp.erlang.OtpErlangMap;
import com.ericsson.otp.erlang.OtpErlangLong;
import com.ericsson.otp.erlang.OtpErlangTuple;

public class VectorClock implements Serializable, Comparable<VectorClock> {
    private static final long serialVersionUID = 8L;
    // https://github.com/AntidoteDB/vectorclock/blob/master/src/vectorclock.erl

    public HashMap<String, BigInteger> vectorclock;

    public VectorClock() {
        vectorclock = new HashMap<String, BigInteger>();
    }

    // Takes the antidote vectorclock implementation and converts the map to java for use here
    public VectorClock(OtpErlangMap clock) {
        vectorclock = new HashMap<String, BigInteger>();

        for (Entry<OtpErlangObject, OtpErlangObject> entry : clock.entrySet()) {
            // Each entry is {NodeName, Meta info} => positive integer
            // I think the Meta info is Key, Type, Bucket but I'm not positive... See Bound_object() in antidote.hrl
            String key = ((OtpErlangTuple) entry.getKey()).elementAt(0).toString();
            System.out.println(key); // TODO remove
            BigInteger value = ((OtpErlangLong) entry.getValue()).bigIntegerValue();
            vectorclock.put(key, value);
        }

    }

    public BigInteger get(String key) {
        BigInteger val = vectorclock.get(key);
        if (val == null) {
            return BigInteger.ZERO; // Provide 0 as a default value
        }
        return val;
    }

    public void maxClock(VectorClock c) {
        for (String key : c.vectorclock.keySet()) {
            if (c.get(key).compareTo(this.get(key)) == 1) { // greater than
                this.vectorclock.put(key, c.get(key));
            }
        }
    }

    public boolean lessthan(VectorClock c) {
        for (String key : c.vectorclock.keySet()) {
            if (this.get(key).compareTo(c.get(key)) != -1) { // greater than or equal to
                return false;
            }
        }
        return true;
    }

    @Override
    public int compareTo(VectorClock c) {
        if (this.lessthan(c))
            return -1;
        if (c.lessthan(this))
            return 1; // greater than
        return 0;
    }
}