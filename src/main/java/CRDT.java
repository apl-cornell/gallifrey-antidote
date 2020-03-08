import java.io.Serializable;

interface CRDT extends Serializable {
  void invoke(GenericFunction obj);

  Object read();

  void snapshot();

  // just serialize for snapshot read

  /*
   * import com.ericsson.otp.erlang.OtpErlangBinary;
   * public static int bin_to_int(OtpErlangBinary val) { return (int)
   * val.getObject(); }
   *
   * public static OtpErlangBinary int_to_bin(int val) { return new
   * OtpErlangBinary(Integer.valueOf(val)); }
   */
}