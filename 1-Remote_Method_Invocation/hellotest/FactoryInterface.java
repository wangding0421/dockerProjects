package hellotest;

import rmi.RMIException;

public interface FactoryInterface{
    public HelloInterface makePingServer(String serverIP) throws RMIException;
}
