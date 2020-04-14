public class CRDTEffect extends Effect {
    private static final long serialVersionUID = 9L;
    CRDT crdt;

    public CRDTEffect(CRDT crdt, VectorClock time) {
        super(time);
        this.crdt = crdt;
    }
}