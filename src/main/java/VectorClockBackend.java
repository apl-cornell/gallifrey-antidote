import java.util.Hashtable;
import java.util.Map;
import java.util.HashSet;
import java.util.TreeSet;
import java.util.Random;

import com.ericsson.otp.erlang.OtpErlangBinary;
import com.ericsson.otp.erlang.OtpErlangTuple;

import com.ericsson.otp.erlang.OtpErlangObject;
import com.ericsson.otp.erlang.OtpErlangMap;

public class VectorClockBackend extends AntidoteBackend {

    public class CRDTMapEntry {
        public CRDT object;
        public HashSet<Integer> idlist; // This might be obsolete witht the effect buffer
        public TreeSet<GenericEffect> effectbuffer;

        public CRDTMapEntry(CRDT Object, HashSet<Integer> funcIdList, TreeSet<GenericEffect> effectList) {
            this.object = Object;
            this.idlist = funcIdList;
            this.effectbuffer = effectList;
        }
    }

    Map<OtpErlangBinary, CRDTMapEntry> ObjectTable = new Hashtable<>();
    VectorClock GlobalClockTime = new VectorClock();

    public VectorClockBackend(String NodeName, String MailBox, String cookie) {
        super(NodeName, MailBox, cookie);
    }

    @Override
    public OtpErlangBinary value(OtpErlangBinary JavaObjectId) throws NoSuchObjectException {
        CRDT crdt_object = ObjectTable.get(JavaObjectId).object;
        if (crdt_object == null) {
            throw new NoSuchObjectException();
        }

        // Add effects to a throwaway object to get the value
        CRDT temp_crdt_object = crdt_object.deepClone();
        for (GenericEffect e : ObjectTable.get(JavaObjectId).effectbuffer) {
            temp_crdt_object.invoke((GenericFunction) e.func);
        }

        return new OtpErlangBinary(temp_crdt_object.value());
    }

    @Override
    public OtpErlangBinary update(OtpErlangBinary JavaObjectId, OtpErlangBinary binary) throws NoSuchObjectException {
        if (!ObjectTable.containsKey(JavaObjectId)) {
            try {
                CRDT crdt_object = ((CRDTEffect) binary.getObject()).crdt;
                CRDTMapEntry maptriple = new CRDTMapEntry(crdt_object, new HashSet<Integer>(),
                        new TreeSet<GenericEffect>());
                ObjectTable.put(JavaObjectId, maptriple);
            } catch (ClassCastException e) {
                // We don't have the object and we have been given an update
                // so we need to request the object from antidote and try again
                assert (binary.getObject().getClass() == GenericFunction.class);
                throw new NoSuchObjectException();
            }
        } else {
            try {
                GenericEffect updateEffect = (GenericEffect) binary.getObject();
                GenericFunction func = updateEffect.func;
                HashSet<Integer> FunctionIdList = ObjectTable.get(JavaObjectId).idlist;
                TreeSet<GenericEffect> EffectList = ObjectTable.get(JavaObjectId).effectbuffer;
                if (!FunctionIdList.contains(func.getId())) {
                    FunctionIdList.add(func.getId());
                    EffectList.add(updateEffect);
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
    public OtpErlangBinary downstream(OtpErlangBinary JavaObjectId, OtpErlangBinary binary, OtpErlangMap clock) {
        VectorClock effectClock = new VectorClock(clock);
        GlobalClockTime.maxClock(effectClock);

        OtpErlangBinary bin;
        try {
            CRDTEffect effect = new CRDTEffect((CRDT) binary.getObject(), effectClock);
            bin = new OtpErlangBinary(effect);
        } catch (ClassCastException e) {
            GenericEffect effect = new GenericEffect((GenericFunction) binary.getObject(), effectClock);
            bin = new OtpErlangBinary(effect);
        }

        return bin;
    }

    @Override
    public OtpErlangTuple snapshot(OtpErlangBinary JavaObjectId) throws NoSuchObjectException {
        // assert that object is in table else request it
        CRDT crdt_object = ObjectTable.get(JavaObjectId).object;
        if (crdt_object == null) {
            throw new NoSuchObjectException();
        }
        CRDT new_crdt_object = crdt_object.deepClone();
        OtpErlangBinary new_key = newJavaObjectId();

        // ObjectTable.get(JavaObjectId).idlist.clear();
        // ObjectTable.remove(ERLObjectId);

        // Apply any effects that have passed the clock time to the new copy of the
        // object
        TreeSet<GenericEffect> new_crdt_effect_buffer = new TreeSet<GenericEffect>();
        for (GenericEffect e : ObjectTable.get(JavaObjectId).effectbuffer) {
            // Use a correct compare based on types
            if (e.time.lessthan(this.GlobalClockTime)) {
                new_crdt_object.invoke((GenericFunction) e.func);
            } else {
                new_crdt_effect_buffer.add(e);
            }
        }

        CRDTMapEntry maptriple = new CRDTMapEntry(new_crdt_object, new HashSet<Integer>(), new_crdt_effect_buffer);
        ObjectTable.put(new_key, maptriple);
        OtpErlangObject[] emptypayload = new OtpErlangObject[2];
        emptypayload[0] = new_key;
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
        VectorClockBackend backend = new VectorClockBackend(nodename, "javamailbox", "antidote");
        backend.run();
    }
}
