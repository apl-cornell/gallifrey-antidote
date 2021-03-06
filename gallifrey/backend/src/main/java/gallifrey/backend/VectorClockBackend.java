package gallifrey.backend;

import java.util.Hashtable;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.locks.*;
import java.util.concurrent.*;

import com.ericsson.otp.erlang.OtpErlangBinary;
import com.ericsson.otp.erlang.OtpErlangTuple;
import com.ericsson.otp.erlang.OtpErlangObject;
import com.ericsson.otp.erlang.OtpErlangMap;

import eu.antidotedb.client.GenericKey;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

import gallifrey.core.BackendRequiresFlushException;
import gallifrey.core.CRDT;
import gallifrey.core.VectorClock;
import gallifrey.core.GenericFunction;

public class VectorClockBackend extends AntidoteBackend {
    private static final long serialVersionUID = 16L;
    ConcurrentMap<OtpErlangBinary, Snapshot> ObjectTable = new ConcurrentHashMap<>();
    ConcurrentMap<OtpErlangBinary, MergeSortedSet> MisssingObjectTable = new ConcurrentHashMap<>();
    BidirectionalMap<GenericKey, OtpErlangBinary> KeyTable = new BidirectionalMap<>();

    VectorClock GlobalClockTime = new VectorClock();
    OtpErlangBinary LastDownstreamJavaId;
    VectorClock LastDownstreamTime = new VectorClock();
    VectorClock LastUpdateTime = new VectorClock();

    public VectorClockBackend(String NodeName, String MailBox, String cookie) throws RemoteException {
        super(NodeName, MailBox, cookie);
    }

    @Override
    public OtpErlangBinary value(OtpErlangBinary JavaObjectId) throws NoSuchObjectException {
        CRDT crdt_object = ObjectTable.get(JavaObjectId).crdt;
        if (crdt_object == null) {
            throw new NoSuchObjectException();
        }
        // So we don't actually want to give antidote the object since it can be pretty
        // big. Instead, we are just going to return true and if someone wants the
        // actual object/some field/part of it they can use the rmiOperation method via
        // SharedObject to get it
        return new OtpErlangBinary(true);
    }

    @Override
    public synchronized OtpErlangBinary update(OtpErlangBinary JavaObjectId, OtpErlangBinary binary)
            throws NoSuchObjectException {
        if (!ObjectTable.containsKey(JavaObjectId)) {
            try {
                CRDTEffect crdt_effect = (CRDTEffect) binary.getObject();
                CRDT crdt_object = crdt_effect.crdt;
                LastUpdateTime.updateClock(crdt_effect.time);

                MergeSortedSet e_set = new MergeSortedSet();

                if (MisssingObjectTable.containsKey(JavaObjectId)) {
                    e_set = MisssingObjectTable.get(JavaObjectId);
                    MisssingObjectTable.remove(JavaObjectId);
                }

                Snapshot mapentry = new Snapshot(crdt_object, e_set);
                ObjectTable.put(JavaObjectId, mapentry);
                KeyTable.put(crdt_object.key, JavaObjectId);
            } catch (ClassCastException e) {
                // We don't have the object and we have been given an update
                // so we need to request the object from antidote and try again
                assert (binary.getObject().getClass() == GenericEffect.class);

                if (MisssingObjectTable.containsKey(JavaObjectId)) {
                    GenericEffect updateEffect = (GenericEffect) binary.getObject();
                    MergeSortedSet e_set = MisssingObjectTable.get(JavaObjectId);
                    e_set.add(updateEffect);
                } else {
                    throw new NoSuchObjectException();

                }
            }
        } else {
            try {
                GenericEffect updateEffect = (GenericEffect) binary.getObject();
                LastUpdateTime.updateClock(updateEffect.time);
                MergeSortedSet sortedEffectSet = ObjectTable.get(JavaObjectId).effectbuffer;
                sortedEffectSet.add(updateEffect);
            } catch (ClassCastException e) {
                /* intentionally ignore this exception */
                // This happens because we don't have an Idset for crdt initializations through
                // updates so we got a redundant crdt object(something we already have)
                assert (binary.getObject().getClass() == CRDTEffect.class);
                CRDTEffect crdt_effect = (CRDTEffect) binary.getObject();
                LastUpdateTime.updateClock(crdt_effect.time);
            }
        }

        return JavaObjectId;
    }

