import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

public class NodeSender {
    private static DatagramSocket socket;

    public NodeSender(DatagramSocket socket) {
        this.socket = socket;

    }

    public NodeSender(DatagramSocket socket, InetAddress ipToConnect, int portToConnect){
        this.socket = socket;
        MessageCreator.createFirstMeet(ipToConnect, portToConnect);
    }

    /**
     * Отправляем все сообщения, находящиеся в myNeighbours
     *
     * @param list Список сообщений для отправки
     */
    public static void sendMessages(List<DatagramPacket> list) {
        for (DatagramPacket datagramPacket : list) {
            try {
                socket.send(datagramPacket);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        list.clear();
    }
}
