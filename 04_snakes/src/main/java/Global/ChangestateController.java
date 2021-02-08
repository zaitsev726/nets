package Global;

import SnakeGame.GameField;
import UserInterface.InterfaceController;
import me.ippolitov.fit.snakes.SnakesProto;

import java.util.Date;

public class ChangestateController extends Thread{
    private GlobalController controller;
    private SnakesProto.GameState state;
    private InterfaceController interfaceController;
    private int delay;
    private SnakesProto.GameState lastState;
    private Date lastDate;
    public ChangestateController(GlobalController controller, SnakesProto.GameState state,
                                 InterfaceController interfaceController, int delay){
        this.controller = controller;
        this.state = state;
        this.interfaceController = interfaceController;
        this.delay = delay;

    }

    @Override
    public void run() {
        if(state!= null) {
            interfaceController.repaintField(GameField.updateField(state),
                    state.getConfig().getWidth(),
                    state.getConfig().getHeight(),
                    controller.getID(),
                    state.getPlayers().getPlayersList());
            lastState = state;
            lastDate = new Date();
        }
        try {
            lastDate = new Date();
            while (true) {
              //  Thread.sleep(delay);
                    Thread.sleep(100);
                state = controller.getNextState();

                if (state != null) {
                    interfaceController.repaintField(GameField.updateField(state),
                            state.getConfig().getWidth(),
                            state.getConfig().getHeight(),
                            controller.getID(),
                            state.getPlayers().getPlayersList());

                        if (lastState == null || state.getStateOrder() > lastState.getStateOrder()) {
                            lastState = state;
                            lastDate = new Date();
                        }

                    }
            }
        }catch (InterruptedException e) {

        }
    }

}
