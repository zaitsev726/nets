import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MessageController {
    private final static int HISTORY_TIME = 35000;
    private HashMap<Packet, Date> history;
    MessageController() {
        history = new HashMap<>();
    }

    public boolean contains(Packet receivedPacket) {

        if (history.isEmpty())
            return false;
        else {
            for (Map.Entry<Packet, Date> iterator : history.entrySet()) {
                if (iterator.getKey().equals(receivedPacket))
                    return true;
            }
            return false;
        }
    }

    public void addNew(Packet receivedPacket) {

            history.put(receivedPacket, new Date());
    }


    public void deleteMessages(Date d) {
        for (Iterator<Map.Entry<Packet, Date>> iterator = history.entrySet().iterator(); iterator.hasNext(); ) {
            Map.Entry<Packet, Date> entry = iterator.next();
            if (d.getTime() - entry.getValue().getTime() > HISTORY_TIME) {
                iterator.remove();
            }
        }
    }
}
