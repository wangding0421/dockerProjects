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
                return "Interface name : " + this.interfaceclass.getName() + "\n"
                + "Connecting to : " + this.address.toString();
            }
        }

        /* If hashCode() is called, then return new hash value */
        if(method.getName().equals("hashCode")) {
            if (method.getReturnType().getName().equals("int") &&
            method.getParameterTypes().length == 0) {
                return this.interfaceclass.hashCode() * 1011 + this.address.hashCode() * 17;
            } else if(method.getReturnType().getName().equals("int") &&
            method.getParameterTypes().length == 1) {
                return args[0].hashCode();
            } else {
                throw new RMIException("hashCode method called incorrectly");
            }
        }

        /* check if two proxy are same. */
        if(method.getName().equals("equals")) {
            if(method.getReturnType().getName().equals("boolean") &&
            method.getParameterTypes().length == 1 &&
            method.getParameterTypes()[0].getName() == "java.lang.Object")  {

                //if(args.length != 1) throw new Error("equals method called incorrectly");
                if(args[0] == null) return false;

                if(!java.lang.reflect.Proxy.isProxyClass(args[0].getClass())) return false;
                //if(!ProxyFactory.isProxyClass(args[0].getClass())) return false;

                MyInvocationHandler otherHandler = (MyInvocationHandler) java.lang.reflect.Proxy.getInvocationHandler(args[0]);

                /* if interface are same and the network are same, then they are equal*/
                if( this.interfaceclass.equals(otherHandler.getinterface()) &&
                this.address.equals(otherHandler.getAddress()) ) {
                    return true;
                }
                else {
                    return false;
                }
            }
        }


        /* Do the real call */
        myObject returnValue = null;
        try {
            Socket connection = new Socket(address.getHostName(), address.getPort());
            ObjectInputStream in = new ObjectInputStream(connection.getInputStream());
            ObjectOutputStream out = new ObjectOutputStream(connection.getOutputStream());

            out.writeObject(method.getName());
            out.writeObject(method.getParameterTypes());
            out.writeObject(method.getReturnType().getName());
            out.writeObject(args);

            returnValue = (myObject)in.readObject();
            connection.close();
        } catch (Exception e) {
            throw new RMIException(e.getCause());
        }

        if(returnValue.getExceptionStatus()) {
            throw (Exception) returnValue.getObject();
        }
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
