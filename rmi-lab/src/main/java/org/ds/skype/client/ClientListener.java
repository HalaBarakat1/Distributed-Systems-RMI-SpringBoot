package org.ds.skype.client;

import org.ds.skype.shared.IClientListener;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Vector;

/**
 * The client's own remote object. Extending UnicastRemoteObject exports it, so the
 * server receives a stub that points back to THIS client and can call it (callback).
 */
public class ClientListener extends UnicastRemoteObject implements IClientListener {

    private final String id;
    /** Listeners of everyone in my room, sent by the server - used for peer-to-peer messages. */
    private Vector<IClientListener> peers = new Vector<IClientListener>();

    public ClientListener(String id) throws RemoteException {
        super();
        this.id = id;
    }

    /** Runs on this client's machine, but is invoked remotely by the server or by another client. */
    @Override
    public void ReceiveMessage(String s) throws RemoteException {
        System.out.println();
        System.out.println("<< " + s);
        System.out.print("> ");
        System.out.flush();
    }

    @Override
    public String GetID() throws RemoteException {
        return id;
    }

    @Override
    public synchronized void getListeners(Vector<IClientListener> listeners) throws RemoteException {
        this.peers = new Vector<IClientListener>(listeners);
    }

    /** Finds the listener of the user "dest" in the list the server gave us (null if absent). */
    public synchronized IClientListener getClientListener(String dest) {
        for (IClientListener l : peers) {
            try {
                if (dest.equals(l.GetID())) {
                    return l;
                }
            } catch (RemoteException e) {
                // that peer has gone away; skip it
            }
        }
        return null;
    }

    public synchronized int peerCount() {
        return peers.size();
    }
}
