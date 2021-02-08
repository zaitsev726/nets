package SnakeGame;

import Global.GlobalController;
import MessageProcessing.MessageCreator;
import com.google.protobuf.InvalidProtocolBufferException;
import me.ippolitov.fit.snakes.SnakesProto;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

public class Players {
    /*
    плейрс отвечает за хранение змей и игроков
    > должен добавлять новых игроков в спискок
    > должен добавлять новых змей в список, и запрашивать их отрисовку
    > назначает роли игрокам
     */
    private static Players instance;
    private volatile List<SnakesProto.GamePlayer> queuePlayers = new ArrayList<>();
    private volatile List<SnakesProto.GamePlayer> roleChangePlayers = new ArrayList<>();
    private volatile List<SnakesProto.GameState.Snake> snakes = new ArrayList<>();
    private volatile ArrayList<SnakesProto.GamePlayer> players = new ArrayList<>();
    private volatile ArrayList<DatagramPacket> confirmJoin = new ArrayList<>();
    private int ID = 1;
    private boolean hasDeputy = false;
    private GlobalController controller;

    public static Players getInstance() {
        Players localInstance = instance;
        if (localInstance == null) {
            synchronized (Players.class) {
                localInstance = instance;
                if (localInstance == null)
                    instance = localInstance = new Players();
            }
        }
        return localInstance;
    }

