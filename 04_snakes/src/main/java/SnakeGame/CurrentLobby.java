package SnakeGame;

public class CurrentLobby {
    private String host_IP;
    private int host_port;
    private boolean master = false;
    private int our_ID;
    private int host_ID;

    public CurrentLobby(boolean master){
        this.master = master;
        this.host_IP = "";
        this.host_port = 0;
        this.our_ID = 1;
    }

    public CurrentLobby(String host_IP, int host_port, boolean master){
        this.host_IP = host_IP;
        this.host_port = host_port;
        this.master = master;
        this.our_ID = 0;
    }

    public String getHost_IP() { return host_IP; }
    public int getHost_port() { return host_port; }
    public boolean isMaster() { return master; }
    public int getHost_ID() {return host_ID;}

    public void setHost_IP(String host_IP) {
        this.host_IP = host_IP;
    }

    public void setHost_port(int host_port) {
        this.host_port = host_port;
    }

    public void setHost_ID(int id){this.host_ID = id;}
    public void setOur_ID(int id){this.our_ID = id; }
    public int getOur_ID(){return our_ID;}
}
