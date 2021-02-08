import java.net.InetAddress;

public class MessageCreator {
    private static volatile int increment = 1;
    private static InetAddress ipSubstitute = null;
    private static int portSubstitute = 0;
    private static String name;

    public static void setName(String name) {
        MessageCreator.name = name;
    }

    public static void setIpSubstitute(InetAddress ipSubstitute) {
        MessageCreator.ipSubstitute = ipSubstitute;
    }

    public static void setPortSubstitute(int portSubstitute) {
        MessageCreator.portSubstitute = portSubstitute;
    }

    public static InetAddress getIpSubstitute() {
        return ipSubstitute;
    }

    public static int getPortSubstitute() {
        return portSubstitute;
    }

    public MessageCreator() {
    }

    /**
     * Создание обычного сообщения, написанного пользователем
     *
     * @param str Передаваемое сообщение
     */
    public static void createNewMessage(String str) {
        for (User i : UserManagement.myNeighbours) {
            MessageManagement.addNewMessage(createPacket(0, str, i.getIP(), i.getPORT()));
        }
    }

    /**
     * Создание пересылаемого сообщения, которое отправляется всем соседям, кроме отправителя
     *
     * @param receivedPacket Пакет, который пришел от отправителя
     */
    public static void createResendMessage(Packet receivedPacket) {
        for (User i : UserManagement.myNeighbours) {
            if (!(i.getIP().toString().equals(receivedPacket.getIP().toString()) && receivedPacket.getPort() == i.getPORT())) {
                MessageManagement.addNewMessage(createPacket(0, new String(receivedPacket.getData()), i.getIP(), i.getPORT()));
            }

        }
    }


    /**
     * Создание сообщениея-подтверждения
     *
     * @param receivedPacket Пакет, который подтверждаем
     */
    public static void createNewConfirmation(Packet receivedPacket) {
        if (receivedPacket.getID() > 0 && receivedPacket.getID() < 9999) {
            System.out.println("Создаем новый ассепт " + receivedPacket.getID());
            String str = "I accept " + receivedPacket.getID();
            MessageManagement.addNewMessage(createPacket(1, str, receivedPacket.getIP(), receivedPacket.getPort()));
        }
    }

    /**
     * Создание служебного сообщения о заместителе
     *
     * @param ip   IP соседа, которому отправляется служебное сообщение
     * @param port порт соседа, которому отправляется служебное сообщение
     */
    public static void createInfoAboutSubstitute(InetAddress ip, int port) {
        String subIP = (ipSubstitute == null) ? "null" : ipSubstitute.toString().substring(1);
        String str = "HI!Mynameis " + name + " " + subIP + " " + portSubstitute + " ";
        MessageManagement.addNewMessage(createPacket(2, str, ip, port));
    }

    /**
     * При первом подключении используется особый тип сообщений, который является подтверждением FirstMeeet'a и служебным сообщением о заместителе
     *
     * @param receivedPacket Пакет, на который отвечаем
     * @return
     */
    public static void createFirstInfo(Packet receivedPacket) {
        String str;
        String subIP = (ipSubstitute == null) ? "null" : ipSubstitute.toString().substring(1);
        str = "HI!Mynameis " + name + " " + subIP + " " + portSubstitute + " " + 0;
        MessageManagement.addNewMessage(createPacket(4, str, receivedPacket.getIP(), receivedPacket.getPort()));
    }

    /**
     * Создание пинг-сообщения
     *
     * @param IP   IP соседа, которому предназначается пинг-сообщение
     * @param port порт соседа, которому предназначается пинг-сообщение
     */
    public static void createNewPing(InetAddress IP, int port) {
        MessageManagement.addNewMessage(createPacket(3, "PING PACKET", IP, port));
    }

    /**
     * Создание сообщения, которое образовывает связь между узлами
     *
     * @param IP   IP узла, с которым хотим образовать связь
     * @param port порт узла, с которым хотим образовать связь
     */
    public static void createFirstMeet(InetAddress IP, int port) {
        String message = "Im joining " + name;
        byte[] packet = new byte[message.getBytes().length + 5];
        packet[0] = 0;
        packet[1] = 0;
        packet[2] = 0;
        packet[3] = 0;
        packet[4] = 0;
        setIpSubstitute(IP);
        setPortSubstitute(port);
        System.arraycopy(message.getBytes(), 0, packet, 5, message.getBytes().length);
        MessageManagement.addNewMessage(new Packet(IP, port, 0, packet));
    }

    /**
     * Создание пакета, у которого первый байт отвечает за тип сообщения, следующие 4 байта отвечают за ID сообщения, далее идет пересылаемое сообщение.
     * Типы сообщений:
     * 0 - обычное сообщение
     * 1 - сообщение-подтверждение
     * 2 - служебное сообщение о заместителе
     * 3 - пинг-сообщение
     * 4 - ответ на сообщение, связавшее два узла
     *
     * @param type    Тип сообщения
     * @param message Пересылаемое сообщение
     * @param IP      IP соседа, которому адресован пакет
     * @param port    порт соседа, которому адресован пакет
     * @return Возвращает созданный пакет
     */
    private static Packet createPacket(int type, String message, InetAddress IP, int port) {
        byte[] array = message.getBytes();
        byte[] msg = new byte[array.length + 5];
        msg[0] = (byte) type;
        for (int i = 0; i < 4; i++) {
            msg[i + 1] = (byte) ((increment / Math.pow(10, 3 - i)) % 10);
        }
        System.arraycopy(array, 0, msg, 5, array.length);
        Packet packet = new Packet(IP, port, increment, msg);
        ++increment;
        if (increment > 9999) {
            increment = 1;
        }
        return packet;
    }
}
