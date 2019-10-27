package main;

import java.net.InetSocketAddress;
import java.security.Key;

import com.ericsson.otp.erlang.OtpErlangBinary;

import eu.antidotedb.client.*;


/**
 * Frontend
 */
public class Frontend {

    AntidoteClient antidote;
    Bucket bucket;
    public Frontend(String hostname, int port, String bucket){
        this.antidote = new AntidoteClient(new InetSocketAddress(hostname, port));
        this.bucket = Bucket.bucket(bucket);
    }

    public void send(GenericKey k, GenericFunction f){
        try (InteractiveTransaction tx = antidote.startTransaction()) {
            bucket.update(tx, k.invoke(new OtpErlangBinary(f)));
        }
    }

    public int read(GenericKey k){
        try (InteractiveTransaction tx = antidote.startTransaction()) {
            return bucket.read(tx, k);
        }
    }

    public static void main(String[] args) {
        Frontend antidote = new Frontend("localhost", 8087, "mybucket");
        GenericKey k = Key.generic("my_example_counter");
        GenericFunction f = new GenericFunction(1, "increment", 1);
        antidote.send(k, f);
    }
}