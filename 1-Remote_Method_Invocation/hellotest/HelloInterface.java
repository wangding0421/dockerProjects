package hellotest;

import rmi.RMIException;

public interface HelloInterface
{
    public String sayHello(int idNumber) throws RMIException;
}