    public void addNewPlayerInQueue() {
        try {
            queuePlayers.add(SnakesProto.GamePlayer.newBuilder()
                    .setName(controller.getName())
                    .setId(0)
                    .setIpAddress(InetAddress.getByName("").toString())
                    .setPort(1)
                    .setRole(SnakesProto.NodeRole.MASTER)
                    .setScore(2)
                    .build());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }


    public synchronized void addNewPlayerInQueue(SnakesProto.GameMessage message, InetAddress address, int port, long msg_seq) {
        for (DatagramPacket datagramPacket : confirmJoin) {
            if (datagramPacket.getAddress().toString().equals(address.toString()) &&
                    datagramPacket.getPort() == port)
                return;
        }

        for (SnakesProto.GamePlayer player : players) {
            if (player.getIpAddress().equals(address.toString()) &&
                    player.getPort() == port &&
                    !player.getRole().equals(SnakesProto.NodeRole.VIEWER)) {
                controller.sendAck(message.getMsgSeq(), player.getId());
                return;
            }
        }
        SnakesProto.GameMessage.JoinMsg join = message.getJoin();
        confirmJoin.add(new DatagramPacket(message.toByteArray(), message.toByteArray().length, address, port));

        SnakesProto.NodeRole role;
        if (join.getOnlyView())
            role = SnakesProto.NodeRole.VIEWER;
        else if (hasDeputy)
            role = SnakesProto.NodeRole.NORMAL;
        else {
            role = SnakesProto.NodeRole.DEPUTY;
            hasDeputy = true;
        }

        queuePlayers.add(SnakesProto.GamePlayer.newBuilder()
                .setName(join.getName())
                .setId(0)
                .setIpAddress(address.toString())
                .setPort(port)
                .setRole(role)
                .setType(join.getPlayerType())
                .setScore(2)
                .build());

        //  MessageCreator.createNewAckMsg(msg_seq,ID); ???
    }

    public boolean canJoin() {
        return GameField.getCoordForSpawn() != null;
    }

    public void updatePlayers() {
        Iterator<SnakesProto.GamePlayer> iterator = queuePlayers.iterator();
        while (iterator.hasNext()) {
            SnakesProto.GamePlayer player = iterator.next();
            int[] a = GameField.getCoordForSpawn();
            // int [] a = randomCoord
            if (a == null) {
                confirmJoin.clear();
                queuePlayers.clear();
                return;
            } else {
                player = player.toBuilder().setId(ID).build();
                players.add(player);
                createNewSnake(a);
                Iterator<DatagramPacket> iterator1 = confirmJoin.iterator();
                while (iterator1.hasNext()) {
                    DatagramPacket next = iterator1.next();
                    if (next.getAddress().toString().equals(player.getIpAddress()) &&
                            next.getPort() == player.getPort()) {
                        byte[] a1 = Arrays.copyOf(next.getData(), next.getLength());
                        SnakesProto.GameMessage message = null;
                        try {
                            message = SnakesProto.GameMessage.parseFrom(a1);
                        } catch (InvalidProtocolBufferException e) {
                            e.printStackTrace();
                        }
                        controller.sendAck(message.getMsgSeq(), ID);
                        if (player.getRole().equals(SnakesProto.NodeRole.MASTER))
                            controller.setOurId(ID);
                        iterator1.remove();
                    }
                }
                ID++;
                iterator.remove();
            }
        }
    }

    private void createNewSnake(int[] a) {
        SnakesProto.Direction direction = randomDirection();
        int[] b = tailCoord(direction);
        if (b == null)
            return;
        SnakesProto.GameState.Snake snake = SnakesProto.GameState.Snake.newBuilder()
                .setPlayerId(ID)
                .setHeadDirection(direction)
                .setState(SnakesProto.GameState.Snake.SnakeState.ALIVE)
                .addPoints(coord(a[0], a[1]))
                .addPoints(coord(b[0], b[1]))
                .build();

        snakes.add(snake);
    }

    public void updateRole(InetAddress address, int port) {
        //вопрос норм ли если челик получил список, а мы такие оп и удалили чела из него
        //а там лежит старый м м м м ?
        Iterator<SnakesProto.GamePlayer> iterator = players.iterator();
        while (iterator.hasNext()) {
            SnakesProto.GamePlayer player = iterator.next();
            if (player.getIpAddress().equals(address.toString()) &&
                    player.getPort() == port) {
                if (player.getRole().equals(SnakesProto.NodeRole.DEPUTY)) {
                    //отправляем ему что он Viewer теперь
                    MessageCreator.createNewRoleChangeMsg(
                            SnakesProto.NodeRole.MASTER,
                            SnakesProto.NodeRole.VIEWER,
                            controller.getID(),
                            player.getId());
                    hasDeputy = false;
                }
                //как выбрать нового заместителя?
                if (!roleChangePlayers.contains(player))
                    roleChangePlayers.add(player);
            }
        }
    }

    public void updateAllRoles() {
        Iterator<SnakesProto.GamePlayer> iterator = roleChangePlayers.iterator();
        while (iterator.hasNext()) {
            SnakesProto.GamePlayer player = iterator.next();

            for (SnakesProto.GamePlayer gamePlayer : players) {
                if (gamePlayer.getPort() == player.getPort() &&
                        gamePlayer.getIpAddress().equals(player.getIpAddress())) {
                    Collections.replaceAll(
                            players,
                            gamePlayer,
                            gamePlayer.toBuilder()
                                    .setRole(SnakesProto.NodeRole.VIEWER)
                                    .build());
                    int ID_player = gamePlayer.getId();
                    for (SnakesProto.GameState.Snake snake : snakes) {
                        if (snake.getPlayerId() == ID_player) {
                            Collections.replaceAll(snakes,
                                    snake,
                                    snake.toBuilder()
                                            .setState(SnakesProto.GameState.
                                                    Snake.SnakeState.ZOMBIE)
                                            .build());
                        }
                    }
                }
            }
            iterator.remove();
        }
    }

    public void setPlayers(ArrayList<SnakesProto.GamePlayer> p) {
       /* for(SnakesProto.GamePlayer player: players){
            if(!p.contains(player)){

                controller.sendRoleChange(
                        SnakesProto.NodeRole.VIEWER,
                        SnakesProto.NodeRole.MASTER,
                        player.getId());
            }
        }*/
        players = p;
    }

    public void setSnakes(List<SnakesProto.GameState.Snake> p) { snakes = p; }

    public ArrayList<SnakesProto.GamePlayer> getPlayers() {return players; }

    public List<SnakesProto.GameState.Snake> getSnakes() { return snakes; }

    private SnakesProto.GameState.Coord coord(int x, int y) {
        return SnakesProto.GameState.Coord.newBuilder().setX(x).setY(y).build();
    }

    private SnakesProto.Direction randomDirection() {
        int random = (int) (Math.random() * 4);
        SnakesProto.Direction d;
        switch (random) {
            case 0:
                d = SnakesProto.Direction.UP;
                break;
            case 1:
                d = SnakesProto.Direction.DOWN;
                break;
            case 2:
                d = SnakesProto.Direction.LEFT;
                break;
            case 3:
                d = SnakesProto.Direction.RIGHT;
                break;
            default:
                d = SnakesProto.Direction.RIGHT;
        }
        return d;
    }

    private int[] tailCoord(SnakesProto.Direction d) {
        int[] a = new int[2];
        switch (d) {
            case UP:
                a[1]++;
                return a;
            case DOWN:
                a[1]--;
                return a;
            case LEFT:
                a[0]++;
                return a;
            case RIGHT:
                a[0]--;
                return a;
        }
        return null;
    }

    public void setController(GlobalController controller) {
        this.controller = controller;
    }
    public GlobalController getController(){return controller;}

    public int getHostID() {
        for (SnakesProto.GamePlayer player : players) {
            if (player.getRole().equals(SnakesProto.NodeRole.MASTER))
                return player.getId();
        }
        return 1;
    }

    public void clearPlayers() {
        players = new ArrayList<>();
        snakes = new ArrayList<>();
        ID = 1;
    }
    public void setID(List<SnakesProto.GamePlayer> players){
        for(SnakesProto.GamePlayer gamePlayer : players){
            if(gamePlayer.getId() >= ID){
                ID = gamePlayer.getId() + 1;
            }
        }
    }
    public void selectNewDeputy(List<SnakesProto.GamePlayer> players){
        ArrayList<SnakesProto.GamePlayer> updatedPlayers = new ArrayList<>();
        for(SnakesProto.GamePlayer gamePlayer : players){
            if(gamePlayer.getRole().equals(SnakesProto.NodeRole.NORMAL) && !hasDeputy){
                hasDeputy = true;
                updatedPlayers.add(gamePlayer.toBuilder().setRole(SnakesProto.NodeRole.DEPUTY).build());
            }
            else
                updatedPlayers.add(gamePlayer);
        }
        setPlayers(updatedPlayers);
    }

    public void selectNewDeputy(List<SnakesProto.GamePlayer> players, boolean deputy){
        ArrayList<SnakesProto.GamePlayer> updatedPlayers = new ArrayList<>();
        for(SnakesProto.GamePlayer gamePlayer : players){
            if(gamePlayer.getRole().equals(SnakesProto.NodeRole.NORMAL) && !deputy){
                this.hasDeputy = true;
                deputy = true;
                updatedPlayers.add(gamePlayer.toBuilder().setRole(SnakesProto.NodeRole.DEPUTY).build());
            }
            else
                updatedPlayers.add(gamePlayer);
        }
        setPlayers(updatedPlayers);
    }
}
