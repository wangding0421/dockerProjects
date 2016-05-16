package naming;

public class ReadWriteLock {

    private volatile int readLockHolders = 0;
    private volatile boolean isWriteLocked = false;
    private volatile int pendingWriteRequests = 0;

	/* Read lock blocks while writers are out and if a write request is waiting */
	public synchronized void lockRead() throws InterruptedException {
        while(isWriteLocked || pendingWriteRequests > 0) wait();
        readLockHolders++;
	}

	/* Update reader count and notify blocking threads */
	public synchronized void unlockRead() {
        readLockHolders--;
        notifyAll();
    }

    public synchronized void lockWrite() throws InterruptedException {
        pendingWriteRequests++;
        while(readLockHolders > 0 || isWriteLocked) wait();
        pendingWriteRequests--;
        isWriteLocked = true;
	}

	public synchronized void unlockWrite() throws InterruptedException {
        isWriteLocked = false;
        notifyAll();
	}

	/* Methods to check status and get counts in lock */
    public synchronized boolean isWriteLocked() {
        return isWriteLocked;
    }

    public synchronized boolean isReadLocked() {
        return readLockHolders > 0;
    }

    public synchronized boolean hasWriteRequests() {
        return pendingWriteRequests > 0;
    }

}
