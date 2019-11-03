package main;

import com.ericsson.otp.erlang.OtpErlangBinary;

interface CRDT {
    void invoke(String func, OtpErlangBinary args);

    OtpErlangBinary read();

    public static int bin_to_int(OtpErlangBinary val) {
        return (Integer) val.getObject();
    }

    public static OtpErlangBinary int_to_bin(int val) {
        return new OtpErlangBinary(Integer.valueOf(val));
    }
}