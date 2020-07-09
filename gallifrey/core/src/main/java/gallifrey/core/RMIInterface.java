package gallifrey.core;

import java.rmi.Remote;
import java.rmi.RemoteException;

import eu.antidotedb.client.GenericKey;

public interface RMIInterface extends Remote {

    public Object rmiOperation(GenericKey k, GenericFunction f) throws RemoteException, BackendRequiresFlushException;

    public Object rmiOperation(GenericKey k, GenericFunction f, VectorClock v) throws RemoteException;

    public VectorClock getCurrentTime(GenericKey k) throws RemoteException;

    // A new object to be managed by the restriction manager
    public void newObject(GenericKey k, String r) throws RemoteException;

    public String readLockRestriction(GenericKey k) throws RemoteException;

    public void readUnlockRestriction(GenericKey k) throws RemoteException;

    public void writeLockRestriction(GenericKey k) throws RemoteException;

    public void writeUnlockRestriction(GenericKey k) throws RemoteException;

    public void setBlockUntilTime(GenericKey k, VectorClock block_time, String new_restriction) throws RemoteException;
}