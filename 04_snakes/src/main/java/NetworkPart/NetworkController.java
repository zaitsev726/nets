package NetworkPart;

import Global.GlobalController;
import MessageProcessing.MessageManagement;
import NetworkPart.NetSocket.MessageReceiver;
import NetworkPart.NetSocket.SendPart.ImmediateQueue;
import NetworkPart.NetSocket.SendPart.MessageSender;
import NetworkPart.NetSocket.SendPart.ResendQueue;
import me.ippolitov.fit.snakes.SnakesProto;

import java.net.*;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

public class NetworkController {
    private int port;
    private InetAddress IP;

    private DatagramSocket socket;
    private GlobalController controller;
    private MessageReceiver receiver;
    private MessageSender sender;
    private MessageManagement management;
    private ImmediateQueue queue;
    private ResendQueue resendQueue;

    public NetworkController(GlobalController globalController){
        this.controller = globalController;
        this.queue = new ImmediateQueue();
        this.resendQueue = new ResendQueue(controller);
        try {
            IP = getLocalAddress();
        } catch (UnknownHostException | SocketException e) {
            e.printStackTrace();
        }
        try {
            socket = new DatagramSocket(port, IP);
        } catch (SocketException e) {
            e.printStackTrace();
        }

        receiver = new MessageReceiver(socket, controller,resendQueue);
        management = new MessageManagement(globalController.getNodeTimeout(),receiver,controller);
        sender = new MessageSender(socket, controller,queue, resendQueue);

        receiver.start();
        sender.start();
        management.start();
    }

    public void setPort(int port){
        if(port >= 1024 && port <= 49151) {
            this.port = port;
        }
    }

    public int getPort(){
        return port;
    }
    public InetAddress getIP(){return IP;}

    public void sendNewMessage(SnakesProto.GameMessage message){

        queue.addNewMessage(message);
    }
    public void deleteAnnouncementMsg(){resendQueue.deleteAnnouncementMsg();}
    public void deleteOldStates(){resendQueue.deleteOldStates();}
    private InetAddress getLocalAddress() throws UnknownHostException, SocketException {
        List<NetworkInterface> netInts = Collections.list(NetworkInterface.getNetworkInterfaces());

        // there is a simple method, but it works sometimes
        // incorrectly when there are several network interfaces
        if (netInts.size() == 1) {
            return InetAddress.getLocalHost();
        }

        for (NetworkInterface net : netInts) {
            if (!net.isLoopback() && !net.isVirtual() && net.isUp()) {
                Enumeration<InetAddress> addrEnum = net.getInetAddresses();
                while (addrEnum.hasMoreElements()) {
                    InetAddress addr = addrEnum.nextElement();
                    // filter out addresses, which cannot be considered as the main address
                    // and return the first suitable address
                    if ( !addr.isLoopbackAddress() && !addr.isAnyLocalAddress()
                            && !addr.isLinkLocalAddress() && !addr.isMulticastAddress()
                    ) {
                        return addr;
                    }
                }
            }
        }
        // we can fall here if there are no suitable addresses/interfaces
        // or we don't have enough permissions
        return null;
    }
}
