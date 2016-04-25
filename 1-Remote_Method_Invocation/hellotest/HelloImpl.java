package hellotest;

import rmi.*;

public class HelloImpl implements HelloInterface
{
    public String ping(int idNumber) throws RMIException
    {
        return "Pong " + idNumber;
    }
}
