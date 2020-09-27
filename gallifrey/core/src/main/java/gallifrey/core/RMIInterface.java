package gallifrey.core;

import java.rmi.Remote;
import java.rmi.RemoteException;

import eu.antidotedb.client.GenericKey;

public interface RMIInterface extends Remote {

    public Snapshot rmiOperation(GenericKey k) throws RemoteException, BackendRequiresFlushException;

    public Snapshot rmiOperation(GenericKey k, VectorClock v) throws RemoteException;

}