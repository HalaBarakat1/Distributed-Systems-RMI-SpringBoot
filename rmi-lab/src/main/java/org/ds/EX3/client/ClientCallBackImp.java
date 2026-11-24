package org.ds.EX3.client;

import org.ds.EX3.shared.IClientCallBack;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class ClientCallBackImp extends UnicastRemoteObject implements IClientCallBack {
    protected ClientCallBackImp() throws RemoteException {
    }

    @Override
    public void notify(String Message) throws RemoteException {
        System.out.println("Received Callback:" +Message);
    }
}
