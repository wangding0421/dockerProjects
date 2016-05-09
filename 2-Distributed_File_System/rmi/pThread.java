package rmi;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.net.*;

public class pThread<T> extends Thread {
    private Skeleton<?> skeleton;
    private Socket connection;
    private Class<T> sclass = null;

    public pThread (Skeleton<?> s, Socket cs, Class<T> c) {
        this.skeleton = s;
        this.connection = cs;
        this.sclass = c;
    }

    public void run() {
        try {
            myObject serverMyObject = null;
            ObjectOutputStream out = new ObjectOutputStream(this.connection.getOutputStream());
            out.flush();
            ObjectInputStream in = new ObjectInputStream(this.connection.getInputStream());
            String methodName = (String) in.readObject();

            Class<?>[] parameterTypes = (Class[]) in.readObject();

            Method serverMethod = null;
            try {
                serverMethod = sclass.getMethod(methodName, parameterTypes);
            } catch(Exception e) {
                serverMyObject = new myObject(new RMIException(e.getCause()), true);
            }

            String returnType = (String) in.readObject();

            if (serverMethod != null) {
                try {
                    Object serverObject = serverMethod.invoke(this.skeleton.getServer(), (Object[])in.readObject());
                    serverMyObject = new myObject(serverObject, false);
                } catch (Throwable e) {
                    serverMyObject = new myObject(e.getCause(), true);
                }
            }

            out.writeObject(serverMyObject);
            connection.close();
        } catch (Throwable e) {
            this.skeleton.service_error(new RMIException(e.getCause()));
        }
    }
}
