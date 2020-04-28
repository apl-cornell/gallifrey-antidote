import java.util.Hashtable;
import java.util.Map;
import java.util.TreeSet;
import java.util.Random;

import com.ericsson.otp.erlang.OtpErlangBinary;
import com.ericsson.otp.erlang.OtpErlangTuple;

import com.ericsson.otp.erlang.OtpErlangObject;
import com.ericsson.otp.erlang.OtpErlangMap;

public class VectorClockBackend extends AntidoteBackend {
    Map<OtpErlangBinary, Snapshot> ObjectTable = new Hashtable<>();
    VectorClock GlobalClockTime = new VectorClock();

    public VectorClockBackend(String NodeName, String MailBox, String cookie) {
        super(NodeName, MailBox, cookie);
    }

    @Override
    public OtpErlangBinary value(OtpErlangBinary JavaObjectId) throws NoSuchObjectException {
        CRDT crdt_object = ObjectTable.get(JavaObjectId).crdt;
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
                Snapshot mapentry = new Snapshot(crdt_object, new TreeSet<GenericEffect>());
                ObjectTable.put(JavaObjectId, mapentry);
            } catch (ClassCastException e) {
                // We don't have the object and we have been given an update
                // so we need to request the object from antidote and try again
                assert (binary.getObject().getClass() == GenericEffect.class);
                throw new NoSuchObjectException();
            }
        } else {
            try {
                GenericEffect updateEffect = (GenericEffect) binary.getObject();
                TreeSet<GenericEffect> sortedEffectSet = ObjectTable.get(JavaObjectId).effectbuffer;
                sortedEffectSet.add(updateEffect);
            } catch (ClassCastException e) {
                /* intentionally ignore this exception */
                // This happens because we don't have an Idset for crdt initializations through
                // updates so we got a redundant crdt object(something we already have)
                assert (binary.getObject().getClass() == CRDTEffect.class);
            }
        }

        return JavaObjectId;
    }

    @Override
    public OtpErlangBinary downstream(OtpErlangBinary JavaObjectId, OtpErlangBinary binary, OtpErlangMap clock,
            OtpErlangMap global_clock) {
        VectorClock effectClock = new VectorClock(clock);
        GlobalClockTime = new VectorClock(global_clock);

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
        Snapshot mapentry = ObjectTable.get(JavaObjectId);
        CRDT crdt_object = mapentry.crdt;
        if (crdt_object == null) {
            throw new NoSuchObjectException();
        }

        // Apply any effects that have passed the clock time to the new copy of the
        // object
        TreeSet<GenericEffect> crdt_effect_buffer = mapentry.effectbuffer;
        TreeSet<GenericEffect> new_crdt_effect_buffer = new TreeSet<GenericEffect>();
        for (GenericEffect e : crdt_effect_buffer) {
            // Use a correct compare based on types
            if (e.time.lessthan(this.GlobalClockTime) || (0 == e.time.compareTo(this.GlobalClockTime))) {
                crdt_object.invoke((GenericFunction) e.func);
            } else {
                // Because I can't do concurrent modicifations to the treeset, add to a new one
                // and replace
                new_crdt_effect_buffer.add(e);
            }
        }
        mapentry.effectbuffer = new_crdt_effect_buffer;

        OtpErlangObject[] emptypayload = new OtpErlangObject[2];
        emptypayload[0] = JavaObjectId;
        emptypayload[1] = new OtpErlangBinary(mapentry);
        OtpErlangTuple new_snapshot = new OtpErlangTuple(emptypayload);
        return new_snapshot;
    }

    @Override
    public OtpErlangBinary newJavaObjectId() {
        byte[] b = new byte[20];
        new Random().nextBytes(b);
        return new OtpErlangBinary(b);
    }

    @Override
    public OtpErlangBinary loadSnapshot(OtpErlangBinary JavaObjectId, OtpErlangBinary binary) {
        ObjectTable.put(JavaObjectId, (Snapshot) binary.getObject());
        return JavaObjectId;
    }

    public static void main(String[] args) {
        String nodename;
        if (args.length >= 1) {
            nodename = args[0];
        } else {
            nodename = "JavaNode@127.0.0.1";
        }

        String target;
        if (args.length >= 2) {
            target = args[1];
        } else {
            target = "antidote@127.0.0.1";
        }
        VectorClockBackend backend = new VectorClockBackend(nodename, "javamailbox", "antidote");
        if (backend.check(target)) {
            System.out.println("The antidote client is up and running");
        } else {
            System.out.println("The antidote client is not up");
        }
        backend.run();
    }
}
