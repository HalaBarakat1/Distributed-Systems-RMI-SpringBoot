package org.ds.skype.client;

import org.ds.skype.shared.IClientListener;
import org.ds.skype.shared.IServerServices;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.NoSuchObjectException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;

/**
 * Console chat client. Run several instances (one per user).
 * Optional argument: the server host (default localhost).
 */
public class Client {

    private static final String HELP =
            "Commands:\n"
            + "  /signup <user> <password> <first> <last>   create an account\n"
            + "  /login <user> <password> <room>            sign in to a room\n"
            + "  /logout                                    sign out of the room\n"
            + "  /rooms                                     list rooms\n"
            + "  /users [room]                              list users in a room\n"
            + "  /addroom <name> [capacity]                 create a room\n"
            + "  /removeroom <name>                         delete a room\n"
            + "  /msg <user> <text>                         private message THROUGH the server (callback)\n"
            + "  /p2p <user> <text>                         private message DIRECTLY to the user (peer-to-peer)\n"
            + "  /all <text>   or just type text            message to the whole room\n"
            + "  /help, /quit";

    private final IServerServices remote_ref;
    private String userName;
    private String roomName;
    private ClientListener listener;

    public Client(IServerServices remote_ref) {
        this.remote_ref = remote_ref;
    }

    public static void main(String[] args) throws IOException, NotBoundException {
        String host = args.length > 0 ? args[0] : "localhost";

        Registry reg = LocateRegistry.getRegistry(host, 6000);
        Remote ref = reg.lookup("rmiServer");
        IServerServices remote_ref = (IServerServices) ref;

        System.out.println("Connected to chat server at " + host + ":6000");
        System.out.println(HELP);
        new Client(remote_ref).run();
    }

