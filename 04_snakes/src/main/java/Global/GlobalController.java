package Global;

import MessageProcessing.MessageCreator;
import NetworkPart.Broadcast.MulticastController;
import NetworkPart.NetSocket.SteerMsgQueue;
import NetworkPart.NetworkController;
import SnakeGame.CurrentLobby;
import SnakeGame.GameController;
import SnakeGame.Players;
import UserInterface.InterfaceController;
import com.google.protobuf.InvalidProtocolBufferException;
import me.ippolitov.fit.snakes.SnakesProto;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GlobalController{
    private InterfaceController interfaceController;
    private GameController gameController;
    private ChangestateController stateController = null;
    private MulticastController multicastController;
    private NetworkController networkController;
    private CurrentLobby lobby;

    private int width = 40;
    private int height = 30;
    private int foodStatic = 1;
    private float foodPerPlayer = (float) 1.0;
    private int stateDelay = 1000;
    private float deadFoodProb = (float) 0.1;
    private int pingDelay = 100;
    private int nodeTimeout = 800;
    private volatile SnakesProto.GameState state = null;
    private String name = "default";

    public void setWidth(int width) { if(width!=0) this.width = width; }
    public void setHeight(int height) { if(height != 0) this.height = height; }
    public void setFoodStatic(int foodStatic) { if(foodStatic != 0) this.foodStatic = foodStatic; }
    public void setFoodPerPlayer(float foodPerPlayer) { if(foodPerPlayer!= 0.0) this.foodPerPlayer = foodPerPlayer; }
    public void setStateDelay(int stateDelay) { if(stateDelay != 0) this.stateDelay = stateDelay; }
    public void setDeadFoodProb(float deadFoodProb) { if(deadFoodProb!= 0.0) this.deadFoodProb = deadFoodProb; }
    public void setPingDelay(int pingDelay) { if(pingDelay != 0) this.pingDelay = pingDelay; }
    public void setNodeTimeout(int nodeTimeout) { if(nodeTimeout != 0) this.nodeTimeout = nodeTimeout; }
    public int getWidth() {
        return width;
    }
    public int getHeight() {
        return height;
    }
    public int getStateDelay(){return stateDelay;}
    public int getPingDelay(){return pingDelay;}
    public int getNodeTimeout(){return nodeTimeout;}

    public void setName(String name) {
        this.name = name;
    }

    public boolean getMaster() {
        return lobby.isMaster();
    }

    public String getHostIP() {
        return lobby.getHost_IP();
    }

    public int getHostPort() {
        return lobby.getHost_port();
    }

    public GlobalController() {
        Players.getInstance().setController(this);
        interfaceController = new InterfaceController(this);
        gameController = null;
        networkController = new NetworkController(this);
        multicastController = new MulticastController(networkController.getIP(), interfaceController);
        multicastController.start();
    }

    public void removeGame() {

        Players.getInstance().clearPlayers();
        stateController.interrupt();
        if (gameController != null) {
            gameController.clearField();
        }
        gameController = null;
    }
    public void masterDead(SnakesProto.GamePlayer deputy){
        //gameController.clearField();
        int ID = lobby.getOur_ID();
        lobby = new CurrentLobby(deputy.getIpAddress(), deputy.getPort(), false);
        lobby.setOur_ID(ID);
        networkController.deleteOldStates();//
        stateController.interrupt();
        networkController.deleteOldStates();//
        stateController = new ChangestateController(this,
                this.state,
                interfaceController, stateDelay);
        stateController.start();
    }

    public void exitSM(){
        if (lobby.isMaster()) {
            for (SnakesProto.GamePlayer player : Players.getInstance().getPlayers()) {
                if (player.getRole().equals(SnakesProto.NodeRole.DEPUTY)) {
                    networkController.sendNewMessage(MessageCreator.createNewRoleChangeMsg(
                            SnakesProto.NodeRole.NORMAL,
                            SnakesProto.NodeRole.DEPUTY,
                            lobby.getOur_ID(),
                            player.getId()));

                    masterDead(player);
                }
            }
        } else
            networkController.sendNewMessage(MessageCreator.createNewRoleChangeMsg(
                    SnakesProto.NodeRole.VIEWER,
                    SnakesProto.NodeRole.MASTER,
                    lobby.getOur_ID(),
                    lobby.getHost_ID()));
    }
    public void initializationGame() {

        lobby = new CurrentLobby(true);
        Players.getInstance().addNewPlayerInQueue();
        gameController = new GameController(width, height, foodStatic, foodPerPlayer,
                stateDelay, deadFoodProb, pingDelay, nodeTimeout, this, 1);

        stateController = new ChangestateController(this,
                gameController.makeNextState(),
                interfaceController, stateDelay);
        stateController.start();
    }

    public void initializationConnect(DatagramPacket dp) {
        byte[] a1 = Arrays.copyOf(dp.getData(), dp.getLength());
        SnakesProto.GameMessage.AnnouncementMsg message = null;
        try {
            message = SnakesProto.GameMessage.parseFrom(a1).getAnnouncement();
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
        width = message.getConfig().getWidth();
        height = message.getConfig().getHeight();
        foodStatic = message.getConfig().getFoodStatic();
        foodPerPlayer = message.getConfig().getFoodPerPlayer();
        stateDelay = message.getConfig().getStateDelayMs();
        deadFoodProb = message.getConfig().getDeadFoodProb();
        pingDelay = message.getConfig().getPingDelayMs();
        nodeTimeout = message.getConfig().getNodeTimeoutMs();

        lobby = new CurrentLobby(dp.getAddress().toString(), dp.getPort(), false);
        networkController.sendNewMessage(MessageCreator.createNewJoinMsg(name));
        stateController = new ChangestateController(this, getNextState(),
                interfaceController, message.getConfig().getStateDelayMs());

        stateController.start();
    }

    public synchronized SnakesProto.GameState getNextState() {

        synchronized (GlobalController.class) {
            if (lobby.isMaster()) {
                state = gameController.makeNextState();
                networkController.deleteAnnouncementMsg();
                networkController.sendNewMessage(MessageCreator.createNewAnnouncementMsg(
                        state.getPlayers(),
                        state.getConfig(),
                        Players.getInstance().canJoin()
                ));
            }

            return state;

        }
    }

    public void errorMessage(SnakesProto.GameMessage.ErrorMsg error) {
        interfaceController.showMessage(error.getErrorMessage());
    }

    public void setState(SnakesProto.GameState state) {
        synchronized (GlobalController.class) {
            if (this.state == null)
                this.state = state;

            if (this.state.getStateOrder() < state.getStateOrder()) {
                this.state = state;
            }
        }
    }

    public void sendState(SnakesProto.GameState state) {
        networkController.deleteOldStates();
        ArrayList<SnakesProto.GamePlayer> players = Players.getInstance().getPlayers();
        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).getId() != lobby.getOur_ID())
                networkController.sendNewMessage(MessageCreator.createNewStateMsg(state, players.get(i).getId()));
        }
    }

    public void sendAck(long msg_seq, int receiver_ID) {
        networkController.sendNewMessage(MessageCreator.createNewAckMsg(msg_seq, receiver_ID));
    }

    public void sendSteer(SnakesProto.Direction direction) {
        if (lobby.isMaster()) {
            try {
                SteerMsgQueue.getInstance().addNewDirection(
                        SnakesProto.GameMessage.SteerMsg.newBuilder().setDirection(direction)
                                .build(),
                        InetAddress.getByName(""),
                        1);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        } else {
            networkController.sendNewMessage(MessageCreator.createNewSteerMsg(direction));
        }
    }

    public void setOurId(int receiverId) {
        lobby.setOur_ID(receiverId);
    }

    public int getID() {
        return lobby.getOur_ID();
    }

    public void sendRoleChange() {
        if (lobby.isMaster()) {
            for (SnakesProto.GamePlayer player : Players.getInstance().getPlayers()) {
                if (player.getRole().equals(SnakesProto.NodeRole.DEPUTY))
                    networkController.sendNewMessage(MessageCreator.createNewRoleChangeMsg(
                            SnakesProto.NodeRole.VIEWER,
                            SnakesProto.NodeRole.DEPUTY,
                            lobby.getOur_ID(),
                            player.getId()));
            }
        } else
            networkController.sendNewMessage(MessageCreator.createNewRoleChangeMsg(
                    SnakesProto.NodeRole.VIEWER,
                    SnakesProto.NodeRole.MASTER,
                    lobby.getOur_ID(),
                    lobby.getHost_ID()));
    }

    public void sendRoleChange(SnakesProto.NodeRole receiverRole,
                               SnakesProto.NodeRole senderRole,
                               int receiverId) {
        networkController.sendNewMessage(MessageCreator.createNewRoleChangeMsg(
                senderRole,
                receiverRole,
                lobby.getOur_ID(),
                receiverId));

    }

    public void setHostID(List<SnakesProto.GamePlayer> playersList) {
        for (SnakesProto.GamePlayer player : playersList) {
            if (player.getRole().equals(SnakesProto.NodeRole.MASTER)) {
                if (lobby != null) {
                    lobby.setHost_ID(player.getId());
                }
            }
        }
    }

    public void setHostIP(String IP) {
        lobby.setHost_IP(IP);
    }

    public void setHostPort(int port) {
        lobby.setHost_port(port);
    }

    public int getHostID() {
        return lobby.getHost_ID();
    }
    public String getName(){return name;}

    public void updateGame(SnakesProto.GameState state, boolean deleteMaster) {
        synchronized (GlobalController.class) {
            boolean isMaterDead = false;
            SnakesProto.GamePlayer player = null;
            if (state == null) {
                state = this.state;
                isMaterDead = true;
            }

            for (SnakesProto.GamePlayer p : state.getPlayers().getPlayersList()) {
                if (p.getId() == lobby.getOur_ID()) {
                    player = p;
                }
            }

            if (player.getRole().equals(SnakesProto.NodeRole.DEPUTY)) {
                ArrayList<SnakesProto.GamePlayer> players = new ArrayList<>();
                ArrayList<SnakesProto.GameState.Snake> snakes = new ArrayList<>(state.getSnakesList());
                for (SnakesProto.GamePlayer gamePlayer : state.getPlayers().getPlayersList()) {
                    if (gamePlayer.getRole().equals(SnakesProto.NodeRole.DEPUTY)) {
                        try {
                            players.add(gamePlayer.toBuilder()
                                    .setRole(SnakesProto.NodeRole.MASTER)
                                    .setPort(1)
                                    .setIpAddress(InetAddress.getByName("").toString())
                                    .build());
                        } catch (UnknownHostException e) {
                            e.printStackTrace();
                        }
                    } else if (gamePlayer.getRole().equals(SnakesProto.NodeRole.MASTER)) {
                        players.add(gamePlayer.toBuilder()
                                .setRole(SnakesProto.NodeRole.VIEWER)
                                .setIpAddress(lobby.getHost_IP())
                                .setPort(lobby.getHost_port())
                                .build());
                        for (SnakesProto.GameState.Snake snake : state.getSnakesList()) {
                            if (snake.getPlayerId() == gamePlayer.getId()) {
                                snakes.remove(snake);
                                if(!isMaterDead || !deleteMaster)
                                    snakes.add(snake.toBuilder().setState(SnakesProto.GameState.Snake.SnakeState.ZOMBIE).build());
                            }
                        }
                    } else
                        players.add(gamePlayer);
                }
                Players.getInstance().setPlayers(players);
                Players.getInstance().setSnakes(snakes);
                Players.getInstance().setID(players);
                Players.getInstance().selectNewDeputy(players);//
                SnakesProto.GameState.Builder currentState = SnakesProto.GameState.newBuilder();

                currentState.setConfig(state.getConfig());
                SnakesProto.GamePlayers.Builder playersBuilder = SnakesProto.GamePlayers.newBuilder();
                for(SnakesProto.GamePlayer gamePlayer : players){
                    playersBuilder.addPlayers(gamePlayer);
                }
                for(SnakesProto.GameState.Snake snake : snakes){
                    currentState.addSnakes(snake);
                }
                List<SnakesProto.GameState.Coord> foods = state.getFoodsList();
                for(SnakesProto.GameState.Coord coord : foods){
                    currentState.addFoods(coord);
                }
                currentState.setPlayers(playersBuilder.build());
                currentState.setStateOrder(state.getStateOrder());
                this.state = currentState.build();

                lobby = new CurrentLobby(true);
                lobby.setOur_ID(player.getId());
                lobby.setHost_ID(player.getId());
                gameController = new GameController(width, height, foodStatic, foodPerPlayer,
                        stateDelay, deadFoodProb, pingDelay, nodeTimeout, this, state.getStateOrder());

                stateController = new ChangestateController(this,
                        state,
                        interfaceController, stateDelay);

            }
        }
    }

    public boolean inGame() {
        return lobby != null;
    }
}
