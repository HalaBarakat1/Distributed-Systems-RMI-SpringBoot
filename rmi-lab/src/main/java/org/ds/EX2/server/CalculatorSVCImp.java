package org.ds.EX2.server;

import org.ds.EX2.shared.ICalculator;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class CalculatorSVCImp extends UnicastRemoteObject implements ICalculator {
    protected CalculatorSVCImp() throws RemoteException {

    }

    @Override
    public int add(int a, int b) throws RemoteException {


        return a+b;
    }

    @Override
    public int multiply(int a, int b) throws RemoteException {
        return a*b;
    }
}
