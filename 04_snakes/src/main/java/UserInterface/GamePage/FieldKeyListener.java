package UserInterface.GamePage;

import Global.GlobalController;
import me.ippolitov.fit.snakes.SnakesProto;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class FieldKeyListener extends KeyAdapter {
    GameFieldPanel panel;
    GlobalController controller;
    public FieldKeyListener(GameFieldPanel p, GlobalController controller){
        panel = p;
        this.controller = controller;
    }

    @Override
    public void keyPressed(KeyEvent e) {
            SnakesProto.Direction direction = null;
            super.keyPressed(e);
            int key = e.getKeyCode();
            if (key == KeyEvent.VK_LEFT) {
                direction = SnakesProto.Direction.LEFT;
            } else if (key == KeyEvent.VK_RIGHT) {
                direction = SnakesProto.Direction.RIGHT;
            } else if (key == KeyEvent.VK_DOWN) {
                direction = SnakesProto.Direction.DOWN;
            } else if (key == KeyEvent.VK_UP) {
                direction = SnakesProto.Direction.UP;
            }
            if (direction != null)
                controller.sendSteer(direction);

    }
}