package MessageProcessing;

import me.ippolitov.fit.snakes.SnakesProto;

public class MessageCreator {
    private static volatile long message_ID = 1;

    /*
    PingMsg ping = 2;
        SteerMsg steer = 3;
        AckMsg ack = 4;
        StateMsg state = 5;
        AnnouncementMsg announcement = 6;
        JoinMsg join = 7;
        ErrorMsg error = 8;
        RoleChangeMsg role_change = 9;
     */

    public static SnakesProto.GameMessage  createNewPing(){
        synchronized (MessageCreator.class) {
            SnakesProto.GameMessage message = SnakesProto.GameMessage.newBuilder()
                    .setMsgSeq(message_ID)
                    .setPing(SnakesProto.GameMessage.PingMsg.newBuilder().build())
                    .build();
            message_ID++;
            return message;
        }
    }

    public static SnakesProto.GameMessage createNewAckMsg(long msg_seq, int ID) {
        synchronized (MessageCreator.class) {
            SnakesProto.GameMessage message = SnakesProto.GameMessage.newBuilder()
                    .setAck(SnakesProto.GameMessage.AckMsg.newBuilder().build())
                    .setMsgSeq(msg_seq)
                    .setReceiverId(ID)
                    .build();
            //MessageManagement.addNewMessage(message);
            return message;
        }
    }

    public static SnakesProto.GameMessage createNewSteerMsg(SnakesProto.Direction direction){
        synchronized (MessageCreator.class){

            SnakesProto.GameMessage message = SnakesProto.GameMessage.newBuilder()
                    .setMsgSeq(message_ID)
                    .setSteer(SnakesProto.GameMessage.SteerMsg.newBuilder()
                            .setDirection(direction).build())
                    .build();
                    message_ID++;
            return message;
        }

    }

    public static SnakesProto.GameMessage createNewAnnouncementMsg(SnakesProto.GamePlayers players,
                                                                   SnakesProto.GameConfig config,
                                                                   boolean can_join){
        synchronized (MessageCreator.class) {
            SnakesProto.GameMessage message = SnakesProto.GameMessage.newBuilder()
                    .setMsgSeq(message_ID)
                    .setAnnouncement(SnakesProto.GameMessage.AnnouncementMsg.newBuilder()
                            .setPlayers(players)
                            .setConfig(config)
                            .setCanJoin(can_join)
                            .build())
                    .build();
            message_ID++;
            return message;
        }
    }

    public static SnakesProto.GameMessage createNewRoleChangeMsg(SnakesProto.NodeRole sender, SnakesProto.NodeRole receiver,
                                                                 int sender_id, int receiver_id){
        synchronized (MessageCreator.class) {
            SnakesProto.GameMessage message = SnakesProto.GameMessage.newBuilder()
                    .setMsgSeq(message_ID)
                    .setSenderId(sender_id)
                    .setReceiverId(receiver_id)
                    .setRoleChange(SnakesProto.GameMessage.RoleChangeMsg.newBuilder()
                            .setSenderRole(sender)
                            .setReceiverRole(receiver)
                            .build())
                    .build();
            message_ID++;
            return message;
        }
    }

    public static SnakesProto.GameMessage createNewStateMsg(SnakesProto.GameState state, int receiver_id){
        synchronized (MessageCreator.class) {
                SnakesProto.GameMessage message = SnakesProto.GameMessage.newBuilder()
                        .setMsgSeq(message_ID)
                        .setReceiverId(receiver_id)
                        .setState(SnakesProto.GameMessage.StateMsg.newBuilder().setState(state).build())
                        .build();
                //добавление в очередь на отрпавку
                message_ID ++;
            return message;
            }
    }

    public static SnakesProto.GameMessage createNewJoinMsg(String name) {
        synchronized (MessageCreator.class){
            SnakesProto.GameMessage message = SnakesProto.GameMessage.newBuilder()
                    .setMsgSeq(message_ID)
                    .setJoin(SnakesProto.GameMessage.JoinMsg.newBuilder()
                            .setOnlyView(false)
                            .setPlayerType(SnakesProto.PlayerType.HUMAN)
                            .setName(name)
                            .build())
                    .build();
            message_ID++;
            return message;
        }
    }
}
