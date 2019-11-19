package main;

import java.io.*;
import java.net.InetSocketAddress;
import java.security.Key;

import com.ericsson.otp.erlang.OtpErlangBinary;

import eu.antidotedb.client.*;

import com.google.protobuf.ByteString;

/**
 * Frontend
 */
public class Frontend {

    AntidoteClient antidote;
    Bucket bucket;

    public Frontend(String hostname, int port, String bucket) {
        this.antidote = new AntidoteClient(new InetSocketAddress(hostname, port));
        this.bucket = Bucket.bucket(bucket);
    }

    public static ByteString custom_serialization(Object obj) {
        // new OtpErlangBinary
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream(); ObjectOutput out = new ObjectOutputStream(bos)) {
            out.writeObject(obj);
            return ByteString.copyFrom(bos.toByteArray());
        }
    }

    // may need to do some wrapping around the GenericFunction depending on how
    // GenericKey turns out
    public void send(GenericKey k, GenericFunction f) {
        try (InteractiveTransaction tx = antidote.startTransaction()) {
            bucket.update(tx, k.invoke(custom_serialization(f)));
        }
    }

    public void static_send(GenericKey k, GenericFunction f) {
        bucket.update(antidote.noTransaction(), k.invoke(custom_serialization(f)));
    }

    public void send(GenericKey k, CRDT obj) {
        try (InteractiveTransaction tx = antidote.startTransaction()) {
            bucket.update(tx, k.invoke(custom_serialization(obj)));
        }
    }

    public void static_send(GenericKey k, CRDT obj) {
        bucket.update(antidote.noTransaction(), k.invoke(custom_serialization(obj)));
    }

    public Object read(GenericKey k) {
        try (InteractiveTransaction tx = antidote.startTransaction()) {
            return bucket.read(tx, k);
        }
    }

    public Object static_read(GenericKey k) {
        return bucket.read(antidote.noTransaction(), k);
    }

    public static void main(String[] args) {
        Frontend antidote = new Frontend("localhost", 8087, "my_bucket");
        GenericKey key = Key.generic("my_example_counter");
        Counter counter = new Counter(0);
        antidote.send(key, counter);
        GenericFunction func = new GenericFunction("increment", 2);
        antidote.send(key, func);
        antidote.read(key);
        GenericFunction func2 = new GenericFunction("increment", 2);
        antidote.static_send(key, func2);
        antidote.static_read(key);
    }
}