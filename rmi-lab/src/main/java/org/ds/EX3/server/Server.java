package org.ds.EX3.server;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Server {
    public static void main(String[] args) throws RemoteException {

        Registry registry = LocateRegistry.createRegistry(1099);
        ComputeAndNotifySVCImp computeAndNotifySVCImp = new ComputeAndNotifySVCImp();
        registry.rebind("ComputeAndNotifySVC", computeAndNotifySVCImp);
        System.out.println("Ready");

    }
}
