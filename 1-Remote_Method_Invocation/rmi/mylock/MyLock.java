package rmi.mylock;

public class MyLock {
    private Integer stopSign;
    //0 -> Running
    //1 -> Stopped
    //2 -> Stop request sent, waiting for stopping

    public MyLock () {
        this.stopSign = 1;
    }

    public MyLock (Integer ss) {
        this.stopSign = ss;
    }

    public void setStopSign(Integer s){
        this.stopSign = s;
    }

    public Integer getStopSign() {
        return this.stopSign;
    }
}

