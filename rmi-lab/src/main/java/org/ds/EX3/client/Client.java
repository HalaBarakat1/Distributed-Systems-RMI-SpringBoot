package org.ds.EX3.client;

import org.ds.EX3.shared.IClientCallBack;
import org.ds.EX3.shared.IComputeAndNotify;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Client {
    public static void main(String[] args) throws RemoteException, NotBoundException {
        Registry registry = LocateRegistry.getRegistry(1099);
        IComputeAndNotify iComputeAndNotify = (IComputeAndNotify) registry.lookup("ComputeAndNotifySVC");
        IClientCallBack iClientCallBack = new ClientCallBackImp();
        iComputeAndNotify.computeAndNotify(10,5,iClientCallBack);
    }
}
