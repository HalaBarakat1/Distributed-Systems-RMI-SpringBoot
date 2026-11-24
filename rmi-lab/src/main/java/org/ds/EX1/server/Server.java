package org.ds.EX1.server;

import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Server {
    public static void main(String[] args) {

        System.setProperty("java.security.policy","./ServerPolicy.policy");
        System.setSecurityManager(new RMISecurityManager());
        try {
            Registry registry = LocateRegistry.createRegistry(1099);

            GreetingSVCImp greetingSVCImp = new GreetingSVCImp();

            registry.rebind("GreetingSVC",greetingSVCImp);
            System.out.println("Server Ready");
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }
}
