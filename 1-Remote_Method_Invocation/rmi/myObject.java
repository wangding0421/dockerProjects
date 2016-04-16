package rmi;

import java.io.Serializable;

public class myObject implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private Object serverObject = null;
    private boolean hasException = false;

    public myObject (Object so, boolean he) {
        this.serverObject = so;
        this.hasException = he;
    }

    public Object getObject () {
        return this.serverObject;
    }

    public boolean getExceptionStatus () {
        return this.hasException;
    }
}
