package org.ds.EX1.server;

import org.ds.EX1.shared.IGreeting;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class GreetingSVCImp extends UnicastRemoteObject implements IGreeting {

    protected GreetingSVCImp() throws RemoteException {
        super();
    }

    @Override
    public String sayHello() throws RemoteException {
        System.out.println("Receive New Request");
        return "Hello From RMI Server";
    }
}
