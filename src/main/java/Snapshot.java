import java.io.Serializable;
import java.util.TreeSet;

public class Snapshot implements Serializable {
    private static final long serialVersionUID = 12L;
    public CRDT object;
    public TreeSet<GenericEffect> effectbuffer;

    public Snapshot(CRDT Object, TreeSet<GenericEffect> sortedEffectSet) {
        this.object = Object;
        this.effectbuffer = sortedEffectSet;
    }
}