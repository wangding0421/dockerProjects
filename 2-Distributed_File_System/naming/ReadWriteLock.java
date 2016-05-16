package naming;

public class ReadWriteLock {

    private volatile int readLockHolders = 0;
    private volatile boolean isWriteLocked = false;
    private volatile int pendingWriteRequests = 0;

	public synchronized void lockRead() throws InterruptedException {
        while(isWriteLocked || pendingWriteRequests > 0) wait();
        readLockHolders++;
	}

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

	public synchronized void unlockWrite() {
        isWriteLocked = false;
        notifyAll();
	}

}
