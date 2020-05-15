public class GenericEffect extends Effect {
    private static final long serialVersionUID = 11L;
    GenericFunction func;

    public GenericEffect(GenericFunction func, VectorClock time) {
        super(time);
        this.func = func;
    }
}