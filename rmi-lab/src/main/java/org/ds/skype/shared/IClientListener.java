package org.ds.skype.shared;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Vector;

/**
 * Implemented by the CLIENT (class ClientListener). This is what makes the client a
 * remote object too: the server holds a reference (stub) to it and calls these
 * methods back on the client - the "callback".
 */
public interface IClientListener extends Remote {

    /** Called by the server (or by another client in peer-to-peer mode) to deliver a message. */
    void ReceiveMessage(String s) throws RemoteException;

    /** The user name of the client that owns this listener. */
    String GetID() throws RemoteException;

    /** Called by the server to give the client the listeners of everyone in its room (peer-to-peer). */
    void getListeners(Vector<IClientListener> listeners) throws RemoteException;
}
