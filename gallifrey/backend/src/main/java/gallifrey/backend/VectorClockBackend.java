package gallifrey.backend;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.TreeSet;
import java.util.Random;
import java.util.concurrent.locks.*;

import com.ericsson.otp.erlang.OtpErlangBinary;
import com.ericsson.otp.erlang.OtpErlangTuple;

import com.ericsson.otp.erlang.OtpErlangObject;
import com.ericsson.otp.erlang.OtpErlangMap;

import eu.antidotedb.client.GenericKey;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

import gallifrey.core.BackendRequiresFlushException;
import gallifrey.core.CRDT;
import gallifrey.core.VectorClock;
import gallifrey.core.GenericFunction;
import gallifrey.core.MergeComparator;

public class VectorClockBackend extends AntidoteBackend {
    private static final long serialVersionUID = 16L;
    Map<OtpErlangBinary, Snapshot> ObjectTable = new Hashtable<>();
    Map<OtpErlangBinary, TreeSet<GenericEffect>> MisssingObjectTable = new Hashtable<>();
    BidirectionalMap<GenericKey, OtpErlangBinary> KeyTable = new BidirectionalMap<>();

    VectorClock GlobalClockTime = new VectorClock();
    OtpErlangBinary LastDownstreamJavaId;
    VectorClock LastDownstreamTime = new VectorClock();
    VectorClock LastUpdateTime = new VectorClock();

    ReadWriteLock update_lock = new ReentrantReadWriteLock();

    private interface AcquiredLock extends AutoCloseable {
        @Override
        public void close();
    };

    private class AcquireReadLock implements AcquiredLock {
        final Lock locked;

        AcquireReadLock() {
            locked = update_lock.readLock();
            locked.lock();
        }

        @Override
        public void close() {
            locked.unlock();
        }
    }

    private class AcquireWriteLock implements AcquiredLock {
        final Lock locked;

        AcquireWriteLock() {
            locked = update_lock.writeLock();
            locked.lock();
        }

        @Override
        public void close() {
            locked.unlock();
        }
    }

    public VectorClockBackend(String NodeName, String MailBox, String cookie) throws RemoteException {
        super(NodeName, MailBox, cookie);
    }

    @Override
    public OtpErlangBinary value(OtpErlangBinary JavaObjectId) throws NoSuchObjectException {
        try (AcquiredLock locked = new AcquireReadLock()) {
            CRDT crdt_object = ObjectTable.get(JavaObjectId).crdt;
            if (crdt_object == null) {
                throw new NoSuchObjectException();
            }
        }
        // So we don't actually want to give antidote the object since it can be pretty
        // big. Instead, we are just going to return true and if someone wants the
        // actual object/some field/part of it they can use the rmiOperation method via
        // SharedObject to get it
        return new OtpErlangBinary(true);
    }

    @Override
    public OtpErlangBinary update(OtpErlangBinary JavaObjectId, OtpErlangBinary binary) throws NoSuchObjectException {
        if (!ObjectTable.containsKey(JavaObjectId)) {
            try (AcquiredLock locked = new AcquireWriteLock()) {
                CRDTEffect crdt_effect = (CRDTEffect) binary.getObject();
                CRDT crdt_object = crdt_effect.crdt;
                LastUpdateTime.updateClock(crdt_effect.time);

                TreeSet<GenericEffect> e_set = new TreeSet<GenericEffect>();

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

                try (AcquiredLock locked = new AcquireWriteLock()) {
                    if (MisssingObjectTable.containsKey(JavaObjectId)) {
                        GenericEffect updateEffect = (GenericEffect) binary.getObject();
                        TreeSet<GenericEffect> e_set = MisssingObjectTable.get(JavaObjectId);
                        e_set.add(updateEffect);
                    } else {
                        throw new NoSuchObjectException();
                    }
                }
            }
        } else {
            try (AcquiredLock locked = new AcquireWriteLock()) {
                GenericEffect updateEffect = (GenericEffect) binary.getObject();
                LastUpdateTime.updateClock(updateEffect.time);
                TreeSet<GenericEffect> sortedEffectSet = ObjectTable.get(JavaObjectId).effectbuffer;
                sortedEffectSet.add(updateEffect);
            } catch (ClassCastException e) {
                /* intentionally ignore this exception */
                // This happens because we don't have an Idset for crdt initializations through
                // updates so we got a redundant crdt object(something we already have)
                assert (binary.getObject().getClass() == CRDTEffect.class);
                CRDTEffect crdt_effect = (CRDTEffect) binary.getObject();
                try (AcquiredLock locked = new AcquireWriteLock()) {
                    LastUpdateTime.updateClock(crdt_effect.time);
                }
            }
        }

        return JavaObjectId;
    }

