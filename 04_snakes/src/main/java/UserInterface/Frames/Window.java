package UserInterface.Frames;

import javax.swing.*;

public class Window extends JFrame {

    public Window(int sizeWidth, int sizeHeight, int locationX, int locationY){

        setTitle("Змейка");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setSize(sizeWidth, sizeHeight);
        setLocation(locationX, locationY);

        setVisible(true);
    }

}
