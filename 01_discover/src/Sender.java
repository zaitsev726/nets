import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class Sender extends Thread{

    private MulticastSocket socket;
    private String sendData;
    private InetAddress ip;
    private int port;

    Sender(int port, String groupIp){
        try {
            socket = new MulticastSocket(port);
            socket.setInterface(InetAddress.getByName("localhost"));
            ip = InetAddress.getByName(groupIp);
            sendData = "hello";//InetAddress.getLocalHost().getHostAddress();
            socket.joinGroup(ip);
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.port = port;
    }
    public void run(){
        while (true) {
            DatagramPacket sendPacket = new DatagramPacket(sendData.getBytes(), sendData.length(), ip, port);
            try {
                socket.send(sendPacket);
                sleep(1000);
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
