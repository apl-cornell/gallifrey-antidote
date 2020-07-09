package gallifrey.core;

public class MatchLocked implements AutoCloseable {
    final private String current_restriction;
    final private SharedObject locked_object;

    public String get_restriction_name() {
        return current_restriction;
    }

    @Override
    public void close() {
        locked_object.release_current_restriction_lock(this);
    }

    public MatchLocked(final String current_restriction, final SharedObject locked_object) {
        this.current_restriction = current_restriction;
        this.locked_object = locked_object;
    }
}
