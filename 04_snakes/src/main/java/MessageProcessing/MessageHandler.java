package MessageProcessing;

import Global.GlobalController;
import NetworkPart.NetSocket.SendPart.ResendQueue;
import NetworkPart.NetSocket.SteerMsgQueue;
import SnakeGame.Players;
import me.ippolitov.fit.snakes.SnakesProto;

import java.net.InetAddress;
import java.util.ArrayList;

public class MessageHandler {
    private GlobalController controller;
    private ResendQueue resendQueue;
    public MessageHandler(GlobalController controller, ResendQueue resendQueue){
        this.controller = controller;
        this.resendQueue = resendQueue;
    }

    public void handlingMessage(SnakesProto.GameMessage message, InetAddress address, int port, long msg_seq){

        switch (message.getTypeCase()) {
            case PING:
                break;
            case STEER:
                SteerMsgQueue.getInstance().addNewDirection(message.getSteer(),address,port);
                break;
            case ACK:
                resendQueue.deleteMessage(message);
                break;
            case STATE:
                if(!controller.getMaster()) {
                    SnakesProto.GameState state = message.getState().getState();
                    Players.getInstance().setPlayers(new ArrayList<SnakesProto.GamePlayer>(state.getPlayers().getPlayersList()));
                    Players.getInstance().setSnakes(state.getSnakesList());
                    controller.sendAck(message.getMsgSeq(), Players.getInstance().getHostID());
                    controller.setHostID(state.getPlayers().getPlayersList());
                    controller.setHostIP(address.toString());
                    controller.setHostPort(port);


                    controller.setState(state);
                }
                break;
            case ANNOUNCEMENT:
                break;
            case JOIN:
                Players.getInstance().addNewPlayerInQueue(message, address,port,msg_seq);
                break;
            case ERROR:
                controller.errorMessage(message.getError());
                break;
            case ROLE_CHANGE:
                controller.sendAck(message.getMsgSeq(), message.getSenderId());
                if(message.getRoleChange().getReceiverRole().equals(SnakesProto.NodeRole.MASTER))
                    Players.getInstance().updateRole(address,port);

                if(message.getRoleChange().getSenderRole().equals(SnakesProto.NodeRole.VIEWER)
                    && message.getRoleChange().getReceiverRole().equals(SnakesProto.NodeRole.MASTER))
                    Players.getInstance().updateRole(address, port);

                if(message.getRoleChange().getReceiverRole().equals(SnakesProto.NodeRole.DEPUTY)
                && message.getRoleChange().getSenderRole().equals(SnakesProto.NodeRole.VIEWER)){
                    controller.updateGame( null,true);
                }
                if(message.getRoleChange().getReceiverRole().equals(SnakesProto.NodeRole.DEPUTY)
                && message.getRoleChange().getSenderRole().equals(SnakesProto.NodeRole.NORMAL)){
                    controller.updateGame( null,false);
                }

                break;
            case TYPE_NOT_SET:
                break;

        }
    }
}
