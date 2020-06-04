package gallifrey.core;

import java.io.*;
import java.net.InetSocketAddress;

import eu.antidotedb.client.*;

import com.google.protobuf.ByteString;

/**
 * Frontend
 */
public class Frontend {

    AntidoteClient antidote;
    Bucket bucket;
    GenericKey LastUpdatedKey;

    public Frontend(String hostname, int port, String bucket) {
        this.antidote = new AntidoteClient(new InetSocketAddress(hostname, port));
        this.bucket = Bucket.bucket(bucket);
    }

    private static ByteString custom_serialization(Object obj) {
        // import com.ericsson.otp.erlang.OtpErlangBinary;
        // new OtpErlangBinary
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream(); ObjectOutput out = new ObjectOutputStream(bos)) {
            out.writeObject(obj);
            return ByteString.copyFrom(bos.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private static Object custom_deserialization(ByteString bs) {
        try (ByteArrayInputStream in = new ByteArrayInputStream(bs.toByteArray());
                ObjectInputStream is = new ObjectInputStream(in)) {
            return is.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public void send(GenericKey k, GenericFunction f) {
        LastUpdatedKey = k;
        try (InteractiveTransaction tx = antidote.startTransaction()) {
            bucket.update(tx, k.invoke(custom_serialization(f)));
            tx.commitTransaction();
        }
    }

    public void static_send(GenericKey k, GenericFunction f) {
        LastUpdatedKey = k;
        bucket.update(antidote.noTransaction(), k.invoke(custom_serialization(f)));
    }

    public void send(GenericKey k, CRDT obj) {
        LastUpdatedKey = k;
        try (InteractiveTransaction tx = antidote.startTransaction()) {
            bucket.update(tx, k.invoke(custom_serialization(obj)));
            tx.commitTransaction();
        }
    }

    public void static_send(GenericKey k, CRDT obj) {
        LastUpdatedKey = k;
        bucket.update(antidote.noTransaction(), k.invoke(custom_serialization(obj)));
    }

    public Object read(GenericKey k) {
        try (InteractiveTransaction tx = antidote.startTransaction()) {
            bucket.read(tx, LastUpdatedKey);
            return custom_deserialization(bucket.read(tx, k));
        }
    }

    public Object static_read(GenericKey k) {
        custom_deserialization(bucket.read(antidote.noTransaction(), LastUpdatedKey));
        return custom_deserialization(bucket.read(antidote.noTransaction(), k));
    }
}
