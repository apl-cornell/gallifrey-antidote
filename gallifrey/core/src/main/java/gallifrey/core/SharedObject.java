package gallifrey.core;

import eu.antidotedb.client.GenericKey;

import java.rmi.Remote;
import java.util.ArrayList;
import java.util.List;

import com.google.protobuf.ByteString;
import sun.net.www.content.text.Generic;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public class SharedObject implements Serializable {
    private static final long serialVersionUID = 17L;
    private static Frontend frontend;
    private static RMIInterface rmiBackend;
    private Snapshot objectSnapshot;
    public GenericKey key;
    public MergeComparator merge_strategy;

    private static Frontend getFrontend() {
        if (frontend == null) {
            String antidote_host = System.getenv("ANTIDOTE_HOST");
            if (antidote_host == null) {
                antidote_host = "localhost";
            }
            String antidote_port_str = System.getenv("ANTIDOTE_PORT");
            Integer antidote_port;
            if (antidote_port_str == null) {
                antidote_port = 8087;
            } else {
                antidote_port = Integer.parseInt(antidote_port_str);
            }
            String antidote_bucket = System.getenv("ANTIDOTE_BUCKET");
            if (antidote_bucket == null) {
                antidote_bucket = "my_bucket";
            }
            frontend = new Frontend(antidote_host, antidote_port, antidote_bucket);
        }
        return frontend;
    }

    private static RMIInterface getBackend() {
        if (rmiBackend == null) {
            try {
                String antidote_backend = System.getenv("ANTIDOTE_BACKEND");
                if (antidote_backend == null) {
                    antidote_backend = "/JavaBackend";
                }
                rmiBackend = (RMIInterface) Naming.lookup(antidote_backend);
            } catch (NotBoundException | MalformedURLException | RemoteException e) {
                System.out.println("Something happend when trying to look up the backend");
                e.printStackTrace();
                System.exit(21);
            }
        }
        return rmiBackend;
    }

    private Object getSnapshot(GenericFunction func, VectorClock downstreamTime) throws BackendRequiresFlushException {
        // get the causal frontier of the current snapshot's effects
        ArrayList<VectorClock> frontier = new ArrayList<VectorClock>();
        if (this.objectSnapshot != null) {
            try (MergeSortedSet.It getit = this.objectSnapshot.effectbuffer.get_iterator()) {
                for (GenericEffect e : getit) {
                    if (frontier.isEmpty()) {
                        // initialize if empty
                        frontier.add(e.time);
                    } else if (frontier.get(0).lessthan(e.time)) {
                        // if we found an event with a greater time than the entire frontier, clear it
                        // and add the new event
                        frontier.clear();
                        frontier.add(e.time);
                    } else if (!e.time.lessthan(frontier.get(0))) {
                        // e.time is concurrent with the entries in the ArrayList, so add it
                        frontier.add(e.time);
                    }
                    // otherwise, it is less than and we ignore it
                }
            }
        }

        // Flushes any waiting operations to the backend
        getFrontend().static_read(key);
        Snapshot snapshot;
        try {
            if (objectSnapshot == null) {
                snapshot = getBackend().rmiOperation(this.key, frontier, null);
            } else if (downstreamTime == null) {
                snapshot = getBackend().rmiOperation(this.key, frontier, objectSnapshot.objectid);
            } else {
                snapshot = getBackend().rmiOperation(this.key, frontier, objectSnapshot.objectid, downstreamTime);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
            System.exit(99);
            return null;
        }
        if (objectSnapshot == null || snapshot.crdt != null) {
            // we overwrite the objectSnapshot with the new CRDT
            this.objectSnapshot = snapshot;
        } else {
            // otherwise, the backend hasn't coordinated and only has new events for us
            for (GenericEffect e : snapshot.effectbuffer.get_iterator()) {
                this.objectSnapshot.effectbuffer.add(e);
            }
        }
        CRDT crdt = this.objectSnapshot.crdt;
        MergeSortedSet effectbuffer = this.objectSnapshot.effectbuffer;
        try (MergeSortedSet.It getit = effectbuffer.get_iterator()) {
            for (GenericEffect e : getit) {
                crdt.invoke((GenericFunction) e.func);
            }
        }
        return crdt.invoke(func);
    }

    // Shared[increment] Counter c = new Counter();
    // ->
    // SharedObject s = new SharedObject(antidote, new Counter(), "/JavaBackend");
    // where antidote = new Frontend(ip, port, bucket);
    // which is created somewhere else
    // and the backend
    public SharedObject(Object SharedObject) {
        CRDT crdt = new CRDT(SharedObject);
        this.key = crdt.key;

        // Tell antidote about this new shared object
        getFrontend().static_send(key, crdt);
    }

    // This is used if you want to specify the value of the key. This could be a way
    // for different instances to get an object without communication as long as
    // they know the SomeHash value. For this case, the SharedObject should be a
    // zero arg constructor version of the object with nothing else done to it. If
    // other people try to initialize the object for SomeHash, iterations after the
    // first will be ignored and this is safe as long as everyone uses the same
    // constructor.
    public SharedObject(Object SharedObject, ByteString SomeHash) {
        CRDT crdt = new CRDT(SharedObject, SomeHash);
        this.key = crdt.key;

        // Tell antidote about this new shared object
        getFrontend().static_send(key, crdt);
    }

    public void change_merge_strategy(MergeComparator merge_strategy) {
        this.merge_strategy = merge_strategy;
    }

    // c.func();
    // ->
    // s.void_call("func");
    public void void_call(String FunctionName) {
        // Restriction
        GenericFunction func = new GenericFunction(FunctionName, merge_strategy);
        getFrontend().static_send(key, func);
    }

    // c.func(arg1, arg2, ...);
    // ->
    // s.void_call("func", [arg1, arg2, ...]);
    public void void_call(String FunctionName, List<Object> Arguments) {
        // Restriction
        GenericFunction func = new GenericFunction(FunctionName, merge_strategy, Arguments);
        getFrontend().static_send(key, func);
    }

    // c.doSomethingSideEffectFree();
    // ->
    // (Object) s.const_call("doSomethingSideEffectFree");
    public Object const_call(String FunctionName) {
        return const_call(FunctionName, new ArrayList<Object>());
    }

    // c.doSomethingSideEffectFree(arg1, arg2, ...);
    // ->
    // (Object) s.const_call("doSomethingSideEffectFree", [arg1, arg2, ...]);
    public Object const_call(String FunctionName, List<Object> Arguments) {
        GenericFunction func = new GenericFunction(FunctionName, merge_strategy, Arguments);
        try {
            return this.getSnapshot(func, null);
        } catch (BackendRequiresFlushException b) {
            try {
                return this.getSnapshot(func, b.time);
            } catch (BackendRequiresFlushException e) {
                // shouldn't happen
                e.printStackTrace();
                System.exit(100);
                return null;
            }
        }
    }

    public MatchLocked get_current_restriction_lock(String current_name) {
        return new MatchLocked(current_name, this);
    }

    public SharedObject transition(String name) {
        // TODO
        return this;
    }

    public void release_current_restriction_lock(MatchLocked ml) {
        // TODO
    }
}
