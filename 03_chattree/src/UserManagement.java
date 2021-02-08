import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class UserManagement {

    public static volatile List<User> myNeighbours;

    UserManagement() {
        myNeighbours = new ArrayList<>();
    }

    /**
     * Проверяет, содержится ли пользователь в списке соседей
     *
     * @param IP   IP проверяемого пользователя
     * @param port порт проверяемого пользователя
     * @return Если такой пользователь является нашим соседом, то возвращаем true, иначе false
     */
    public static boolean contains(InetAddress IP, int port) {
        if (myNeighbours.isEmpty())
            return false;
        for (User i : myNeighbours) {
            if (i.getIP().toString().equals(IP.toString()) && i.getPORT() == port)
                return true;
        }
        return false;
    }

    /**
     * Добавление пользователя в список соседей
     *
     * @param IP   IP добавляемого пользователя
     * @param port порт добавляемого пользователя
     */
    public synchronized static void addNew(InetAddress IP, int port) {
        User user = new User(IP, port);
        myNeighbours.add(user);
    }

    /**
     * Обновляем время последнего соединения
     *
     * @param receivedPacket пакет пользователя, для которого обновляется время последнего соединения
     * @param d              Время, когда обновляется информация
     */
    public static void updateDate(Packet receivedPacket, Date d) {
        for (User i : myNeighbours) {
            if (i.getIP().toString().equals(receivedPacket.getIP().toString()) && i.getPORT() == receivedPacket.getPort())
                i.setLastPing(d);
        }
    }

    /**
     * Обновляем информацию о заместителе
     *
     * @param receivedPacket пакет, который сообщает измененную информацию
     */
    public static void updateInfoAboutSubstitute(Packet receivedPacket) {
        String IP = receivedPacket.getIP().toString();
        int port = receivedPacket.getPort();
        String msg = new String(receivedPacket.getData());
        String[] array = msg.split(" ");
        for (User i : myNeighbours) {
            if (i.getIP().toString().equals(IP) && i.getPORT() == port) {
                try {
                    i.setIP_substitute(InetAddress.getByName(array[array.length - 3]));
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
                System.out.println(i.getIP_substitute());
                i.setPORT_substitute(Integer.parseInt(array[array.length - 2]));
                System.out.println(i.getPORT_substitute());
            }
        }
    }

    /**
     * Выводит список пользователей в консоль
     */
    public static void printUsers() {
        synchronized (myNeighbours) {
            System.out.println("Выводим список пользователей");
            for (int i = 0; i < myNeighbours.size(); i++) {
                User user = myNeighbours.get(i);
                System.out.println("Name: " + user.getName() + " IP: " + user.getIP() + " PORT: " + user.getPORT());
            }
        }
    }

    /**
     * Оповещение всех соседей о моем заместителе, кроме самого заместителя
     */
    public static void notifyAllExceptSubstitute() {
        for (User neighbour : myNeighbours) {
            if (!neighbour.getIP().toString().equals(MessageCreator.getIpSubstitute().toString()) && !(neighbour.getPORT() == MessageCreator.getPortSubstitute()))
                MessageCreator.createInfoAboutSubstitute(neighbour.getIP(), neighbour.getPORT());
        }

    }

    /**
     * Оповещение всех соседей о моем заместителе
     */
    public static void notifyAllNeighboursAboutMySubstitute() {
        for (User neighbour : myNeighbours) {
            MessageCreator.createInfoAboutSubstitute(neighbour.getIP(), neighbour.getPORT());
        }

    }


}
