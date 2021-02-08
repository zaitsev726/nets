package UserInterface.GamePage;

import Global.GlobalController;
import UserInterface.Layouts.VerticalLayout;
import me.ippolitov.fit.snakes.SnakesProto;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GamePanel extends JPanel {
    public GameFieldPanel gameField;
    public JPanel scorePanel;
    public JButton backButton;
    public JButton viewModButton;

    private JPanel chatPanel;
    private JPanel currentGamePanel;
    private HashMap<SnakesProto.GamePlayer, JLabel> score;
    public GamePanel(GlobalController controller) {
        this.setLayout(null);
        score = new HashMap<>();
        gameField = new GameFieldPanel();
        backButton = new JButton("BACK");
        viewModButton = new JButton("VIEW MOD");

        chatPanel = new JPanel();
        scorePanel = new JPanel();
        currentGamePanel = new JPanel();


        JScrollPane scorePane = new JScrollPane(scorePanel);
        JScrollPane chatPane = new JScrollPane(chatPanel);
        JScrollPane currentPane = new JScrollPane(currentGamePanel);

        scorePane.setBounds(1010, 5, 490, 225);
        currentPane.setBounds(1010,235, 245,115);
        backButton.setBounds(1260, 235, 240, 115);
        viewModButton.setBounds(1010, 355, 490,115);
        chatPane.setBounds(1010, 475, 490, 530);


        scorePanel.setBackground(Color.green);
        gameField.setBackground(Color.black);
        chatPanel.setBackground(Color.CYAN);

        scorePanel.setLayout(new VerticalLayout(465, 25));
     /*   scorePanel.add(new JLabel("123123"));
        scorePanel.add(new JLabel("123123"));
        scorePanel.add(new JLabel("123123"));
        scorePanel.add(new JLabel("123123"));
        scorePanel.add(new JLabel("123123"));
        scorePanel.add(new JLabel("123123"));
        scorePanel.add(new JLabel("123123"));
        scorePanel.add(new JLabel("123123"));
        scorePanel.add(new JLabel("123123"));*/

        currentGamePanel.setLayout(new VerticalLayout(225,25));
        currentGamePanel.add(new JLabel("Ведущий "));
        currentGamePanel.add(new JLabel("Размер поля"));
        currentGamePanel.add(new JLabel("Еда"));

        chatPanel.setLayout(new VerticalLayout(465, 25));
        chatPanel.add(new JLabel("123123"));
        chatPanel.add(new JLabel("123123"));
        chatPanel.add(new JLabel("123123"));
        chatPanel.add(new JLabel("123123"));
        chatPanel.add(new JLabel("123123"));
        chatPanel.add(new JLabel("123123"));

        this.setFocusable(true);
        this.requestFocus();
        addKeyListener(new FieldKeyListener(gameField,controller));

        this.add(scorePane);
        this.add(currentPane);
        this.add(backButton);
        this.add(viewModButton);
        this.add(chatPane);

    }

    public void addGameField(int width, int height) {
        int size = gameField.getDOT_SIZE();
        int x = (1000 - width*size)/2;
        int y = (1000 - height*size)/2;
        gameField.setBounds(x+5,y+5,width*size,height*size);
        gameField.setBackground(Color.BLACK);
        this.add(gameField);

        setFocusable(true);
        requestFocus();
        this.repaint();

    }

    public void repaintScore(List<SnakesProto.GamePlayer> players) {//не работает
        if(!score.isEmpty()){
            for(Map.Entry<SnakesProto.GamePlayer, JLabel> entry : score.entrySet()){
                scorePanel.remove(entry.getValue());
            }
        }
        score.clear();
        for(SnakesProto.GamePlayer player: players){
            if(!player.getRole().equals(SnakesProto.NodeRole.VIEWER)) {
                JLabel label = new JLabel(player.getName() + " счет: " + player.getScore());
                score.put(player, label);
                scorePanel.add(label);
            }
        }
        scorePanel.revalidate();
        //scorePanel.repaint();
       // repaint();
        setFocusable(true);
        requestFocus();
    }
}
