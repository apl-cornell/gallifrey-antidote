package main;

import com.ericsson.otp.erlang.OtpErlangBinary;

/**
 * Counter
 */
public class Counter implements CRDT {
    int count;

    public Counter(int val) {
        count = val;
    }

    public int value() {
        System.out.println("did read");
        return count;
    }

    public void increment(int val) {
        System.out.println("did increment");
        count += val;
    }

    public void decrement(int val) {
        System.out.println("did decrement");
        count -= val;
    }

    @Override
    public void invoke(String func, OtpErlangBinary args) {
        switch (func) {
        case "increment":
            increment_bin(args);
            break;
        case "decrement":
            decrement_bin(args);
            break;

        default:
            throw new IllegalArgumentException(func + " is not a function for Counter");
        }
    }

    public void increment_bin(OtpErlangBinary args) {
        increment(CRDT.bin_to_int(args));
    }

    public void decrement_bin(OtpErlangBinary args) {
        decrement(CRDT.bin_to_int(args));
    }

    @Override
    public OtpErlangBinary read() {
        return CRDT.int_to_bin(value());
    }

    public static void main(String[] args) {
        Counter testCounter = new Counter(0);
        int val = CRDT.bin_to_int(testCounter.read());
        System.out.println(val);
        testCounter.invoke("increment", CRDT.int_to_bin(2));
        int val2 = CRDT.bin_to_int(testCounter.read());
        System.out.println(val2);
        testCounter.invoke("decrement", CRDT.int_to_bin(1));
        int val3 = CRDT.bin_to_int(testCounter.read());
        System.out.println(val3);

    }
}