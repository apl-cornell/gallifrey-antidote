import eu.antidotedb.client.*;

import java.util.List;
import java.util.Random;

import com.google.protobuf.ByteString;

public class SharedObject {

    Frontend frontend;
    GenericKey key;

    // Shared[increment] Counter c = new Counter();
    // ->
    // SharedObject s = new SharedObject(antidote, new Counter());
    // where antidote = new Frontend(ip, port, bucket);
    // which is created somewhere else
    public SharedObject(Frontend frontend, CRDT crdt) {
        this.frontend = frontend;

        Random rd = new Random();
        byte[] random_bytes = new byte[10];
        rd.nextBytes(random_bytes);
        ByteString random_key = ByteString.copyFrom(random_bytes);
        this.key = Key.generic(random_key);

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
        GenericFunction func2 = new GenericFunction(FunctionName, Arguments);
        frontend.static_send(key, func2);
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
/*     public CRDT getObject() {
        value();
        backend.getObject(some_identifier_we_have);
    } */

    // java rmi
/*     public Object dosideeffectfreemethod() {

    } */
}