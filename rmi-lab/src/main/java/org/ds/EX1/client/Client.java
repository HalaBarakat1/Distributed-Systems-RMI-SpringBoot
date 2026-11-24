package org.ds.EX1.client;

import org.ds.EX1.shared.IGreeting;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Client {
    public static void main(String[] args) {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            IGreeting greeting = (IGreeting) registry.lookup("GreetingSVC");
            System.out.println(greeting.sayHello());
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        } catch (NotBoundException e) {
            throw new RuntimeException(e);
        }
    }
}
