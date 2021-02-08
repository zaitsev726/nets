package SnakeGame;

import Global.GlobalController;
import me.ippolitov.fit.snakes.SnakesProto;

import java.util.ArrayList;

public class GameController {
    private GameLogic logic;
    private GameField field;
    private int delay;
    private int pingDelay;
    private int nodeTimeout;
    private GlobalController controller;
    public GameController(int width, int height, int foodStatic, float foodPerPlayer,
                          int delay, float deadFoodProb, int pingDelay, int nodeTimeout,
                          GlobalController controller, int stateOrder) {
        field = new GameField(width,height);
        logic = new GameLogic(width,height,foodStatic,foodPerPlayer,deadFoodProb, stateOrder);
        this.delay = delay;
        this.pingDelay = pingDelay;
        this.nodeTimeout = nodeTimeout;
        this.controller = controller;
    }

    public SnakesProto.GameState makeNextState(){

        logic.makeNextStep();
        SnakesProto.GameState state = logic.createNewState(delay,pingDelay,nodeTimeout);
            controller.sendState(state);
        /*
        отправка стейта всем
         */
        return state;
    }

    public void clearField(){

        field = new GameField(0,0);
        logic = new GameLogic(0,0,0,0,0,1);
        Players.getInstance().setPlayers(new ArrayList<SnakesProto.GamePlayer>());
        Players.getInstance().setSnakes(new ArrayList<SnakesProto.GameState.Snake>());
        delay = 0;
        nodeTimeout = 0;
    }
}
