package org.ds.skype.server;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * Starts the RMI registry on port 6000 and registers the chat service as "rmiServer".
 */
public class Server {

    public static final int PORT = 6000;
    public static final String SERVICE_NAME = "rmiServer";

    public static void main(String[] args) throws RemoteException {
        Registry reg = LocateRegistry.createRegistry(PORT);

        Server_Services_Imp obj = new Server_Services_Imp();
        // Two rooms ready for testing; clients can add more with /addroom
        obj.AddChatRoom("General");
        obj.AddChatRoom("Study", 3);

        reg.rebind(SERVICE_NAME, obj);
        System.out.println("Chat server ready: registry on port " + PORT + ", service \"" + SERVICE_NAME + "\"");
    }
}
