package backend;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

public class ServerConn {

    //How are we going to use these?
    static ConcurrentHashMap<Integer, Socket> uidToSocket = new ConcurrentHashMap<>();
    static ConcurrentHashMap<Integer, ServerThread> uidToServerThread = new ConcurrentHashMap<>();


    private static ServerSocket listener;

    public static void main(String[] args) throws Exception {
        System.err.println("Server is running!");
        ServerConn conn = new ServerConn();
        conn.start();
    }

    private void start() throws IOException {
        try {
            listener = new ServerSocket(5000);

            while (true) {
                Socket sock = listener.accept();
                System.out.println("Accepted    socket from: " + sock.toString());
                ServerThread new_thread = new ServerThread(sock);
                new_thread.start();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

}
