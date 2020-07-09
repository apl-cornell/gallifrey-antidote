package gallifrey.core;

import java.rmi.Remote;
import java.rmi.RemoteException;

import eu.antidotedb.client.GenericKey;

public interface RMIInterface extends Remote {

    public Object rmiOperation(GenericKey k, GenericFunction f) throws RemoteException, BackendRequiresFlushException;

    public Object rmiOperation(GenericKey k, GenericFunction f, VectorClock v) throws RemoteException;

    public VectorClock getCurrentTime(GenericKey k);

    // A new object to be managed by the restriction manager
    public void newObject(GenericKey k, String r);

    public String readLockRestriction(GenericKey k);

    public void readUnlockRestriction(GenericKey k);

    public void writeLockRestriction(GenericKey k);

    public void writeUnlockRestriction(GenericKey k);

    public void setBlockUntilTime(GenericKey k, VectorClock block_time, String new_restriction);
}