package NetworkPart.Broadcast;


import UserInterface.InterfaceController;

import java.io.IOException;
import java.net.*;

public class MulticastController extends Thread {
    private final int multicastPort = 9192;
    private InetAddress multicastAddress;
    private InterfaceController interfaceController;
    private MulticastSocket multicastSocket;

    public MulticastController(InetAddress inetInterface, InterfaceController interfaceController){
        try {
            this.interfaceController = interfaceController;
            multicastAddress =  InetAddress.getByName("239.192.0.4");
            multicastSocket = new MulticastSocket(multicastPort);
            multicastSocket.setInterface(inetInterface);
            multicastSocket.setSoTimeout(1000);
            multicastSocket.joinGroup(multicastAddress);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void run() {
        byte[] buf = new byte[4096];
        DatagramPacket dp = new DatagramPacket(buf,buf.length);
        while(true){
            try {
                multicastSocket.receive(dp);
                interfaceController.addNewConnectButton(dp);
                interfaceController.removeButton();

            }catch (SocketTimeoutException e){
                interfaceController.removeButton();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            catch (Exception e){
            }
        }
    }
}
