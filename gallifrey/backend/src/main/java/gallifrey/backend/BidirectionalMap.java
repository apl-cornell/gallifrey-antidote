package gallifrey.backend;

import java.util.HashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

// Modified from https://stackoverflow.com/questions/9783020/bidirectional-map
public class BidirectionalMap<K, V> extends HashMap<K, V> {
    private static final long serialVersionUID = 666L;
    public HashMap<V, K> inversedMap = new HashMap<V, K>();
    ReadWriteLock update_lock = new ReentrantReadWriteLock();

    private interface AcquiredLock extends AutoCloseable {
        @Override
        public void close();
    };

    private class AcquireReadLock implements AcquiredLock {
        final Lock locked;

        AcquireReadLock() {
            locked = update_lock.readLock();
            locked.lock();
        }

        @Override
        public void close() {
            locked.unlock();
        }
    }

    private class AcquireWriteLock implements AcquiredLock {
        final Lock locked;

        AcquireWriteLock() {
            locked = update_lock.writeLock();
            locked.lock();
        }

        @Override
        public void close() {
            locked.unlock();
        }
    }

    @Override
    public int size() {
        return this.size();
    }

    @Override
    public boolean isEmpty() {
        return this.size() > 0;
    }

    @Override
    public V remove(Object key) {
        if (key == null) {
            throw new RuntimeException("1");
        }
        V val;
        try (AcquiredLock locked = new AcquireWriteLock()) {
            val = super.remove(key);
            inversedMap.remove(val);
        }
        if (val == null) {
            throw new RuntimeException("2");
        }
        return val;
    }

    public V getValue(K key) {
        if (key == null) {
            throw new RuntimeException("3");
        }
        try (AcquiredLock locked = new AcquireReadLock()) {
            V val = super.get(key);

            if (val == null) {
                throw new RuntimeException("4");
            }
            return val;
        }
    }

    public K getKey(V value) {
        if (value == null) {
            throw new RuntimeException("5");
        }
        try (AcquiredLock locked = new AcquireReadLock()) {
            K key = inversedMap.get(value);
            if (key == null) {
                throw new RuntimeException("6");
            }
            return key;
        }
    }

    @Override
    public V put(K key, V value) {
        if (key == null) {
            throw new RuntimeException("7");
        }
        if (value == null) {
            throw new RuntimeException("8");
        }
        try (AcquiredLock locked = new AcquireWriteLock()) {
            /*
             * // Remove old key/value pairs if (super.containsKey(key)) { V val =
             * super.remove(key); inversedMap.remove(val); if (val == null) { throw new
             * RuntimeException("9"); } }
             */

            // Add new ones
            inversedMap.put(value, key);
            V new_val = super.put(key, value);
            return new_val;
        }
    }

}