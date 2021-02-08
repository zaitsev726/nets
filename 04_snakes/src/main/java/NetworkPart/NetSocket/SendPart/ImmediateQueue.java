package NetworkPart.NetSocket.SendPart;

import me.ippolitov.fit.snakes.SnakesProto.GameMessage;

import java.util.concurrent.LinkedBlockingQueue;

public class ImmediateQueue {
    private LinkedBlockingQueue<GameMessage> q;

    public ImmediateQueue(){
        q = new LinkedBlockingQueue<>();
    }

    public void addNewMessage(GameMessage message){ q.add(message); }

    public LinkedBlockingQueue<GameMessage> getQ() { return q; }
}

