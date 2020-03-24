import java.util.Set;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

/**
 * Remove Wins Set
 */
public class RWSet<T> extends CRDT {
    private static final long serialVersionUID = new Random().nextLong();
    private Set<T> addset;
    private Set<T> removeset;

    public Class<?>[] add = new Class[] { Object.class };
    public Class<?>[] addSet = new Class[] { Set.class };
    public Class<?>[] remove = new Class[] { Object.class };
    public Class<?>[] removeSet = new Class[] { Set.class };

    public RWSet() {
        addset = new HashSet<T>();
        removeset = new HashSet<T>();
    }

    public Set<T> value() {
        Set<T> currentset = new HashSet<T>();
        for (T elem : new ArrayList<T>(addset)) {
            if (!removeset.contains(elem)) {
                currentset.add(elem);
            }
        }
        return currentset;
    }

    public void add(T elem) {
        addset.add(elem);
        System.out.println("did add");
    }

    public void addSet(Set<T> elems) {
        for (T elem : new ArrayList<T>(elems)) {
            addset.add(elem);
        }
    }

    public void remove(T elem) {
        removeset.add(elem);
        System.out.println("did remove");
    }

    public void removeSet(Set<T> elems) {
        for (T elem : new ArrayList<T>(elems)) {
            addset.add(elem);
        }
    }
}