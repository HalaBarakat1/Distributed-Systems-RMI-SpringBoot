package org.ds.EX3.shared;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IComputeAndNotify extends Remote {
    public void computeAndNotify(int a,int b, IClientCallBack clientCallBack) throws RemoteException;
}
