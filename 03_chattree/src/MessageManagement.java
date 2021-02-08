import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MessageManagement extends Thread {
    private static volatile Map<Packet, Date> queue = new ConcurrentHashMap<>();
    private final static int PING_TIME = 1000;
    private final static int RESEND_TIME = 1000;
    public static volatile List<DatagramPacket> messages = new ArrayList<>();
    /**
     * Добавляет пакет в очередь на отправку
     *
     * @param packet пакет, который необходимо отправить
     */
    public synchronized static void addNewMessage(Packet packet) {
        if (queue.isEmpty()) {
            queue.put(packet, new Date());
        } else {
            for (Iterator<Map.Entry<Packet, Date>> it = queue.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry<Packet, Date> iterator = it.next();
                if (iterator.getKey().equals(packet)) {
                    return;
                }
            }
            queue.put(packet, new Date());
        }
    }

    /**
     * Очищает очередь от пакетов, одрасованных пользователю
     *
     * @param addr IP пользователя, которому адресованы сообщения
     * @param port порт пользователя, которому адресованы сообщения
     */
    public synchronized static void clearQueue(InetAddress addr, int port) {
        if (!queue.isEmpty()) {
            for (Iterator<Map.Entry<Packet, Date>> iterator = queue.entrySet().iterator(); iterator.hasNext(); ) {
                Map.Entry<Packet, Date> entry = iterator.next();
                if (addr.toString().equals(entry.getKey().getIP().toString()) && port == entry.getKey().getPort()) {
                    iterator.remove();
                }
            }
        }
    }


    /** Удаление сообщения из очереди на отправку
     * @param msg Удаляемое сообщение
     */
    public synchronized static void deleteMessage(String msg) {
        String[] array = msg.split(" ");
        int ID = Integer.parseInt(array[array.length - 1]);
        if (!queue.isEmpty()) {
            for (Iterator<Map.Entry<Packet, Date>> iterator = queue.entrySet().iterator(); iterator.hasNext(); ) {
                Map.Entry<Packet, Date> entry = iterator.next();
                if (ID == entry.getKey().getID()) {
                    iterator.remove();
                }
            }
        }
    }

    private static int getType(byte[] msg) {
        return msg[0];
    }

    @Override
    public void run() {
        while (true) {
            if (!queue.isEmpty()) {

                Date d = new Date();
                for (Iterator<Map.Entry<Packet, Date>> iterator = queue.entrySet().iterator(); iterator.hasNext(); ) {
                    Map.Entry<Packet, Date> entry = iterator.next();
                    int type = getType(entry.getKey().getData());

                    if (type == 3) {
                        if (d.getTime() - entry.getValue().getTime() > PING_TIME) {
                            queue.put(entry.getKey(), d);
                            messages.add(new DatagramPacket(entry.getKey().getData(), entry.getKey().getData().length,
                                    entry.getKey().getIP(), entry.getKey().getPort()));
                        }
                    } else if (type == 4) {
                        messages.add(new DatagramPacket(entry.getKey().getData(), entry.getKey().getData().length,
                                entry.getKey().getIP(), entry.getKey().getPort()));
                        iterator.remove();
                    } else {
                        if (d.getTime() - entry.getValue().getTime() > RESEND_TIME) {
                            queue.put(entry.getKey(), d);
                            messages.add(new DatagramPacket(entry.getKey().getData(), entry.getKey().getData().length,
                                    entry.getKey().getIP(), entry.getKey().getPort()));
                            if (type == 1) {
                                iterator.remove();
                            } else
                                entry.getValue().setTime(d.getTime());
                        }
                    }
                    if (!messages.isEmpty())
                        NodeSender.sendMessages(messages);
                }

            }
        }
    }
}
