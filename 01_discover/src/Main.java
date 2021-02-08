public class Main {

    private static final int PORT = 5555;
    private static final String GROUP_IP = "224.5.6.7";//"ff02::137";

    public static void main(String[] args) {
        Sender sender = new Sender(PORT, GROUP_IP);
        sender.start();
        Receiver receiver = new Receiver(PORT, GROUP_IP);
        receiver.start();
    }
}
