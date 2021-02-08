package SnakeGame;

public class Event {
    private int x;
    private int y;
    private int host_head;

    public Event (int x,int y,int head){
        this.x = x;
        this.y = y;
        host_head = head;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getHost_head() {
        return host_head;
    }
}
