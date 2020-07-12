package gallifrey.core;

import java.rmi.Remote;
import java.rmi.RemoteException;

import eu.antidotedb.client.GenericKey;

public interface CentralDudeInterface extends Remote {
    public void transition(GenericKey key, String restriction) throws RemoteException;
}