    @Override
    public synchronized OtpErlangBinary downstream(OtpErlangBinary JavaObjectId, OtpErlangBinary binary,
            OtpErlangMap clock, OtpErlangMap global_clock) {
        VectorClock effectClock = new VectorClock(clock);

        GlobalClockTime = new VectorClock(global_clock);

        LastDownstreamJavaId = JavaObjectId;
        LastDownstreamTime.updateClock(effectClock);

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
    public synchronized OtpErlangTuple snapshot(OtpErlangBinary JavaObjectId) throws NoSuchObjectException {
        // assert that object is in table else request it

        OtpErlangBinary newJavaId;
        Snapshot new_snapshot;
        CRDT new_crdt_object;

        Snapshot mapentry = ObjectTable.get(JavaObjectId);
        CRDT crdt_object = mapentry.crdt;
        if (crdt_object == null) {
            throw new NoSuchObjectException();
        }

        new_crdt_object = crdt_object.deepClone();

        // Apply any effects that have passed the clock time to the new copy of the
        // object
        MergeSortedSet crdt_effect_buffer = mapentry.effectbuffer;
        MergeSortedSet new_crdt_effect_buffer = new MergeSortedSet();

        try (MergeSortedSet.It getit = crdt_effect_buffer.get_iterator()) {
            for (GenericEffect e : getit) {
                // Use a correct compare based on types
                if (e.time.lessthan(this.GlobalClockTime) || (0 == e.time.compareTo(this.GlobalClockTime))
                        || this.GlobalClockTime.isEmpty()) {
                    new_crdt_object.invoke((GenericFunction) e.func);
                } else {
                    // Because I can't do concurrent modicifations to the treeset, add to a new one
                    // and replace
                    new_crdt_effect_buffer.add(e);
                }
            }
        }

        newJavaId = newJavaObjectId();
        new_snapshot = new Snapshot(new_crdt_object, new_crdt_effect_buffer);
        ObjectTable.put(newJavaId, new_snapshot);
        KeyTable.put(new_crdt_object.key, newJavaId);

        OtpErlangObject[] emptypayload = new OtpErlangObject[2];
        emptypayload[0] = newJavaId;
        emptypayload[1] = new OtpErlangBinary(new_snapshot);
        OtpErlangTuple new_antidote_snapshot = new OtpErlangTuple(emptypayload);
        return new_antidote_snapshot;
    }

    public Object doRmiOperation(GenericKey key, GenericFunction func) {
        OtpErlangBinary JavaObjectId;
        Snapshot mapentry;
        CRDT crdt_object;
        JavaObjectId = KeyTable.get(key);
        mapentry = ObjectTable.get(JavaObjectId);
        crdt_object = mapentry.crdt;

        // Add effects to a throwaway object to get the value
        CRDT temp_crdt_object = crdt_object.deepClone();
        try (MergeSortedSet.It getit = ObjectTable.get(JavaObjectId).effectbuffer.get_iterator()) {
            for (GenericEffect e : getit) {
                temp_crdt_object.invoke((GenericFunction) e.func);
            }
        }

        return temp_crdt_object.invoke(func);
    }

    @Override
    public Object rmiOperation(GenericKey key, GenericFunction func)
            throws RemoteException, BackendRequiresFlushException {
        VectorClock currentDownstreamTime = LastDownstreamTime;
        boolean sleep_decision = false;

        sleep_decision = LastUpdateTime.lessthan(currentDownstreamTime);

        if (false && sleep_decision) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }

            if (LastUpdateTime.lessthan(currentDownstreamTime)) {
                throw new BackendRequiresFlushException(KeyTable.getKey(LastDownstreamJavaId), LastDownstreamTime);
            }

        }

        return doRmiOperation(key, func);
    }

    @Override
    public Object rmiOperation(GenericKey key, GenericFunction func, VectorClock DownstreamTime)
            throws RemoteException {
        boolean sleep_decision = false;
        sleep_decision = false && LastUpdateTime.lessthan(DownstreamTime);

        while (sleep_decision) {

            sleep_decision = LastUpdateTime.lessthan(DownstreamTime);

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
        }

        return doRmiOperation(key, func);
    }

    @Override
    public OtpErlangBinary newJavaObjectId() {
        byte[] b = new byte[20];
        new Random().nextBytes(b);
        return new OtpErlangBinary(b);
    }

    @Override
    public synchronized OtpErlangBinary loadSnapshot(OtpErlangBinary JavaObjectId, OtpErlangBinary binary) {
        Snapshot s = (Snapshot) binary.getObject();

        if (s == null) {
            MisssingObjectTable.put(JavaObjectId, new MergeSortedSet());
        } else {
            ObjectTable.put(JavaObjectId, s);
            KeyTable.put(s.crdt.key, JavaObjectId);
        }

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
        try {
            System.out.println("For garbage collection, using:");
            for (GarbageCollectorMXBean gcMxBean : ManagementFactory.getGarbageCollectorMXBeans()) {
                System.out.println(gcMxBean.getObjectName());
            }
            VectorClockBackend backend = new VectorClockBackend(nodename, "javamailbox", "antidote");
            LocateRegistry.createRegistry(1099); // 1099 is the default port
            String java_hostname = System.getenv("JAVA_HOSTNAME");
            if (java_hostname == null) {
                java_hostname = "127.0.0.1";
            }
            System.setProperty("java.rmi.server.hostname", java_hostname);
            String antidote_backend = System.getenv("ANTIDOTE_BACKEND");
            if (antidote_backend == null) {
                antidote_backend = "/JavaBackend";
            }
            Naming.rebind(antidote_backend, backend);
            if (backend.check(target)) {
                System.out.println("The antidote client is up and running");
            } else {
                System.out.println("The antidote client is not up");
            }
            backend.run();
        } catch (RemoteException | MalformedURLException e) {
            e.printStackTrace();
            System.exit(101);
        }
    }
}
