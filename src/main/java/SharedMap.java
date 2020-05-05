import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class SharedMap<K, V> extends CRDT{
    private static final long serialVersionUID = 14L;
    private HashMap<K, V> map;

    public SharedMap(){
        this.map = new HashMap<K, V>();
    }

    public SharedMap(int init_capacity) {
        this.map = new HashMap<K, V>(init_capacity);
    }

    public SharedMap(Map<K,V> map) {
        this.map = new HashMap<K, V>(map);
    }

    public HashMap<K, V> value(){
        return map;
    }

    public void put(K key, V value){
        map.put(key, value);
    }

    // You would want some get(K key), containsKey(K key) functions. Targets for rmi
}