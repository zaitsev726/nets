import java.net.InetAddress;
import java.util.Date;

public class User {
    private InetAddress IP;
    private int PORT;
    private String name;

    private  InetAddress IP_substitute;
    private  int PORT_substitute;

    private Date lastPing;

    public InetAddress getIP_substitute() {
        return IP_substitute;
    }

    public void setIP_substitute(InetAddress IP_substitute) {
        this.IP_substitute = IP_substitute;
    }

    public int getPORT_substitute() {
        return PORT_substitute;
    }

    public void setPORT_substitute(int PORT_substitute) {
        this.PORT_substitute = PORT_substitute;
    }


    public InetAddress getIP() {
        return IP;
    }

    public void setLastPing(Date lastPing) {
        this.lastPing = lastPing;
    }

    public Date getLastPing() {
        return lastPing;
    }

    public int getPORT() {
        return PORT;
    }

    public User(InetAddress ip, int port){
        IP = ip;
        PORT = port;
        IP_substitute = null;
        PORT_substitute = 0;
        name = "";
        lastPing = new Date();
    }

    public String getName() {
        return name;
    }
}
