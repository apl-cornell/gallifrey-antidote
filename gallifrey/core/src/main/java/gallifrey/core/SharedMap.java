package gallifrey.core;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Shared Map
 */
public class SharedMap<K, V> implements Serializable {
    private static final long serialVersionUID = 14L;
    private HashMap<K, V> map;


    public final Class<?>[] value = new Class[] { };
    public final Class<?>[] put = new Class[] { Object.class, Object.class};
    public final Class<?>[] get = new Class[] { Object.class };
    public final Class<?>[] containsKey = new Class[] { Object.class };

    public SharedMap() {
        this.map = new HashMap<K, V>();
    }

    public SharedMap(int init_capacity) {
        this.map = new HashMap<K, V>(init_capacity);
    }

    public SharedMap(Map<K, V> map) {
        this.map = new HashMap<K, V>(map);
    }

    public HashMap<K, V> value() {
        return map;
    }

    public void put(K key, V value) {
        map.put(key, value);
    }

    public Object get(K key) {
        return map.get(key);
    }

    public boolean containsKey(K key) {
        return map.containsKey(key);
    }
}