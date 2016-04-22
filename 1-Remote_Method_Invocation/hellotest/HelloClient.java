package hellotest;

import rmi.*;
import java.net.*;

public class HelloClient
{

    public static void main(String[] args)
    {
        InetSocketAddress           address;
        boolean                     listening;
        if (args.length == 0) {
            System.out.println("Please provide the Server IP!");
            return;
        }
        String serverIP = args[0];
        address = new InetSocketAddress(serverIP, 7000);
        HelloInterface   stub;

        // Create the stub.
        try
        {
            stub = Stub.create(HelloInterface.class, address);
        }
        catch(Throwable t)
        {
            System.out.println(t.toString());
            return;
        }

        try
        {
            System.out.println(stub.sayHello());
        }
        catch(RMIException e)
        {
            System.out.println(e.toString());
        }
        catch(Throwable t)
        {
            System.out.println(t.toString());
        }

    }

}
