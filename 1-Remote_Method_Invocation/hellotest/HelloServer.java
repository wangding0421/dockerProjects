package hellotest;

import rmi.*;
import java.net.*;

public class HelloServer
{
    public static void main(String[] args) {
        HelloImpl                    server;
        InetSocketAddress            address;
        Skeleton<HelloInterface>     skeleton;
        boolean                      stopped;

        address = new InetSocketAddress(7000);
        server = new HelloImpl();
        skeleton = new Skeleton<HelloInterface>(HelloInterface.class, server, address);

        try {
            skeleton.start();
        } catch (Throwable t) {
            System.out.println("skeleton start failed");
        }
    }

}
