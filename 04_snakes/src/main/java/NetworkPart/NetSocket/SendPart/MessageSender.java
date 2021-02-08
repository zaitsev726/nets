package NetworkPart.NetSocket.SendPart;

import Global.GlobalController;
import MessageProcessing.MessageCreator;
import SnakeGame.Players;
import me.ippolitov.fit.snakes.SnakesProto;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class MessageSender extends Thread {
    private DatagramSocket socket;
    private GlobalController controller;
    private ResendQueue resend;
    private ImmediateQueue queue;
    private int minToResend;        //минимальное время до отправки пакета из resend очереди
    private int ping_delay_ms;      //повторное отправление

    private ArrayList<SnakesProto.GamePlayer> currentPlayers;

    public MessageSender(DatagramSocket socket, GlobalController controller, ImmediateQueue queue, ResendQueue resendQueue) {
        this.socket = socket;
        this.queue = queue;
        this.ping_delay_ms = controller.getPingDelay(); //default value
        this.controller = controller;

        resend = resendQueue;
        minToResend = 10000;
    }

    public void setPing_delay_ms(int ping_delay_ms) {
        this.ping_delay_ms = ping_delay_ms;
    }

    @Override
    public void run() {
        Date lastMessage = new Date();
        while (true) {
                currentPlayers = (ArrayList<SnakesProto.GamePlayer>)Players.getInstance().getPlayers().clone();
                Date date = new Date();
                LinkedBlockingQueue q = queue.getQ();

                try {
                    SnakesProto.GameMessage message = (SnakesProto.GameMessage) q.poll(minToResend, TimeUnit.MILLISECONDS);

                    if (message != null) {
                        lastMessage = new Date();
                        switch (message.getTypeCase()) {
                            case PING:

                                break;
                            case STEER:
                                sendSteer(message);
                                break;
                            case ACK:
                                sendAck(message);
                                break;
                            case STATE:
                                sendState(message);
                                resend.addNewResendMessage(message);
                                break;
                            case ANNOUNCEMENT:
                                sendAnnouncementMsg(message);
                                resend.addNewResendMessage(message);
                                break;
                            case JOIN:
                                sendJoin(message);
                                resend.addNewResendMessage(message);
                                break;
                            case ERROR:
                                resend.addNewResendMessage(message);
                                break;
                            case ROLE_CHANGE:
                                sendRoleChange(message);
                                resend.addNewResendMessage(message);
                                break;
                        }
                        minToResend = ping_delay_ms - (int) resend.getMinToResend();
                    }

                } catch (InterruptedException | NullPointerException e) {
                    e.printStackTrace();
                }

                synchronized (ResendQueue.class) {
                    ConcurrentHashMap<SnakesProto.GameMessage, Date> map = resend.getResendQueue();
                    for (Map.Entry<SnakesProto.GameMessage, Date> next : map.entrySet()) {
                        if ((date.getTime() - next.getValue().getTime()) > ping_delay_ms) {
                            lastMessage = new Date();
                            switch (next.getKey().getTypeCase()) {
                                case ANNOUNCEMENT:
                                    sendAnnouncementMsg(next.getKey());
                                    break;
                                case JOIN:
                                    sendJoin(next.getKey());
                                    break;
                                case STATE:
                                    sendState(next.getKey());
                                    default:
                            }
                            resend.addNewResendMessage(next.getKey());
                        }
                    }
                }
                 if ((new Date()).getTime() - lastMessage.getTime() > ping_delay_ms) {
                     if(controller.inGame()) {
                         sendPing(MessageCreator.createNewPing());
                         lastMessage = new Date();
                     }
                }
            }

    }

    public void sendPing(SnakesProto.GameMessage message) {

        try {
            if (controller.getMaster()) {
                for (SnakesProto.GamePlayer gamePlayer : currentPlayers) {
                    if(!gamePlayer.getRole().equals(SnakesProto.NodeRole.VIEWER) &&
                        !gamePlayer.getRole().equals(SnakesProto.NodeRole.MASTER))
                    socket.send(new DatagramPacket(message.toByteArray(), message.toByteArray().length,
                            InetAddress.getByName(gamePlayer.getIpAddress().substring(1)),
                            gamePlayer.getPort()));
                }
            }
            else{
                for (SnakesProto.GamePlayer gamePlayer : currentPlayers) {
                    if(gamePlayer.getRole().equals(SnakesProto.NodeRole.MASTER))
                        if (!gamePlayer.getIpAddress().equals("localhost/127.0.0.1"))
                            socket.send(new DatagramPacket(message.toByteArray(), message.toByteArray().length,
                                    InetAddress.getByName(gamePlayer.getIpAddress().substring(1)),
                                    gamePlayer.getPort()));
                        else
                            socket.send(new DatagramPacket(message.toByteArray(), message.toByteArray().length,
                                InetAddress.getLocalHost(),
                                gamePlayer.getPort()));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendSteer(SnakesProto.GameMessage message) {
        //отправляем мастеру смс о повроте узла
        DatagramPacket dp = null;
        try {
            if (!controller.getHostIP().equals("localhost/127.0.0.1"))
                dp = new DatagramPacket(message.toByteArray(), message.toByteArray().length,
                        InetAddress.getByName(controller.getHostIP().substring(1)),
                        controller.getHostPort());
            else
                dp = new DatagramPacket(message.toByteArray(), message.toByteArray().length,
                        InetAddress.getLocalHost(),
                        controller.getHostPort());
            socket.send(dp);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void sendAck(SnakesProto.GameMessage message) {
        //отправляем мастеру подтверждение его сообщения
        DatagramPacket dp = null;
        try {
            for (SnakesProto.GamePlayer currentPlayer : currentPlayers) {
                //как то узнаем что мы хост
                if (currentPlayer.getId() == message.getReceiverId()) {
                    if (!currentPlayer.getIpAddress().equals("localhost/127.0.0.1"))
                        dp = new DatagramPacket(message.toByteArray(), message.toByteArray().length,
                                InetAddress.getByName(currentPlayer.getIpAddress().substring(1)),
                                currentPlayer.getPort());
                    else
                        dp = new DatagramPacket(message.toByteArray(), message.toByteArray().length,
                                InetAddress.getLocalHost(),
                                currentPlayer.getPort());
                    socket.send(dp);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendState(SnakesProto.GameMessage message) {
        DatagramPacket dp = null;
        try {
            for (SnakesProto.GamePlayer currentPlayer : currentPlayers) {
                //как то узнаем что мы хост
                if (controller.getHostID() == 0) {
                    controller.setHostID(currentPlayers);
                }
                if (currentPlayer.getId() != controller.getHostID()) {
                    if (!currentPlayer.getIpAddress().equals("localhost/127.0.0.1"))
                        dp = new DatagramPacket(message.toByteArray(), message.toByteArray().length,
                                InetAddress.getByName(currentPlayer.getIpAddress().substring(1)),
                                currentPlayer.getPort());
                    else
                        dp = new DatagramPacket(message.toByteArray(), message.toByteArray().length,
                                InetAddress.getLocalHost(),
                                currentPlayer.getPort());
                   /* dp = new DatagramPacket(message.toByteArray(), message.toByteArray().length,
                            InetAddress.getByName(currentPlayer.getIpAddress().substring(1)),
                            currentPlayer.getPort());*/

                    socket.send(dp);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendJoin(SnakesProto.GameMessage message) {
        DatagramPacket dp = null;
        try {
            dp = new DatagramPacket(message.toByteArray(), message.toByteArray().length,
                    InetAddress.getByName(controller.getHostIP().substring(1)), controller.getHostPort());

            socket.send(dp);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendRoleChange(SnakesProto.GameMessage message) {
        DatagramPacket dp = null;
        try {
            for (SnakesProto.GamePlayer player : currentPlayers) {
                if (player.getId() == message.getReceiverId()) {
                    if (!player.getRole().equals(SnakesProto.NodeRole.MASTER))
                        dp = new DatagramPacket(message.toByteArray(), message.toByteArray().length,
                                InetAddress.getByName(player.getIpAddress().substring(1)),
                                player.getPort());
                    else
                        dp = new DatagramPacket(message.toByteArray(), message.toByteArray().length,
                                InetAddress.getByName(controller.getHostIP().substring(1)),
                                controller.getHostPort());
                    socket.send(dp);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void sendAnnouncementMsg(SnakesProto.GameMessage message) {
        try {
            DatagramPacket dp = new DatagramPacket(message.toByteArray(), message.toByteArray().length,
                    InetAddress.getByName("239.192.0.4"), 9192);
            socket.send(dp);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
