package NetworkPart.NetSocket;

import Global.GlobalController;
import MessageProcessing.MessageHandler;
import NetworkPart.NetSocket.SendPart.ResendQueue;
import me.ippolitov.fit.snakes.SnakesProto;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

public class MessageReceiver extends Thread{
    private static final int BUF_SIZE = 4096;
    private DatagramSocket socket;
    private MessageHandler handler;
    private GlobalController controller;
    private ResendQueue resend;
    private ConcurrentHashMap<SnakesProto.GamePlayer, Date> lastMessage;
    public MessageReceiver(DatagramSocket socket, GlobalController controller, ResendQueue resend){
        this.socket = socket;
        this.controller = controller;
        this.resend = resend;
        lastMessage = new ConcurrentHashMap<>();
        handler = new MessageHandler(controller,resend);
    }
    public ConcurrentHashMap<SnakesProto.GamePlayer, Date> getLastMessage() {
        return lastMessage;
    }

    @Override
    public void run() {
        try {
            byte[] buf = new byte[BUF_SIZE];
            DatagramPacket dp = new DatagramPacket(buf, buf.length);
            while (true) {
                socket.receive(dp);
                byte[] a1 = Arrays.copyOf(dp.getData(), dp.getLength());

                SnakesProto.GameMessage message = SnakesProto.GameMessage.parseFrom(a1);
                SnakesProto.GamePlayer player = SnakesProto.GamePlayer.newBuilder()
                        .setName("")
                        .setId(0)
                        .setIpAddress(dp.getAddress().toString())
                        .setPort(dp.getPort())
                        .setRole(SnakesProto.NodeRole.NORMAL)
                        .setScore(0)
                        .build();
                lastMessage.put(player,new Date());
                handler.handlingMessage(message, dp.getAddress(), dp.getPort(),message.getMsgSeq());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
