import java.net.InetAddress;

public class Packet {

    private InetAddress IP;
    private int port;
    private int ID;
    private byte[] data;

    public Packet(InetAddress IP, int port, int ID){
        this.IP = IP;
        this.port = port;
        this.ID = ID;

    }

    public byte[] getData() {
        return data;
    }

    public Packet(InetAddress IP, int port, int ID, byte[] data){
        this.IP = IP;
        this.port = port;
        this.ID = ID;
        this.data = data;

    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Packet) {
            Packet m = (Packet) obj;

            return m.getID() == this.ID &&
                    m.getPort() == this.getPort() &&
                    m.getIP().toString().equals(this.getIP().toString());
        }
        return false;
    }

    public InetAddress getIP() {
        return IP;
    }

    public int getPort() {
        return port;
    }

    public int getID() {
        return ID;
    }

}
