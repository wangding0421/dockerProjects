package hellotest;

import rmi.*;
import java.net.*;

public class HelloClient
{

    public static void main(String[] args)
    {
        InetSocketAddress           address;
        boolean                     listening;
        if (args.length != 2) {
            System.out.println("Please provide the Server IP an then the idNumber!");
            return;
        }
        String serverIP = args[0];
        address = new InetSocketAddress(serverIP, 7000);
        HelloInterface   stub;

        // Create the stub.
        for (int i = 0; i < 4; i++){
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
                    System.out.println(stub.sayHello(Integer.parseInt(args[1])));
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
