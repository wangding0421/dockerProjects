package naming;

public class ReadWriteLock {

    private volatile int readLockHolders = 0;
    private volatile boolean isWriteLocked = false;
    private volatile int pendingWriteRequests = 0;
    private volatile int requests = 0;

	public synchronized void lockRead() throws InterruptedException {
        while(this.isWriteLocked || this.pendingWriteRequests > 0) wait();
        this.readLockHolders++;
        this.requests++;
	}

	public synchronized void unlockRead() {
        this.readLockHolders--;
        notifyAll();
    }

    public synchronized void lockWrite() throws InterruptedException {
        this.pendingWriteRequests++;
        while(this.readLockHolders > 0 || this.isWriteLocked) wait();
        this.pendingWriteRequests--;
        this.isWriteLocked = true;
	}

	public synchronized void unlockWrite() {
        this.isWriteLocked = false;
        notifyAll();
	}

    public synchronized int getRequests() {
        return this.requests;
    }

}
