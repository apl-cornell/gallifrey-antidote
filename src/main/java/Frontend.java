import java.io.*;
import java.net.InetSocketAddress;

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
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static Object custom_deserialization(ByteString bs) {
        try (ByteArrayInputStream in = new ByteArrayInputStream(bs.toByteArray());
                ObjectInputStream is = new ObjectInputStream(in)) {
            return is.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    // may need to do some wrapping around the GenericFunction depending on how
    // GenericKey turns out
    public void send(GenericKey k, GenericFunction f) {
        try (InteractiveTransaction tx = antidote.startTransaction()) {
            bucket.update(tx, k.invoke(custom_serialization(f)));
            tx.commitTransaction();
        }
    }

    public void static_send(GenericKey k, GenericFunction f) {
        bucket.update(antidote.noTransaction(), k.invoke(custom_serialization(f)));
    }

    public void send(GenericKey k, CRDT obj) {
        try (InteractiveTransaction tx = antidote.startTransaction()) {
            bucket.update(tx, k.invoke(custom_serialization(obj)));
            tx.commitTransaction();
        }
    }

    public void static_send(GenericKey k, CRDT obj) {
        bucket.update(antidote.noTransaction(), k.invoke(custom_serialization(obj)));
    }

    public Object read(GenericKey k) {
        try (InteractiveTransaction tx = antidote.startTransaction()) {
            return custom_deserialization(bucket.read(tx, k));
        }
    }

    public Object static_read(GenericKey k) {
        return custom_deserialization(bucket.read(antidote.noTransaction(), k));
    }

    public static void main(String[] args) {
        int port;
        String ip;
        String bucket;
        if (args.length >= 1) {
            ip = args[0];
        } else {
            ip = "localhost";
        }
        if (args.length >= 2) {
            port = Integer.parseInt(args[1]);
        } else {
            port = 8087;
        }
        if (args.length >= 3) {
            bucket = args[2];
        } else {
            bucket = "my_bucket";
        }
        Frontend antidote = new Frontend(ip, port, bucket);

        GenericKey key = Key.generic("my_example_counter");
        Counter counter = new Counter(0);
        antidote.static_send(key, counter);
        for (int i = 1; i <= 10; i++) {
            GenericFunction func = new GenericFunction("increment", 2);
            antidote.static_send(key, func);
            System.out.println(antidote.static_read(key));
            GenericFunction func2 = new GenericFunction("decrement", 1);
            antidote.static_send(key, func2);
            System.out.println((Integer) antidote.read(key));
        }
    }
}
