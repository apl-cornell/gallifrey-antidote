package gallifrey.frontend;

import org.junit.Test;

import eu.antidotedb.client.GenericKey;
import eu.antidotedb.client.Key;

import java.util.Random;

import com.google.protobuf.ByteString;

import gallifrey.core.CRDT;
import gallifrey.core.Counter;
import gallifrey.core.Frontend;
import gallifrey.core.GenericFunction;

public class FrontendTest {
    @Test
    // Might be a silly test since the frontend can not get the value of the object
    // anymore
    // FrontendTesting
    public void test() {
        int port = 8087;
        String ip = "localhost";
        String bucket = "my_bucket";

        Frontend antidote = new Frontend(ip, port, bucket);

        Random rd = new Random();
        byte[] random_bytes = new byte[10];
        rd.nextBytes(random_bytes);
        ByteString random_key = ByteString.copyFrom(random_bytes);

        GenericKey key = Key.generic(random_key);
        Counter counter = new Counter(0);
        CRDT crdt = new CRDT(counter);
        antidote.static_send(key, crdt);
        for (int i = 1; i <= 10; i++) {
            GenericFunction func = new GenericFunction("increment", 2);
            antidote.static_send(key, func);
            GenericFunction func2 = new GenericFunction("decrement", 1);
            antidote.static_send(key, func2);
            antidote.static_read(key);
        }
    }
}