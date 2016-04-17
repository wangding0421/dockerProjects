package rmi;

import java.net.*;
import java.io.*;
import java.lang.reflect.*;

public class MyInvocationHandler implements InvocationHandler, Serializable {

    private InetSocketAddress address;
    private Class interfaceclass;

    public MyInvocationHandler(InetSocketAddress address, Class c) {
		this.address = address;
		this.interfaceclass = c;
	}

	public Object invoke(Object proxy, Method method, Object[] args) throws Exception  {

        /* If toString() is called, then return method name and network address */
		if(method.getName().equals("toString")) {
            if (method.getReturnType().getName().equals("java.lang.String") &&
            method.getParameterTypes().length == 0) {
			    MyInvocationHandler handler = (MyInvocationHandler)java.lang.reflect.Proxy.getInvocationHandler(proxy);
			    return "Interface name : " + handler.getinterface().getName() + "\n"
                + "Connecting to : " + handler.getAddress().toString();
		    } else {
                throw new Error("toString method called incorrectly");
            }
        }

		/* If hashCode() is called, then return new hash value */
		if(method.getName().equals("hashCode")) {
            if (method.getReturnType().getName().equals("int") &&
            method.getParameterTypes().length == 0) {
                MyInvocationHandler handler = (MyInvocationHandler)java.lang.reflect.Proxy.getInvocationHandler(proxy);
                return handler.getinterface().hashCode() * 1011 + handler.getAddress().hashCode() * 17;
            } else {
                throw new Error("hashCode method called incorrectly");
            }
        }

		/* check if two proxy are same. */
		if(method.getName().equals("equals")) {
            if(method.getReturnType().getName().equals("boolean") &&
            method.getParameterTypes().length == 1)  {

                if(args.length != 1) throw new Error("equals method called incorrectly");
                if(args[0] == null) return false;

                MyInvocationHandler handler1 = (MyInvocationHandler)java.lang.reflect.Proxy.getInvocationHandler(proxy);
                MyInvocationHandler handler2 = (MyInvocationHandler) java.lang.reflect.Proxy.getInvocationHandler(args[0]);

                /* if interface are same and the network are same, then they are equal*/
                if( handler1.getinterface().equals(handler2.getinterface()) &&
                    handler1.getAddress().equals(handler2.getAddress()) ) {
                        return true;
                    }
                else
                    return false;
            } else {
                throw new Error("equals method called incorrectly");
            }
        }


        /* Do the real call */

		Socket connection;
		myObject returnValue = null;

		try {
            connection = new Socket(address.getHostName(), address.getPort());
            ObjectInputStream in = new ObjectInputStream(connection.getInputStream());
			ObjectOutputStream out = new ObjectOutputStream(connection.getOutputStream());

			out.writeObject(method.getName());
			out.writeObject(method.getParameterTypes());
			out.writeObject(method.getReturnType().getName());
			out.writeObject(args);

			returnValue = (myObject)in.readObject();
			connection.close();

		} catch (IOException e) {
			throw new RMIException(e.getCause());
		} catch (ClassNotFoundException e) {
			throw new RMIException(e.getCause());
		}

		if(returnValue.getExceptionStatus())
			throw (Exception) returnValue.getObject();
		return returnValue.getObject();
	}


    public InetSocketAddress getAddress() {
        return address;
    }

    public void setAddress(InetSocketAddress addr) {
        address = addr;
    }

    public Class getinterface() {
        return interfaceclass;
    }

    public void setinterface(Class c) {
        interfaceclass = c;
    }

}
