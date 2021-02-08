package UserInterface.MenuPage;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import static java.lang.System.exit;

public class MenuPanel extends JPanel {

    private final int buttonWidth = 1000;
    private final int buttonHeight = 150;

    public JButton connectButton;
    public JButton newGameButton;
    public JButton netInfoButton;
    private JButton exitButton;

    public MenuPanel(int sizeWidth, int sizeHeight) {
        this.setLayout(null);

        connectButton = new JButton("CONNECT");
        newGameButton = new JButton("NEW GAME");
        netInfoButton = new JButton("NETWORK");
        exitButton = new JButton("EXIT");

        connectButton.setVerticalTextPosition(AbstractButton.CENTER);
        connectButton.setHorizontalTextPosition(AbstractButton.CENTER);

        newGameButton.setVerticalTextPosition(AbstractButton.CENTER);
        newGameButton.setHorizontalTextPosition(AbstractButton.CENTER);

        netInfoButton.setVerticalTextPosition(AbstractButton.CENTER);
        netInfoButton.setHorizontalTextPosition(AbstractButton.CENTER);

        exitButton.setVerticalTextPosition(AbstractButton.CENTER);
        exitButton.setHorizontalTextPosition(AbstractButton.CENTER);

        exitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                exit(0);
            }
        });

        netInfoButton.setBounds(10,10,100,20);

        connectButton.setBounds((sizeWidth - buttonWidth) / 2, (sizeHeight - buttonHeight) / 2 - buttonHeight - 5, buttonWidth, buttonHeight);
        newGameButton.setBounds((sizeWidth - buttonWidth) / 2, (sizeHeight - buttonHeight) / 2, buttonWidth, buttonHeight);
        exitButton.setBounds((sizeWidth - buttonWidth) / 2, (sizeHeight - buttonHeight) / 2 + buttonHeight + 5, buttonWidth, buttonHeight);

        this.add(connectButton);
        this.add(newGameButton);
        this.add(exitButton);
        this.add(netInfoButton);

        setVisible(true);
    }
}
