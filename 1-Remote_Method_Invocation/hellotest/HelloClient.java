package hellotest;

import rmi.*;
import java.net.*;
import java.rmi.server.RemoteServer;

public class HelloClient
{

    public static void main(String[] args)
    {
        System.out.println("Program Started!");
        InetSocketAddress           address;
        boolean                     listening;
        if (args.length != 2) {
            System.out.println("Please provide the Server IP an then the idNumber!");
            return;
        }
        String serverIP = args[0];
        address = new InetSocketAddress(serverIP, 7000);
        FactoryInterface remoteFactory;
        HelloInterface stub;
        // Create the stub.
        try
            {
                remoteFactory = Stub.create(FactoryInterface.class, address);
                stub = remoteFactory.makePingServer(serverIP);

            }
        catch(Throwable t)
            {
                System.out.println(t.toString());
                return;
            }

        for (int i = 0; i < 4; i++){
            try
                {
                    System.out.println(stub.ping(Integer.parseInt(args[1])));
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

}
