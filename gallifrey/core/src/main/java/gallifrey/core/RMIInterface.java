package gallifrey.core;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import com.ericsson.otp.erlang.OtpErlangBinary;
import eu.antidotedb.client.GenericKey;

public interface RMIInterface extends Remote {

    public Snapshot rmiOperation(GenericKey k, List<VectorClock> frontier, OtpErlangBinary objectid)
            throws RemoteException, BackendRequiresFlushException;

    public Snapshot rmiOperation(GenericKey k, List<VectorClock> frontier, OtpErlangBinary objectid, VectorClock v)
            throws RemoteException;

}