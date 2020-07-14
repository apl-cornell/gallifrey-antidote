package gallifrey.core;

import java.io.*;
import java.net.InetSocketAddress;

import eu.antidotedb.client.AntidoteClient;
import eu.antidotedb.client.AntidoteException;
import eu.antidotedb.client.Bucket;
import eu.antidotedb.client.GenericKey;
import eu.antidotedb.client.NoTransaction;
import eu.antidotedb.client.TransactionWithReads;
import eu.antidotedb.client.UpdateOp;
import eu.antidotedb.client.AntidoteTransaction;

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

    private static ByteString custom_serialization(Object obj) {
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

    private NoTransaction getNoTx() {
        NoTransaction tx;
        while ((tx = antidote.noTransaction()) == null) {
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                // Cool man
            }
        }
        return tx;
    }

    private void doUpdate(AntidoteTransaction tx, UpdateOp update) {
        try {
            bucket.update(tx, update);
        } catch (AntidoteException e) {
            try {
                Thread.sleep(5);
            } catch (InterruptedException e1) {
                // Cool man
            }
            doUpdate(tx, update);
        }
    }

    private ByteString doRead(TransactionWithReads tx, GenericKey k) {
        try {
            ByteString s;
            if (tx == null) {
                System.out.println("Transaction is null");
            }
            if (k == null) {
                System.out.println("Key is null");
            }
            while ((s = bucket.read(tx, k)) == null) {
                /* Try again */
                /* Strangely this can return null */
                try {
                    Thread.sleep(5);
                } catch (InterruptedException e1) {
                    // Cool man
                }
            }
            return s;
        } catch (AntidoteException e) {
            try {
                Thread.sleep(5);
            } catch (InterruptedException e1) {
                // Cool man
            }
            return doRead(tx, k);
        }
    }

    public void static_send(GenericKey k, GenericFunction f) {
        doUpdate(getNoTx(), k.invoke(custom_serialization(f)));
    }

    public void static_send(GenericKey k, CRDT obj) {
        doUpdate(getNoTx(), k.invoke(custom_serialization(obj)));
    }

    public Object static_read(GenericKey k) {
        return custom_deserialization(doRead(getNoTx(), k));
    }
}
