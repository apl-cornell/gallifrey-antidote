package gallifrey.core;

import java.rmi.Remote;
import java.rmi.RemoteException;

import eu.antidotedb.client.GenericKey;

public interface RMIInterface extends Remote {

    public Object rmiOperation(GenericKey k, GenericFunction f) throws RemoteException, BackendRequiresFlushException;

    public Object rmiOperation(GenericKey k, GenericFunction f, VectorClock v) throws RemoteException;

}