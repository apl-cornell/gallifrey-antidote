import eu.antidotedb.client.GenericKey;

import java.util.List;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public class SharedObject {

    Frontend frontend;
    GenericKey key;
    RMIInterface rmiBackend;


    // Shared[increment] Counter c = new Counter();
    // ->
    // SharedObject s = new SharedObject(antidote, new Counter(), "/JavaBackend");
    // where antidote = new Frontend(ip, port, bucket);
    // which is created somewhere else
    // and the backend
    public SharedObject(Frontend frontend, CRDT crdt, String backend) {
        this.frontend = frontend;
        this.key = crdt.key;

        try {
            rmiBackend = (RMIInterface) Naming.lookup(backend);
        } catch (NotBoundException | MalformedURLException | RemoteException e) {
            System.out.println("Something happend when trying to look up the backend");
            e.printStackTrace();
            System.exit(21);
        }

        frontend.static_send(key, crdt);
    }

    // If another instance initializes the object, they can give the key for that object to you so you can use that shared object
    public SharedObject(Frontend frontend, GenericKey key) {
        this.frontend = frontend;
        this.key = key;
    }

    // May need to update java-antidote-client to make GenericKeys serializable?
    // Get key to send to other people
    // Use key as a pointer for shared objects in shared objects?
    public GenericKey getKey(){
        return this.key;
    }

    // c.func(arg1, arg2, ...);
    // ->
    // s.call("func", [arg1, arg2, ...]);
    public void call(String FunctionName, List<Object> Arguments) {
        // Restriction
        GenericFunction func = new GenericFunction(FunctionName, Arguments);
        frontend.static_send(key, func);
    }

    // c.value();
    // ->
    // s.value();
    public Object value(){
        return frontend.static_read(key);
    }

    // doSomething(c);
    // ->
    // doSomething(s.getObject(some_identifier_here));
    // Presumably there is a method here which talks to the backend directly and gets the object
    // java rmi
    public Object dosideeffectfreemethod(String FunctionName, List<Object> Arguments) {
        GenericFunction func = new GenericFunction(FunctionName, Arguments);
        try {
            return rmiBackend.rmiOperation(this.key, func);
        } catch (RemoteException e) {
            e.printStackTrace();
            System.exit(21);
            return null;
        }

    }
}