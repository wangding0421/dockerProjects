package hellotest;

import rmi.RMIException;

public interface HelloInterface
{
    public String ping(int idNumber) throws RMIException;
}
