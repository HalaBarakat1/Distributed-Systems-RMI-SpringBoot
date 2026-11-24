package org.ds.skype.server;

import org.ds.skype.shared.IClientListener;
import org.ds.skype.shared.IServerServices;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

/**
 * The chat server's remote object. RMI runs each client call on its own thread,
 * so every public method is synchronized to keep the maps consistent.
 */
public class Server_Services_Imp extends UnicastRemoteObject implements IServerServices {

    private static final int DEFAULT_CAPACITY = 10;

    /** A registered account. */
    private static class Account {
        final String password;
        final String firstName;
        final String lastName;

        Account(String password, String firstName, String lastName) {
            this.password = password;
            this.firstName = firstName;
            this.lastName = lastName;
        }
    }

    /** A chat room and the user names signed in to it. */
    private static class ChatRoom {
        final String name;
        final int capacity;
        final Set<String> members = new LinkedHashSet<String>();

        ChatRoom(String name, int capacity) {
            this.name = name;
            this.capacity = capacity;
        }
    }

    private final Map<String, Account> accounts = new HashMap<String, Account>();
    private final Map<String, ChatRoom> rooms = new LinkedHashMap<String, ChatRoom>();
    /** user name -> room the user is signed in to */
    private final Map<String, String> userRoom = new HashMap<String, String>();
    /** user name -> the client's callback object (a stub pointing back to the client) */
    private final Map<String, IClientListener> listeners = new HashMap<String, IClientListener>();

    public Server_Services_Imp() throws RemoteException {
        super();
    }

    // ------------------------------------------------------------------ rooms

    @Override
    public synchronized int AddChatRoom(String name) throws RemoteException {
        return AddChatRoom(name, DEFAULT_CAPACITY);
    }

    @Override
    public synchronized int AddChatRoom(String name, int capacity) throws RemoteException {
        if (name == null || name.trim().isEmpty() || capacity < 1 || rooms.containsKey(name)) {
            log("AddChatRoom(" + name + ", " + capacity + ") -> 0");
            return 0;
        }
        rooms.put(name, new ChatRoom(name, capacity));
        log("AddChatRoom(" + name + ", " + capacity + ") -> 1");
        return 1;
    }

    @Override
    public synchronized int removeChatRoom(String name) throws RemoteException {
        ChatRoom room = rooms.remove(name);
        if (room == null) {
            log("removeChatRoom(" + name + ") -> 0");
            return 0;
        }
        // Tell every member (callback) and sign them out.
        for (String member : new ArrayList<String>(room.members)) {
            IClientListener l = listeners.remove(member);
            userRoom.remove(member);
            if (l != null) {
                try {
                    l.ReceiveMessage("*** Room '" + name + "' was removed. You have been signed out.");
                    l.getListeners(new Vector<IClientListener>());
                } catch (RemoteException e) {
                    log("could not reach " + member + " (" + e.getClass().getSimpleName() + ")");
                }
            }
        }
        log("removeChatRoom(" + name + ") -> 1");
        return 1;
    }

    @Override
    public synchronized String[] ListAvailableRooms() throws RemoteException {
        List<String> result = new ArrayList<String>();
        for (ChatRoom room : rooms.values()) {
            result.add(room.name + " (" + room.members.size() + "/" + room.capacity + ")");
        }
        return result.toArray(new String[0]);
    }

    @Override
    public synchronized String[] ListClientsInRoom(String name) throws RemoteException {
        ChatRoom room = rooms.get(name);
        if (room == null) {
            return new String[0];
        }
        return room.members.toArray(new String[0]);
    }

    // --------------------------------------------------------------- accounts

    @Override
    public synchronized int signup(String userName, String password, String FName, String LName) throws RemoteException {
        if (userName == null || userName.trim().isEmpty() || accounts.containsKey(userName)) {
            log("signup(" + userName + ") -> 0");
            return 0;
        }
        accounts.put(userName, new Account(password, FName, LName));
        log("signup(" + userName + ", " + FName + " " + LName + ") -> 1");
        return 1;
    }

    @Override
    public synchronized int signin(String userName, String password, String roomName) throws RemoteException {
        int result;
        Account account = accounts.get(userName);
        ChatRoom room = rooms.get(roomName);
        if (account == null || !account.password.equals(password)) {
            result = 0;
        } else if (room == null) {
            result = -1;
        } else if (userRoom.containsKey(userName)) {
            result = -3;
        } else if (room.members.size() >= room.capacity) {
            result = -2;
        } else {
            room.members.add(userName);
            userRoom.put(userName, roomName);
            result = 1;
        }
        log("signin(" + userName + ", " + roomName + ") -> " + result);
        return result;
    }

