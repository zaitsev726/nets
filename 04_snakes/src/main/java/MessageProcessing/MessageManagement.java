package MessageProcessing;

import Global.GlobalController;
import NetworkPart.NetSocket.MessageReceiver;
import SnakeGame.Players;
import me.ippolitov.fit.snakes.SnakesProto;

import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MessageManagement extends Thread {

    private int node_timeout_ms;
    private MessageReceiver receiver;
    private GlobalController controller;

    public MessageManagement(int node_timeout_ms, MessageReceiver receiver, GlobalController controller) {
        this.node_timeout_ms = node_timeout_ms;
        this.receiver = receiver;
        this.controller = controller;
    }

    @Override
    public void run() {
        while (true) {
            Date now = new Date();
            ConcurrentHashMap<SnakesProto.GamePlayer, Date> lastMessage = receiver.getLastMessage();
            Iterator<Map.Entry<SnakesProto.GamePlayer, Date>> iterator = lastMessage.entrySet().iterator();

            while (iterator.hasNext()) {
                Map.Entry<SnakesProto.GamePlayer, Date> player = iterator.next();
                if (now.getTime() - player.getValue().getTime() > node_timeout_ms) {
                    if (controller.getMaster()) {
                        for (SnakesProto.GamePlayer gamePlayer : Players.getInstance().getPlayers()) {
                            if (gamePlayer.getIpAddress().equals(player.getKey().getIpAddress()) &&
                                    gamePlayer.getPort() == player.getKey().getPort()) {
                                if (controller.getMaster()) {
                                    controller.sendRoleChange(SnakesProto.NodeRole.VIEWER,
                                            SnakesProto.NodeRole.MASTER,
                                            gamePlayer.getId());
                                }
                            }
                        }
                    } else if (player.getKey().getIpAddress().equals(controller.getHostIP()) &&
                            player.getKey().getPort() == controller.getHostPort()) {

                        controller.updateGame(null, false);
                    }
                    iterator.remove();
                }
            }
        }
    }
}
