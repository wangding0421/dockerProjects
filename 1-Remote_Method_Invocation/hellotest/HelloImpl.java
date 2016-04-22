package hellotest;

import rmi.*;

public class HelloImpl implements HelloInterface
{
    @Override
    public String sayHello() throws RMIException
    {
        return "Hello, World";
    }
}
