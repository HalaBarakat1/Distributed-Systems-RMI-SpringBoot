package org.ds.EX3.shared;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IClientCallBack extends Remote {

    public void notify(String Message) throws RemoteException;
}
