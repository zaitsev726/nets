import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.Date;

public class MessageHandler {
    private static MessageController messageController = new MessageController();

    public MessageHandler() {
    }

    public static void handlingMessage(DatagramPacket dp) {
        byte[] msg = new byte[dp.getLength() - 5];
        byte type = dp.getData()[0];
        System.arraycopy(dp.getData(), 5, msg, 0, dp.getLength() - 5);
        int ID = getId(dp);
        Packet receivedPacket = new Packet(dp.getAddress(), dp.getPort(), ID, msg);
        switch (type) {
            case 0:
                if (ID == 0) {
                    MessageCreator.createFirstInfo(receivedPacket);
                } else {
                    if (!messageController.contains(receivedPacket)) {
                        messageController.addNew(receivedPacket);
                        MessageCreator.createResendMessage(receivedPacket);
                    }
                    MessageCreator.createNewConfirmation(receivedPacket);
                }
                break;
            case 1:
                MessageManagement.deleteMessage(new String(receivedPacket.getData()));
                break;
            case 2:
                UserManagement.updateInfoAboutSubstitute(receivedPacket);
                MessageCreator.createNewConfirmation(receivedPacket);
                break;
            case 4:
                MessageManagement.deleteMessage(new String(receivedPacket.getData()));
                UserManagement.updateInfoAboutSubstitute(receivedPacket);
                break;
            default:
                break;
        }
        messageController.deleteMessages(new Date());
        UserManagement.updateDate(receivedPacket, new Date());
    }

    public static int getId(DatagramPacket dp) {
        int ID = 0;
        byte[] id = new byte[4];
        System.arraycopy(dp.getData(), 1, id, 0, 4);
        for (int i = 0; i < 4; i++) {
            ID += (int) id[i] * Math.pow(10, 3 - i);
        }

        return ID;
    }

    /**
     * Если нет заместителя, то делаем заместителем пользователя
     *
     * @param IP   IP пользователя-заместителя
     * @param port порт пользователя-заместителя
     */
    public static void updateSubstitute(InetAddress IP, int port) {
        if (MessageCreator.getIpSubstitute() == null && MessageCreator.getPortSubstitute() == 0) {
            MessageCreator.setIpSubstitute(IP);
            MessageCreator.setPortSubstitute(port);
        }
    }
}