    @Override
    public synchronized int signout(String userName, String roomName) throws RemoteException {
        ChatRoom room = rooms.get(roomName);
        if (room == null || !room.members.contains(userName)) {
            log("signout(" + userName + ", " + roomName + ") -> 0");
            return 0;
        }
        removeMember(userName);
        log("signout(" + userName + ", " + roomName + ") -> 1");
        notifyRoom(room, userName, "*** " + userName + " left the room");
        pushListenersToRoom(room);
        return 1;
    }

    // --------------------------------------------------------------- messages

    @Override
    public synchronized int SendMessage(String m, String to, String roomName) throws RemoteException {
        ChatRoom room = rooms.get(roomName);
        int result = 0;
        if (room != null && room.members.contains(to) && deliver(to, m)) {
            result = 1;
        }
        log("SendMessage(to=" + to + ", room=" + roomName + ") -> " + result);
        return result;
    }

    @Override
    public synchronized int SendBroadCatMessage(String userName, String m, String roomName) throws RemoteException {
        ChatRoom room = rooms.get(roomName);
        if (room == null || !room.members.contains(userName)) {
            log("SendBroadCatMessage(from=" + userName + ", room=" + roomName + ") -> 0");
            return 0;
        }
        int count = 0;
        for (String member : new ArrayList<String>(room.members)) {
            if (!member.equals(userName) && deliver(member, "[" + roomName + "] " + userName + ": " + m)) {
                count++;
            }
        }
        log("SendBroadCatMessage(from=" + userName + ", room=" + roomName + ") -> " + count);
        return count;
    }

    // -------------------------------------------------------------- callbacks

    @Override
    public synchronized void AddReceiveListener(IClientListener c) throws RemoteException {
        String id = c.GetID(); // a remote call back to the client
        listeners.put(id, c);
        log("AddReceiveListener(" + id + ") - listeners: " + listeners.keySet());

        String roomName = userRoom.get(id);
        ChatRoom room = roomName == null ? null : rooms.get(roomName);
        if (room != null) {
            notifyRoom(room, id, "*** " + id + " joined the room");
            pushListenersToRoom(room); // peer-to-peer: everyone gets the new list
        }
    }

    @Override
    public synchronized void RemoveReceiveListener(IClientListener c) throws RemoteException {
        String id = c.GetID();
        listeners.remove(id);
        log("RemoveReceiveListener(" + id + ")");
    }

    // ---------------------------------------------------------------- helpers

    /** Callback: calls ReceiveMessage on the receiver's own machine. Drops clients that cannot be reached. */
    private boolean deliver(String userName, String message) {
        IClientListener l = listeners.get(userName);
        if (l == null) {
            return false;
        }
        try {
            l.ReceiveMessage(message);
            return true;
        } catch (RemoteException e) {
            log("client " + userName + " is unreachable, removing it (" + e.getClass().getSimpleName() + ")");
            String roomName = userRoom.get(userName);
            removeMember(userName);
            ChatRoom room = roomName == null ? null : rooms.get(roomName);
            if (room != null) {
                pushListenersToRoom(room);
            }
            return false;
        }
    }

    /** Sends a notice to every member of the room except one. */
    private void notifyRoom(ChatRoom room, String except, String notice) {
        for (String member : new ArrayList<String>(room.members)) {
            if (!member.equals(except)) {
                deliver(member, notice);
            }
        }
    }

    /**
     * Peer-to-peer part: gives every member of the room the listeners (stubs) of all
     * members, by calling getListeners on each client. After this, clients can call
     * each other's ReceiveMessage directly, without the server.
     */
    private void pushListenersToRoom(ChatRoom room) {
        Vector<IClientListener> roomListeners = new Vector<IClientListener>();
        for (String member : room.members) {
            IClientListener l = listeners.get(member);
            if (l != null) {
                roomListeners.add(l);
            }
        }
        for (IClientListener l : roomListeners) {
            try {
                l.getListeners(roomListeners);
            } catch (RemoteException e) {
                log("could not send the listener list (" + e.getClass().getSimpleName() + ")");
            }
        }
    }

    private void removeMember(String userName) {
        String roomName = userRoom.remove(userName);
        listeners.remove(userName);
        ChatRoom room = roomName == null ? null : rooms.get(roomName);
        if (room != null) {
            room.members.remove(userName);
        }
    }

    private static void log(String text) {
        System.out.println(new SimpleDateFormat("HH:mm:ss").format(new Date()) + "  " + text);
    }
}
