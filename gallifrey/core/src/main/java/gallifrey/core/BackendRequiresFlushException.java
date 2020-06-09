package gallifrey.core;

import eu.antidotedb.client.GenericKey;

public class BackendRequiresFlushException extends Exception {
    private static final long serialVersionUID = 5L;
    public GenericKey key;
    public VectorClock time;

    public BackendRequiresFlushException(GenericKey key, VectorClock downstreamTime) {
        this.key = key;
        this.time = downstreamTime;
    }
}