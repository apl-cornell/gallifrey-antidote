import java.io.Serializable;

import com.ericsson.otp.erlang.OtpErlangBinary;


interface CRDT extends Serializable {
    void invoke(GenericFunction obj);

    Object read();

    void snapshot();

    // just serialize for snapshot read

    /*
     * public static int bin_to_int(OtpErlangBinary val) { return (int)
     * val.getObject(); }
     *
     * public static OtpErlangBinary int_to_bin(int val) { return new
     * OtpErlangBinary(Integer.valueOf(val)); }
     */
}