package gallifrey.backend;

import java.util.HashMap;
import java.util.Map;

import java.util.concurrent.locks.*;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import eu.antidotedb.client.GenericKey;
import gallifrey.core.VectorClock;

public class BackendRestrictionManager {

    class ManagedRestriction {
        ReentrantReadWriteLock rwl;
        String current_restriction;

        ManagedRestriction(ReentrantReadWriteLock rwl, String current_restriction) {
            this.rwl = rwl;
            this.current_restriction = current_restriction;
        }
    }

    public Map<GenericKey, ManagedRestriction> map = new HashMap<>();

    // RMI by CentralDude
    public void newObject(GenericKey k, String r) {
        assert (!map.containsKey(k));
        map.put(k, new ManagedRestriction(new ReentrantReadWriteLock(), r));
    }

    public String readLockRestriction(GenericKey k) {
        assert (map.containsKey(k));
        map.get(k).rwl.readLock().lock();
        return map.get(k).current_restriction;
    }

    public void readUnlockRestriction(GenericKey k) {
        assert (map.containsKey(k));
        map.get(k).rwl.readLock().unlock();
    }

    // Like readLockRestriction
    void writeLockRestriction(GenericKey k) {
        assert (map.containsKey(k));
        map.get(k).rwl.writeLock().lock();
    }

    void writeUnlockRestriction(GenericKey k) {
        assert (map.containsKey(k));
        map.get(k).rwl.writeLock().unlock();
    }

    // centraldude -> vectorClockBackend -> setBlockUntilTime
    // vectorClockBackend.setBlockUntilTime(VectorClock block_time, String
    // new_restriction);
    // ->
    public void setBlockUntilTime(GenericKey k, VectorClock block_time, String new_restriction,
            VectorClock LastUpdateTime) {
        assert (map.containsKey(k));
        WriteLock l = map.get(k).rwl.writeLock();
        // This should already be held as we are in the process of concluding the transisiton
        assert(l.getHoldCount() > 0);

        // Wait till the backend last update vectorclock gets past the block time
        while (!LastUpdateTime.lessthan(block_time)) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
        }
        ManagedRestriction r = map.get(k);
        r.current_restriction = new_restriction;
        l.unlock();
    }
}
