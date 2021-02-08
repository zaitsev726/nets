package NetworkPart.NetSocket;

import SnakeGame.Players;
import me.ippolitov.fit.snakes.SnakesProto;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SteerMsgQueue {
    private static volatile SteerMsgQueue instance;
    private HashMap<Integer, SnakesProto.Direction> map;

    private SteerMsgQueue() {
        map = new HashMap<>();
    }

    public static SteerMsgQueue getInstance() {
        SteerMsgQueue localInstance = instance;
        if (localInstance == null) {
            synchronized (SteerMsgQueue.class) {
                localInstance = instance;
                if (localInstance == null)
                    instance = localInstance = new SteerMsgQueue();
            }
        }
        return localInstance;
    }

    public void addNewDirection(SnakesProto.GameMessage.SteerMsg msg, InetAddress IP, int port) {
        String ip_address = IP.toString();
        int ID = 0;
        List<SnakesProto.GamePlayer> players = Players.getInstance().getPlayers();
        List<SnakesProto.GameState.Snake> snakes = Players.getInstance().getSnakes();
        for (int i = 0; i < players.size(); i++) {
            SnakesProto.GamePlayer player = players.get(i);
            if (!player.getRole().equals(SnakesProto.NodeRole.VIEWER)) {
                SnakesProto.GameState.Snake snake = null;

                for (int j = 0; j < snakes.size(); j++) {
                    if (snakes.get(j).getPlayerId() == player.getId())
                        snake = snakes.get(j);
                }
                if (snake != null) {

                    if (player.getPort() == port && player.getIpAddress().equals(ip_address)
                            && isOppositeDirection(snake, msg.getDirection())) {
                        ID = player.getId();
                    }
                } else
                    return;

                if (ID != 0) {
                    map.put(ID, msg.getDirection());
                }
            }
        }
    }

    public Map<Integer, SnakesProto.Direction> getMap(){
        return map;
    }
    private boolean isOppositeDirection(SnakesProto.GameState.Snake snake, SnakesProto.Direction direction){
        if((snake.getHeadDirection() == SnakesProto.Direction.RIGHT || snake.getHeadDirection() == SnakesProto.Direction.LEFT)
                && (direction == SnakesProto.Direction.LEFT || direction == SnakesProto.Direction.RIGHT)){
            return false;
        }
        if((snake.getHeadDirection() == SnakesProto.Direction.UP || snake.getHeadDirection() == SnakesProto.Direction.DOWN )
                && (direction == SnakesProto.Direction.UP || direction == SnakesProto.Direction.DOWN)){
            return false;
        }
        return true;
    }
}
