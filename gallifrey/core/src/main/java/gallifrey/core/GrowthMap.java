package gallifrey.core;

import java.util.Map;
import java.util.HashMap;

import java.io.Serializable;

/**
 * A boring map that only adds things
 * This is mainly to show objects with methods that take more than one argument
 */
public class GrowthMap<T, S> implements Serializable {
    private static final long serialVersionUID = 4L;
    private Map<T, S> growthMap;

    public final Class<?>[] value = new Class[] {};
    public final Class<?>[] add = {Object.class, Object.class};
    public final Class<?>[] addMap = new Class[] { Map.class };
    public final Class<?>[] get = new Class[] { Object.class };
    public final Class<?>[] containsKey = new Class[] { Object.class };

    public GrowthMap() {
        growthMap = new HashMap<T, S>();
    }

    public Map<T, S> value() {
        return growthMap;
    }

    public void add(T key, S value) {
        // We would need to add time stamps so that replics converge
        growthMap.put(key, value);
    }

    public void addMap(Map<T, S> otherMap) {
        growthMap.putAll(otherMap);
    }

    public Object get(T key) {
        return growthMap.get(key);
    }

    public boolean containsKey(T key) {
        return growthMap.containsKey(key);
    }
}