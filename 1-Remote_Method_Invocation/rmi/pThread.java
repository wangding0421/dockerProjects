package rmi;

import java.net.*;

public class pThread extends Thread {
    private Skeleton skeleton;
    private Socket csocket;

    public pThread (Skeleton s, Socket cs) {
        this.skeleton = s;
        this.csocket = cs;
    }

    public void run() {
        
    }
}
