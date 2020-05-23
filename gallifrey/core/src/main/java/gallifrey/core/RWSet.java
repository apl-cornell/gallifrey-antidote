package gallifrey.core;

import java.util.Set;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * Remove Wins Set
 */
public class RWSet<T> implements Serializable {
    private static final long serialVersionUID = 7L;
    private Set<T> addset;
    private Set<T> removeset;


    public final Class<?>[] value = new Class[] { };
    public final Class<?>[] add = {Object.class};
    public final Class<?>[] addSet = new Class[] { Set.class };
    public final Class<?>[] remove = {Object.class};
    public final Class<?>[] removeSet = new Class[] { Set.class };

    public RWSet(Class<T> cls) {
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