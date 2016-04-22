package hellotest;

import rmi.*;

public class HelloImpl implements HelloInterface
{
    @Override
    public String sayHello(int idNumber) throws RMIException
    {
        return "Pong " + idNumber;
    }
}