    private void run() throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        String line;
        System.out.print("> ");
        while ((line = in.readLine()) != null) {
            line = line.trim();
            if (!line.isEmpty()) {
                try {
                    if (!handle(line)) {
                        break;
                    }
                } catch (RemoteException e) {
                    System.out.println("!! Server error: " + e.getMessage());
                }
            }
            System.out.print("> ");
        }
        quit();
    }

    /** Returns false when the user wants to quit. */
    private boolean handle(String line) throws RemoteException {
        String[] p = line.split("\\s+", 3);
        String cmd = p[0].toLowerCase();

        if (cmd.equals("/quit")) {
            return false;
        } else if (cmd.equals("/help")) {
            System.out.println(HELP);
        } else if (cmd.equals("/signup")) {
            String[] a = line.split("\\s+");
            if (a.length != 5) {
                System.out.println("Usage: /signup <user> <password> <first> <last>");
            } else {
                int r = remote_ref.signup(a[1], a[2], a[3], a[4]);
                System.out.println(r == 1 ? "Account created." : "User name already taken.");
            }
        } else if (cmd.equals("/login")) {
            String[] a = line.split("\\s+");
            if (a.length != 4) {
                System.out.println("Usage: /login <user> <password> <room>");
            } else {
                login(a[1], a[2], a[3]);
            }
        } else if (cmd.equals("/logout")) {
            logout();
        } else if (cmd.equals("/rooms")) {
            System.out.println("Rooms: " + Arrays.toString(remote_ref.ListAvailableRooms()));
        } else if (cmd.equals("/users")) {
            String room = p.length > 1 ? p[1] : roomName;
            if (room == null) {
                System.out.println("Usage: /users <room>");
            } else {
                System.out.println("In " + room + ": " + Arrays.toString(remote_ref.ListClientsInRoom(room)));
            }
        } else if (cmd.equals("/addroom")) {
            if (p.length < 2) {
                System.out.println("Usage: /addroom <name> [capacity]");
            } else {
                int r = p.length > 2 ? remote_ref.AddChatRoom(p[1], Integer.parseInt(p[2])) : remote_ref.AddChatRoom(p[1]);
                System.out.println(r == 1 ? "Room created." : "Room already exists (or bad capacity).");
            }
        } else if (cmd.equals("/removeroom")) {
            if (p.length < 2) {
                System.out.println("Usage: /removeroom <name>");
            } else {
                System.out.println(remote_ref.removeChatRoom(p[1]) == 1 ? "Room removed." : "No such room.");
            }
        } else if (cmd.equals("/msg")) {
            if (requireLogin() && p.length == 3) {
                // Through the server: server finds the receiver's listener and calls it back.
                int r = remote_ref.SendMessage("[" + userName + " -> you] " + p[2], p[1], roomName);
                System.out.println(r == 1 ? "(sent via server)" : p[1] + " is not in this room.");
            } else if (p.length != 3) {
                System.out.println("Usage: /msg <user> <text>");
            }
        } else if (cmd.equals("/p2p")) {
            if (requireLogin() && p.length == 3) {
                sendPeerToPeer(p[1], p[2]);
            } else if (p.length != 3) {
                System.out.println("Usage: /p2p <user> <text>");
            }
        } else if (cmd.equals("/all")) {
            if (requireLogin() && p.length >= 2) {
                broadcast(line.substring(4).trim());
            }
        } else if (cmd.startsWith("/")) {
            System.out.println("Unknown command. Type /help");
        } else if (requireLogin()) {
            broadcast(line);
        }
        return true;
    }

    private void login(String user, String password, String room) throws RemoteException {
        if (userName != null) {
            System.out.println("Already signed in as " + userName + ". Use /logout first.");
            return;
        }
        int flag = remote_ref.signin(user, password, room);
        if (flag == 1) {
            userName = user;
            roomName = room;
            // Make this client a remote object and hand it to the server (callback registration).
            listener = new ClientListener(user);
            remote_ref.AddReceiveListener(listener);
            System.out.println("Signed in to " + room + " as " + user + ". In room: "
                    + Arrays.toString(remote_ref.ListClientsInRoom(room)));
        } else if (flag == 0) {
            System.out.println("Error in Sign in: wrong user name or password.");
        } else if (flag == -1) {
            System.out.println("Error in Sign in: no room called " + room + ".");
        } else if (flag == -2) {
            System.out.println("Error in Sign in: room " + room + " is full.");
        } else {
            System.out.println("Error in Sign in: " + user + " is already signed in.");
        }
    }

    private void logout() throws RemoteException {
        if (userName == null) {
            System.out.println("Not signed in.");
            return;
        }
        remote_ref.signout(userName, roomName);
        remote_ref.RemoveReceiveListener(listener);
        try {
            UnicastRemoteObject.unexportObject(listener, true);
        } catch (NoSuchObjectException ignored) {
            // already unexported
        }
        System.out.println("Signed out of " + roomName + ".");
        userName = null;
        roomName = null;
        listener = null;
    }

    private void broadcast(String text) throws RemoteException {
        int n = remote_ref.SendBroadCatMessage(userName, text, roomName);
        System.out.println("(sent to " + n + " user(s) in " + roomName + ")");
    }

    /** Calls the other client's ReceiveMessage directly - the server is not involved. */
    private void sendPeerToPeer(String dest, String text) {
        IClientListener clientListener = listener.getClientListener(dest);
        if (clientListener == null) {
            System.out.println(dest + " is not in my peer list (" + listener.peerCount() + " peer(s)).");
            return;
        }
        try {
            clientListener.ReceiveMessage("[P2P " + userName + " -> you] " + text);
            System.out.println("(sent directly to " + dest + ", not through the server)");
        } catch (RemoteException e) {
            System.out.println("Could not reach " + dest + " directly: " + e.getMessage());
        }
    }

    private boolean requireLogin() {
        if (userName == null) {
            System.out.println("Sign in first: /login <user> <password> <room>");
            return false;
        }
        return true;
    }

    private void quit() {
        try {
            if (userName != null) {
                logout();
            }
        } catch (RemoteException e) {
            System.out.println("Server unreachable while signing out.");
        }
        System.out.println("Bye.");
        System.exit(0);
    }
}
