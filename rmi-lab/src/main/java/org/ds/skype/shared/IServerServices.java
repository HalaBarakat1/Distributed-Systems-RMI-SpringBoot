package org.ds.skype.shared;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Services offered by the chat server (implemented by Server_Services_Imp).
 * A copy of this interface exists on both the server and the client.
 *
 * Return codes:  1 = success,  0 = failure (see each method for the exact meaning).
 */
public interface IServerServices extends Remote {

    /** 1 = room created, 0 = a room with this name already exists. Default capacity is 10. */
    int AddChatRoom(String name) throws RemoteException;

    /** 1 = room created, 0 = name already exists or capacity < 1. */
    int AddChatRoom(String name, int capacity) throws RemoteException;

    /** 1 = room removed (its members are notified and signed out), 0 = no such room. */
    int removeChatRoom(String name) throws RemoteException;

    /** 1 = account created, 0 = user name already taken. */
    int signup(String userName, String password, String FName, String LName) throws RemoteException;

    /** 1 = signed in, 0 = wrong user name/password, -1 = no such room, -2 = room is full, -3 = already signed in. */
    int signin(String userName, String password, String roomName) throws RemoteException;

    /** 1 = signed out, 0 = the user is not in this room. */
    int signout(String userName, String roomName) throws RemoteException;

    /** Each entry looks like "General (2/10)". */
    String[] ListAvailableRooms() throws RemoteException;

    /** User names currently signed in to the room (empty if the room does not exist). */
    String[] ListClientsInRoom(String name) throws RemoteException;

    /**
     * Sends m to the user "to" through the server: the server looks up the receiver's
     * listener and calls its ReceiveMessage (callback).
     * 1 = delivered, 0 = the receiver is not in this room.
     */
    int SendMessage(String m, String to, String roomName) throws RemoteException;

    /** Sends m to every member of the room except the sender. Returns how many received it. */
    int SendBroadCatMessage(String userName, String m, String roomName) throws RemoteException;

    /** Registers the client's callback object, so the server can call it later. */
    void AddReceiveListener(IClientListener c) throws RemoteException;

    /** Unregisters the client's callback object. */
    void RemoveReceiveListener(IClientListener c) throws RemoteException;
}
