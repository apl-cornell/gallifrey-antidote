import java.util.Map;
import java.util.HashMap;

/**
 * A boring map that only adds things
 * This is mainly to show objects with methods that take more than one argument
 */
public class GrowthMap<T, S> extends CRDT {
    private static final long serialVersionUID = 4L;
    private Map<T, S> growthMap;

    public Class<?>[] add = new Class[] { Object.class, Object.class };
    public Class<?>[] addMap = new Class[] { Map.class };

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
}