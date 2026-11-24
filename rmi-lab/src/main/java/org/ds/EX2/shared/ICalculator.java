package org.ds.EX2.shared;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ICalculator extends Remote {
    public int add (int a, int b) throws RemoteException;
    public int multiply (int a, int b) throws RemoteException;
}
