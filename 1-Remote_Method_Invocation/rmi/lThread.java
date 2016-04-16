package rmi;

import java.io.IOException;
import java.net.*;

public class lThread extends Thread {

    private Skeleton<?> skeleton = null;
    private ServerSocket listen_socket = null;
    private Exception stopException = null;

    public lThread (Skeleton<?> s, ServerSocket ls) {
        this.skeleton = s;
        this.listen_socket = ls;
    }

    public void run() {
        while (this.skeleton.getRunningStatus() && !this.isInterrupted()) {
            try {
                Socket connection = listen_socket.accept();
                pThread process_thread = new pThread(this.skeleton, connection);
                if (skeleton.getRunningStatus()) {
                    process_thread.start();
                }
            }
            catch (IOException e) {
                if (this.skeleton.getRunningStatus() && this.skeleton.listen_error(e)) {}
                else {
                    this.skeleton.setRunningStatus(false);
                    this.interrupt();
                    this.stopException = e;
                }
            }
        }
        this.skeleton.stopped(this.stopException);
    }
}
