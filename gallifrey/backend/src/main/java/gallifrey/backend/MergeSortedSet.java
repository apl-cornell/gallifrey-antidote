package gallifrey.backend;

import java.io.Serializable;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.ArrayList;
import java.util.Comparator;

import gallifrey.core.MergeComparator;

// A growth only Set which attempts to use the MergeComparator to order operations when operations are concurrrent
// Implementes Iterable over Set because this is specifically a set for GenericFunctions and it seemed pointless to implement functions we probably wouldn't use.
public class MergeSortedSet implements Iterable<GenericEffect>, Serializable {
    private static final long serialVersionUID = 42L;

    MergeComparator currentMergeStrategy = null;

    TreeSet<GenericEffect> current_group = null;
    ArrayList<TreeSet<GenericEffect>> grouped_by_merge_strategy = new ArrayList<>();

    public MergeSortedSet() {
        current_group = new TreeSet<>();
        grouped_by_merge_strategy.add(current_group);
    }

    public boolean add(GenericEffect e) {
        if (e == null) {
            throw new NullPointerException();
        }

        // I'm assuming that since concensus is required to transition an object that
        // when we change merge strategies we will not need to insert any operations to
        // earlier groups.
        // Compare addresses to avoid NPE then compare content
        if ((currentMergeStrategy != null && !currentMergeStrategy.equals(e.get_merge_strategy())
                || currentMergeStrategy == null && e.get_merge_strategy() != null)) {
            currentMergeStrategy = e.get_merge_strategy();
            current_group = new TreeSet<>((currentMergeStrategy == null ? null : new Comparator<GenericEffect>() {
                @Override
                public int compare(GenericEffect l, GenericEffect r) {
                    // First see if there is a causal ordering, else try the merge strategy. If that
                    // fails, fall back to vectorclock compare
                    if (l.time.lessthan(r.time)) {
                        return -1;
                    } else if (r.time.lessthan(l.time)) {
                        return 1;
                    } else {
                        int user_option = currentMergeStrategy.compare(l.func, r.func);
                        if (user_option == 0) {
                            return l.compareTo(r);
                        } else {
                            return user_option;
                        }
                    }
                }
            }));
            grouped_by_merge_strategy.add(current_group);
        }

        return current_group.add(e);
    }

    public int size() {
        int acc = 0;
        for (TreeSet<GenericEffect> e_set : grouped_by_merge_strategy) {
            acc += e_set.size();
        }
        return acc;
    }

    // Some tests need an arraylist version to compare the elements of two sets
    public ArrayList<GenericEffect> toArrayList() {
        ArrayList<GenericEffect> acc = new ArrayList<>();
        for (GenericEffect e : this) {
            acc.add(e);
        }
        return acc;
    }

    public Iterator<GenericEffect> iterator() {
        Iterator<GenericEffect> it = new Iterator<GenericEffect>() {

            private Iterator<TreeSet<GenericEffect>> group_iterator = grouped_by_merge_strategy.iterator();
            private Iterator<GenericEffect> current_iterator = group_iterator.next().iterator();

            @Override
            public boolean hasNext() {
                return current_iterator.hasNext() || group_iterator.hasNext();
            }

            @Override
            public GenericEffect next() {
                while (!current_iterator.hasNext()) {
                    TreeSet<GenericEffect> tmp = group_iterator.next();
                    current_iterator = tmp.iterator();
                }
                return current_iterator.next();
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
        return it;
    }
}