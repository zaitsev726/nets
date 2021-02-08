import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.*;

public class Receiver extends Thread {

    private int port;
    private MulticastSocket socket;
    private InetAddress ip;
    private byte[] receiveData = new byte[1024];
    private Map<String, Long> adresses = new HashMap<>();
    private boolean update = true;


    Receiver(int port, String ip){
        try {
            socket = new MulticastSocket(port);
            socket.setInterface(InetAddress.getByName("192.168.1.207"));
            this.ip = InetAddress.getByName(ip);
            socket.joinGroup(this.ip);
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.port = port;
    }
    public void run(){
        while(true) {
            System.out.println("rrrrr");
            Date date = new Date();
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length,ip, port);
            try {
                socket.receive(receivePacket);
            } catch (IOException e) {
                e.printStackTrace();
            }
            String sentence = new String(receivePacket.getAddress().toString());
            if (!adresses.containsKey(sentence)) {
                adresses.put(sentence, date.getTime());
                update = true;
            } else {
                adresses.put(sentence, date.getTime());
            }
            checkOld();
            if(update) {
                for (String key : adresses.keySet()) {
                    System.out.println("IP: " + key);
                }
                System.out.println("---------------");
            }
            update = false;
            Arrays.fill(receiveData, (byte)0);
        }

    }
    private void checkOld(){
        Date date = new Date();
        for (Iterator<String> iterator = adresses.keySet().iterator(); iterator.hasNext(); ) {
            String key = iterator.next();
            if ((date.getTime() - adresses.get(key)) > 3000) {
                iterator.remove();
                update = true;
            }
        }
    }

}
