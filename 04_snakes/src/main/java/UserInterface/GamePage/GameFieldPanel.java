package UserInterface.GamePage;

import javax.swing.*;
import java.awt.*;

public class GameFieldPanel extends JPanel{
    private final int DOT_SIZE = 10;

    private Image enemyBody;
    private Image enemyHead;
    private Image ourBody;
    private Image ourHead;
    private Image apple;
    private volatile int[][] gameField = null;
    private int width = 0;
    private int height = 0;
    private int ID = 0;

    public GameFieldPanel() {
        loadImages();
    }

    public void loadImages(){
        apple = new ImageIcon(this.getClass().getResource("..\\..\\apple.png")).getImage();//new ImageIcon("resources\\apple.png").getImage();
        enemyBody = new ImageIcon(this.getClass().getResource("..\\..\\enemyBody.png")).getImage();
        enemyHead = new ImageIcon(this.getClass().getResource("..\\..\\enemyHead.jpg")).getImage();
        ourBody = new ImageIcon(this.getClass().getResource("..\\..\\ourBody.png")).getImage();
        ourHead = new ImageIcon(this.getClass().getResource("..\\..\\ourHead.png")).getImage();
    }

    public synchronized void repaintField(int[][] gameField, int width, int height, int ID) {
        this.gameField = gameField;
        this.width = width;
        this.height = height;
        this.ID = ID;

        repaint();
        setFocusable(true);
        requestFocus();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if(gameField!= null) {
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {

                /*    if(gameField[i][j] == (ID + 1)){
                        g.drawImage(ourBody,i*DOT_SIZE,j*DOT_SIZE,this);
                    } else if(gameField[i][j] == (-(ID + 1))){
                        g.drawImage(ourHead,i*DOT_SIZE,j*DOT_SIZE,this);
                    } else if(gameField[i][j] == 1){
                        g.drawImage(apple, i*DOT_SIZE,j*DOT_SIZE, this);
                    }else if(gameField[i][j] > 1){
                        g.drawImage(enemyBody, i*DOT_SIZE,j*DOT_SIZE, this);
                    }else if(gameField[i][j] < -1){
                        g.drawImage(enemyHead, i*DOT_SIZE,j*DOT_SIZE, this);
                    }
                }*/

                    if (gameField[i][j] == 1) {
                        g.drawImage(apple, i * DOT_SIZE, j * DOT_SIZE, this);
                    } else if (gameField[i][j] > 1) {
                        if (gameField[i][j] == (ID + 1))
                            g.drawImage(ourBody, i * DOT_SIZE, j * DOT_SIZE, this);
                        else
                            g.drawImage(enemyBody, i * DOT_SIZE, j * DOT_SIZE, this);
                    } else if (gameField[i][j] < -1) {
                        if (gameField[i][j] == (-(ID + 1)))
                            g.drawImage(ourHead, i * DOT_SIZE, j * DOT_SIZE, this);
                        else
                            g.drawImage(enemyHead, i * DOT_SIZE, j * DOT_SIZE, this);
                    }
                }
            }
        }

    }

    public int getDOT_SIZE(){return  DOT_SIZE;}
}