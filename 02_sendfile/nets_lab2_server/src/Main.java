import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.LinkedList;

public class Main {
    private static final int PORT = 5555;
    private static LinkedList<ServerSomething> serverList = new LinkedList<>();
    private static ServerSocket server;
    public static void main(String[] args) throws UnknownHostException {
        System.out.println(InetAddress.getLocalHost());
        try {
            server = new ServerSocket(PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Server Started");
        try {
            while (true) {
                Socket socket = server.accept();
                System.out.println(socket.getInetAddress());
                serverList.add(new ServerSomething(socket));
            }
        } catch (IOException e) {
            System.out.println("SM is bad");
        }
    }
}

