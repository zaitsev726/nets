import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class NodeReceiver extends Thread {
    private static final int BUF_SIZE = 2048;
    private DatagramSocket socket;
    private int losses;

    public NodeReceiver(DatagramSocket s, int lost) {
        socket = s;
        losses = lost;
    }

    @Override
    public void run() {
        try {
            byte[] buf = new byte[BUF_SIZE];
            DatagramPacket dp = new DatagramPacket(buf, buf.length);
            while (true) {
                socket.receive(dp);
                if (((int) (Math.random() * 100)) >= losses) {
                    if (!UserManagement.contains(dp.getAddress(), dp.getPort())) {
                        if (UserManagement.myNeighbours.isEmpty()) {
                            MessageHandler.updateSubstitute(dp.getAddress(), dp.getPort());
                        }
                        UserManagement.addNew(dp.getAddress(), dp.getPort());
                        MessageCreator.createNewPing(dp.getAddress(), dp.getPort());
                    }
                    MessageHandler.handlingMessage(dp);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
