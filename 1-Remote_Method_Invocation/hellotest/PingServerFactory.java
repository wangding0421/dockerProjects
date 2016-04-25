package hellotest;

import rmi.*;
import java.net.*;

public class PingServerFactory implements FactoryInterface
{
    public static void main(String[] args) {
        PingServerFactory factory = new PingServerFactory();
        InetSocketAddress            address;
        Skeleton<FactoryInterface>     skeleton;

        address = new InetSocketAddress(7000);
        skeleton = new Skeleton<FactoryInterface>(FactoryInterface.class, factory, address);

        try {
            skeleton.start();
        } catch (Throwable t) {
            System.out.println("skeleton start failed");
        }
    }

    public HelloInterface makePingServer(String serverIP) throws RMIException
    {
        InetSocketAddress address = new InetSocketAddress(7100);
        HelloImpl server = new HelloImpl();
        Skeleton<HelloInterface> skeleton1 = new Skeleton(HelloInterface.class, server, address);
        skeleton1.start();
        HelloInterface remote_server = Stub.create(HelloInterface.class, skeleton1, serverIP);
        return remote_server;
    }
}
