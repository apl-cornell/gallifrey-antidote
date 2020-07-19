package gallifrey.backend;

import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.*;

// Modified from https://stackoverflow.com/questions/9783020/bidirectional-map
public class BidirectionalMap<K, V> extends ConcurrentHashMap<K, V> {
    private static final long serialVersionUID = 666L;

    public V getValue(K key) {
        return get(key);
    }

    private class GetKeyConsumer implements Consumer<BidirectionalMap.Entry<K, V>> {
        public K key = null;
        public final V value;

        public GetKeyConsumer(final V value) {
            this.value = value;
        }

        @Override
        public void accept(BidirectionalMap.Entry<K, V> e) {
            if (e.getValue().equals(value)) {
                key = e.getKey();
            }
        }
    }

    public K getKey(final V value) {
        GetKeyConsumer f = new GetKeyConsumer(value);
        forEachEntry(0, f);
        return f.key;
    }
}
