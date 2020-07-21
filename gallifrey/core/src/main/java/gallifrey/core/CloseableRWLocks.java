package gallifrey.core;

import java.util.concurrent.locks.*;
import java.io.*;

public class CloseableRWLocks implements Serializable {

    ReadWriteLock update_lock = new ReentrantReadWriteLock(true);

    public interface AcquiredLock extends AutoCloseable {

        public AcquiredLock lock();

        @Override
        public void close();

    };

    private class AcquireReadLock implements AcquiredLock, Serializable {
        Lock locked;

        AcquireReadLock() {
            locked = update_lock.readLock();
        }

        public AcquiredLock lock() {
            locked = update_lock.readLock();
            locked.lock();
            return this;
        }

        @Override
        public void close() {
            locked.unlock();
        }
    }

    private class AcquireWriteLock implements AcquiredLock, Serializable {
        Lock locked;

        AcquireWriteLock() {
            locked = update_lock.writeLock();
        }

        public AcquiredLock lock() {
            locked = update_lock.writeLock();
            locked.lock();
            return this;
        }

        @Override
        public void close() {
            locked.unlock();
        }

    }

    AcquiredLock write_lock = new AcquireWriteLock();
    AcquiredLock read_lock = new AcquireReadLock();

    public AcquiredLock write_lock() {
        return write_lock.lock();
    }

    public AcquiredLock read_lock() {
        return read_lock.lock();
    }

}
