package gallifrey.core;

import eu.antidotedb.client.GenericKey;

import java.util.ArrayList;
import java.util.List;

import com.google.protobuf.ByteString;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public class SharedObject implements Serializable {
    private static final long serialVersionUID = 17L;
    private static Frontend frontend;
    private static RMIInterface rmiBackend;
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
        // Flushes any waithing operations to the backend
        return const_call(FunctionName, new ArrayList<Object>());
    }

    // c.doSomethingSideEffectFree(arg1, arg2, ...);
    // ->
    // (Object) s.const_call("doSomethingSideEffectFree", [arg1, arg2, ...]);
    public Object const_call(String FunctionName, List<Object> Arguments) {
        // Flushes any waithing operations to the backend
        getFrontend().static_read(key);
        GenericFunction func = new GenericFunction(FunctionName, merge_strategy, Arguments);
        try {
            return getBackend().rmiOperation(this.key, func);
        } catch (BackendRequiresFlushException b) {
            getFrontend().static_read(b.key);
            try {
                return getBackend().rmiOperation(this.key, func, b.time);
            } catch (RemoteException e) {
                e.printStackTrace();
                System.exit(99);
                return null;
            }
        } catch (RemoteException e) {
            e.printStackTrace();
            System.exit(22);
            return null;
        }
    }

    public MatchLocked get_current_restriction_lock(String current_name) {
        return new MatchLocked(current_name, this);
    }

    public SharedObject transition(String name) {
        return this;
    }

    public void release_current_restriction_lock(MatchLocked ml) {

    }
}