    @Override
    public OtpErlangBinary downstream(OtpErlangBinary JavaObjectId, OtpErlangBinary binary, OtpErlangMap clock,
            OtpErlangMap global_clock) {
        VectorClock effectClock = new VectorClock(clock);
        try (AcquiredLock locked = new AcquireWriteLock()) {
            GlobalClockTime = new VectorClock(global_clock);

            LastDownstreamJavaId = JavaObjectId;
            LastDownstreamTime.updateClock(effectClock);
        }

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

        OtpErlangBinary newJavaId;
        Snapshot new_snapshot;
        CRDT new_crdt_object;
        try (AcquiredLock locked = new AcquireReadLock()) {
            Snapshot mapentry = ObjectTable.get(JavaObjectId);
            CRDT crdt_object = mapentry.crdt;
            if (crdt_object == null) {
                throw new NoSuchObjectException();
            }

            new_crdt_object = crdt_object.deepClone();

            // Apply any effects that have passed the clock time to the new copy of the
            // object
            TreeSet<GenericEffect> crdt_effect_buffer = mapentry.effectbuffer;
            TreeSet<GenericEffect> new_crdt_effect_buffer = new TreeSet<GenericEffect>();
            HashMap<MergeComparator, ArrayList<GenericEffect>> grouped_by_merge_strategy = new HashMap<>();
            ArrayList<MergeComparator> strategy_order = new ArrayList<>();
            {
                for (GenericEffect e : crdt_effect_buffer) {
                    if (e.time.lessthan(this.GlobalClockTime) || (0 == e.time.compareTo(this.GlobalClockTime))
                            || this.GlobalClockTime.isEmpty()) {
                        MergeComparator merge_strategy = e.get_merge_strategy();
                        if (!grouped_by_merge_strategy.containsKey(merge_strategy)) {
                            grouped_by_merge_strategy.put(merge_strategy, new ArrayList<GenericEffect>());
                            strategy_order.add(merge_strategy);
                        }
                        grouped_by_merge_strategy.get(merge_strategy).add(e);
                    } else {
                        // Because I can't do concurrent modicifations to the treeset, add to a new one
                        // and replace
                        new_crdt_effect_buffer.add(e);
                    }
                }
            }
            for (MergeComparator key : strategy_order) {
                ArrayList<GenericEffect> single_merge_strat = grouped_by_merge_strategy.get(key);
                Comparator<GenericEffect> effect_comparator = new Comparator<GenericEffect>() {
                    @Override
                    public int compare(GenericEffect l, GenericEffect r) {
                        return key.compare(l.func, r.func);
                    }
                };
                single_merge_strat.sort(effect_comparator);
                for (GenericEffect e : single_merge_strat) {
                    new_crdt_object.invoke((GenericFunction) e.func);
                }
            }

            newJavaId = newJavaObjectId();
            new_snapshot = new Snapshot(new_crdt_object, new_crdt_effect_buffer);
        }

        try (AcquiredLock locked = new AcquireWriteLock()) {
            ObjectTable.put(newJavaId, new_snapshot);
            KeyTable.put(new_crdt_object.key, newJavaId);
        }

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
        try (AcquiredLock locked = new AcquireReadLock()) {
            JavaObjectId = KeyTable.get(key);
            mapentry = ObjectTable.get(JavaObjectId);
            crdt_object = mapentry.crdt;
        }

        // Add effects to a throwaway object to get the value
        CRDT temp_crdt_object = crdt_object.deepClone();
        for (GenericEffect e : ObjectTable.get(JavaObjectId).effectbuffer) {
            temp_crdt_object.invoke((GenericFunction) e.func);
        }

        return temp_crdt_object.invoke(func);
    }

    @Override
    public Object rmiOperation(GenericKey key, GenericFunction func)
            throws RemoteException, BackendRequiresFlushException {
        VectorClock currentDownstreamTime = LastDownstreamTime;
        boolean sleep_decision = false;
        try (AcquiredLock locked = new AcquireReadLock()) {
            sleep_decision = LastUpdateTime.lessthan(currentDownstreamTime);
        }
        if (sleep_decision) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
            try (AcquiredLock locked = new AcquireReadLock()) {
                if (LastUpdateTime.lessthan(currentDownstreamTime)) {
                    throw new BackendRequiresFlushException(KeyTable.getKey(LastDownstreamJavaId), LastDownstreamTime);
                }
            }
        }

        return doRmiOperation(key, func);
    }

    @Override
    public Object rmiOperation(GenericKey key, GenericFunction func, VectorClock DownstreamTime)
            throws RemoteException {
        boolean sleep_decision = false;
        try (AcquiredLock locked = new AcquireReadLock()) {
            sleep_decision = LastUpdateTime.lessthan(DownstreamTime);
        }
        while (sleep_decision) {
            try (AcquiredLock locked = new AcquireReadLock()) {
                sleep_decision = LastUpdateTime.lessthan(DownstreamTime);
            }
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
    public OtpErlangBinary loadSnapshot(OtpErlangBinary JavaObjectId, OtpErlangBinary binary) {
        Snapshot s = (Snapshot) binary.getObject();
        try (AcquiredLock locked = new AcquireWriteLock()) {
            if (s == null) {
                MisssingObjectTable.put(JavaObjectId, new TreeSet<>());
            } else {
                ObjectTable.put(JavaObjectId, s);
                KeyTable.put(s.crdt.key, JavaObjectId);
            }
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
