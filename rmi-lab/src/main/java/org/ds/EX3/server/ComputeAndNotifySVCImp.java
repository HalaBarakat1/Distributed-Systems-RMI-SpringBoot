package org.ds.EX3.server;

import org.ds.EX3.shared.IClientCallBack;
import org.ds.EX3.shared.IComputeAndNotify;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class ComputeAndNotifySVCImp extends UnicastRemoteObject implements IComputeAndNotify {
    protected ComputeAndNotifySVCImp() throws RemoteException {
    }

    @Override
    public void computeAndNotify(int a, int b, IClientCallBack clientCallBack) throws RemoteException {
        int res = a+b;
        clientCallBack.notify("Result: " + res);
    }
}
