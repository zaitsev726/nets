import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class ChatNode {
    private InetAddress ipToConnect;
    private int portToConnect;

    private MessageManagement messageManagement;
    private NodeReceiver in;
    private static DelayController delayController;
    private static DatagramSocket socket;


    public ChatNode(String[] args) throws ArgsNotFoundException, IOException {
        String nodeName;
        int port, losses;
        if (args.length < 3)
            throw new ArgsNotFoundException("Недостаточно аргументов, необходимо 3 аргумента");
        nodeName = args[0];
        port = Integer.parseInt(args[1]);
        losses = Integer.parseInt(args[2]);
        portToConnect = 0;
        if (args.length == 5) {
            ipToConnect = InetAddress.getByName(args[3]);
            portToConnect = Integer.parseInt(args[4]);
        }
        socket = new DatagramSocket(port);
        new UserManagement();
        MessageCreator.setName(nodeName);
        new NodeSender(
                (portToConnect == 0) ? socket : socket, ipToConnect, portToConnect
        );
        messageManagement = new MessageManagement();
        delayController = new DelayController(InetAddress.getLocalHost(), port);
        delayController.start();
        messageManagement.start();
        in = new NodeReceiver(socket, losses);
        in.start();
    }

    public void exit() {
        messageManagement.interrupt();
        delayController.interrupt();
        in.interrupt();
        socket.close();
    }
}
