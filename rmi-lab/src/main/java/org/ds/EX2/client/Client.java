package org.ds.EX2.client;

import org.ds.EX2.shared.ICalculator;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Client {
    public static void main(String[] args) {
        Registry registry = null;
        try {
            registry = LocateRegistry.getRegistry(1099);
            ICalculator calculator = (ICalculator) registry.lookup("CalculatorSVC");
            System.out.println("Addition: " + calculator.add(10, 5));
            System.out.println("Multiplication: " + calculator.multiply(10, 5));

        } catch (RemoteException e) {
            throw new RuntimeException(e);
        } catch (NotBoundException e) {
            throw new RuntimeException(e);
        }
    }
}
