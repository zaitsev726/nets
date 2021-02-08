import java.net.InetAddress;
import java.util.Date;
import java.util.Iterator;

public class DelayController extends Thread {
    private final static int DISCONNECTION_TIME = 30000;
    private InetAddress IP;
    private int port;

    public DelayController(InetAddress IP, int port) {
        this.IP = IP;
        this.port = port;
    }

    @Override
    public void run() {
        while (true) {

            if (!UserManagement.myNeighbours.isEmpty()) {
                synchronized (UserManagement.myNeighbours) {
                    Date d = new Date();
                    Iterator<User> iterator = UserManagement.myNeighbours.iterator();
                    while (iterator.hasNext()) {
                        User removedNeighbour = iterator.next();
                        if (d.getTime() - removedNeighbour.getLastPing().getTime() > DISCONNECTION_TIME) {
                            MessageManagement.clearQueue(
                                    removedNeighbour.getIP(),
                                    removedNeighbour.getPORT()
                            );
                            iterator.remove();
                            if (removedNeighbour.getIP_substitute() != null && removedNeighbour.getPORT_substitute() != 0) { //если у удаленного был заместитель
                                if (    !IP.toString().equals(removedNeighbour.getIP_substitute().toString())
                                        || !(removedNeighbour.getPORT_substitute() == port)) {                               //если заместитель не я, то образовать связь с заместителем
                                    MessageCreator.createFirstMeet(
                                            removedNeighbour.getIP_substitute(),
                                            removedNeighbour.getPORT_substitute()
                                    );
                                    UserManagement.notifyAllExceptSubstitute();
                                } else {                                                                                    // если я заместитель удаленного, значит выше него никого не было
                                    if (!UserManagement.myNeighbours.isEmpty()) {                                                   // т.е. я теперь корень, поэтому
                                        User user1 = UserManagement.myNeighbours.get(0);                                            // если есть соседи, выбрать нового заместителя
                                        MessageCreator.setIpSubstitute(user1.getIP());
                                        MessageCreator.setPortSubstitute(user1.getPORT());
                                        UserManagement.notifyAllNeighboursAboutMySubstitute();
                                    } else {                                                                                // если соседей нет, заместителя нет
                                        MessageCreator.setIpSubstitute(null);
                                        MessageCreator.setPortSubstitute(0);
                                    }
                                }
                            }
                        }
                    }
                }
            }

            try {
                Thread.sleep(DISCONNECTION_TIME / 2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}