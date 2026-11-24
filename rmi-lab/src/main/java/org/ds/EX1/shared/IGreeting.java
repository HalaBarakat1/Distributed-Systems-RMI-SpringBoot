package org.ds.EX1.shared;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IGreeting extends Remote {
    public String sayHello() throws RemoteException;
}
