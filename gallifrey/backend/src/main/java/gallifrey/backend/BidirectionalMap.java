package gallifrey.backend;

import java.util.HashMap;

// Modified from https://stackoverflow.com/questions/9783020/bidirectional-map
public class BidirectionalMap<K, V> extends HashMap<K, V> {
    private static final long serialVersionUID = 666L;
    public HashMap<V, K> inversedMap = new HashMap<V, K>();

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
        V val = super.remove(key);
        inversedMap.remove(val);
        return val;
    }

    public V getValue(K key) {
        return super.get(key);
    }

    public K getKey(V value) {
        return inversedMap.get(value);
    }

    @Override
    public V put(K key, V value) {
        // Remove old key/value pairs
        V val = super.remove(key);
        inversedMap.remove(val);

        // Add new ones
        inversedMap.put(value, key);
        return super.put(key, value);
    }

}