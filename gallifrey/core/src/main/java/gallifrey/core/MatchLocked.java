package gallifrey.core;

import java.rmi.RemoteException;

public class MatchLocked implements AutoCloseable {
    private String current_restriction;
    private SharedObject locked_object;

    public String get_restriction_name() {
        return current_restriction;
    }

    @Override
    public void close() {
        try {
            SharedObject.getBackend().readUnlockRestriction(locked_object.key);
        } catch (RemoteException e) {
            e.printStackTrace();
            System.exit(333);
        }
    }

    public MatchLocked(final String current_restriction, final SharedObject locked_object) {
        try {
            this.current_restriction = SharedObject.getBackend().readLockRestriction(locked_object.key);
            this.locked_object = locked_object;
        } catch (RemoteException e) {
            e.printStackTrace();
            System.exit(999);
            this.current_restriction = null;
            this.locked_object = null;
        }
    }
}
