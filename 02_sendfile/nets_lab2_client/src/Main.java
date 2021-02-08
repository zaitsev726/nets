import java.net.InetAddress;

public class Main{

    public static void main(String[] args)throws Exception {
        System.out.println(InetAddress.getLocalHost());
        ClientSocket cl = new ClientSocket("10.9.72.159", 5555,"/Users/arski/Desktop/MoP.MP3");
        cl.start();
        //ClientSocket cl2 = new ClientSocket("10.9.75.66", 5555,"/Users/arski/Desktop/MoP.MP3");
        //cl2.start();
        //ClientSocket cl3 = new ClientSocket("10.9.75.66", 5555,"/Users/arski/Desktop/hey.txt");
        //cl3.start();
    }
}