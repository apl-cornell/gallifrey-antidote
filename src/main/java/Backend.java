import java.util.Hashtable;
import java.util.Map;
import java.util.HashSet;
import java.util.Random;

import com.ericsson.otp.erlang.OtpErlangBinary;
import com.ericsson.otp.erlang.OtpErlangTuple;
import com.ericsson.otp.erlang.OtpErlangObject;

public class Backend extends AntidoteBackend {

    public class CRDTMapEntry<T, S> {
        public T object;
        public S idlist;

        public CRDTMapEntry(T Object, S funcIdList) {
            this.object = Object;
            this.idlist = funcIdList;
        }
    }

    Map<OtpErlangBinary, CRDTMapEntry<CRDT, HashSet<Integer>>> ObjectTable = new Hashtable<>();

    public Backend(String NodeName, String MailBox, String cookie) {
        super(NodeName, MailBox, cookie);
    }

    @Override
    public OtpErlangBinary value(OtpErlangBinary JavaObjectId) throws NoSuchObjectException {
        CRDT crdt_object = ObjectTable.get(JavaObjectId).object;
        if (crdt_object == null) {
            throw new NoSuchObjectException();
        }
        return new OtpErlangBinary(crdt_object.value());
    }

    @Override
    public OtpErlangBinary update(OtpErlangBinary JavaObjectId, OtpErlangBinary binary) throws NoSuchObjectException {
        if (!ObjectTable.containsKey(JavaObjectId)) {
            try {
                CRDT crdt_object = (CRDT) binary.getObject();
                CRDTMapEntry<CRDT, HashSet<Integer>> mappair = new CRDTMapEntry<CRDT, HashSet<Integer>>(crdt_object,
                        new HashSet<Integer>());
                ObjectTable.put(JavaObjectId, mappair);
            } catch (ClassCastException e) {
                // We don't have the object and we have been given an update
                // so we need to request the object from antidote and try again
                assert (binary.getObject().getClass() == GenericFunction.class);
                throw new NoSuchObjectException();
            }
        } else {
            try {
                CRDT crdt_object = ObjectTable.get(JavaObjectId).object;
                GenericFunction func = (GenericFunction) binary.getObject();
                HashSet<Integer> FunctionIdList = ObjectTable.get(JavaObjectId).idlist;
                if (!FunctionIdList.contains(func.getId())) {
                    crdt_object.invoke(func);
                    FunctionIdList.add(func.getId());
                }
            } catch (ClassCastException e) {
                /* intentionally ignore this exception */
                // This happens because we don't have an Idset for crdt initializations through
                // updates so we got a redundant crdt object(something we already have)
                assert (binary.getObject().getClass() == CRDT.class);
            }
        }

        return JavaObjectId;
    }

    @Override
    public OtpErlangBinary downstream(OtpErlangBinary JavaObjectId, OtpErlangBinary binary) {
        // TBD what if anything we want to do here otherwise something like this
        return binary;
    }

    @Override
    public OtpErlangTuple snapshot(OtpErlangBinary JavaObjectId) throws NoSuchObjectException {
        // assert that object is in table else request it
        CRDT crdt_object = ObjectTable.get(JavaObjectId).object;
        if (crdt_object == null) {
            throw new NoSuchObjectException();
        }

        // ObjectTable.get(JavaObjectId).idlist.clear();
        // ObjectTable.remove(ERLObjectId);

        OtpErlangBinary new_key = newJavaObjectId();
        CRDTMapEntry<CRDT, HashSet<Integer>> mappair = new CRDTMapEntry<CRDT, HashSet<Integer>>(
                crdt_object.deepClone(), new HashSet<Integer>());
        ObjectTable.put(new_key, mappair);
        OtpErlangObject[] emptypayload = new OtpErlangObject[2];
        emptypayload[0] = JavaObjectId; // new_key;
        emptypayload[1] = new OtpErlangBinary(crdt_object);
        OtpErlangTuple new_snapshot = new OtpErlangTuple(emptypayload);
        return new_snapshot;
    }

    @Override
    public OtpErlangBinary newJavaObjectId() {
        byte[] b = new byte[20];
        new Random().nextBytes(b);
        return new OtpErlangBinary(b);
    }

    public static void main(String[] args) {
        String nodename;
        if (args.length >= 1) {
            nodename = args[0];
        } else {
            nodename = "JavaNode@127.0.0.1";
        }
        /*
         * String target; if (args.length >= 2) { target = args[1]; } else { target =
         * "antidote@127.0.0.1"; }
         */
        Backend backend = new Backend(nodename, "javamailbox", "antidote");
        backend.run();
    }
}
