package UserInterface.ConnectionPage;

import UserInterface.Layouts.VerticalLayout;

import javax.swing.*;
import java.awt.*;

public class ConnectionPanel extends JPanel {
    public JPanel panel;
    JScrollPane scrollPane;
    public JButton backButton;

    public ConnectionPanel() {
        this.setLayout(new BorderLayout());

        panel = new JPanel();

        JLabel label = new JLabel("PLAY MULTIPLAYER");
        label.setVerticalAlignment(JLabel.CENTER);
        label.setHorizontalAlignment(JLabel.CENTER);
        label.setFont(new Font("Dialog", Font.ITALIC, 36));

        backButton = new JButton("BACK");
        backButton.setVerticalTextPosition(AbstractButton.CENTER);
        backButton.setHorizontalTextPosition(AbstractButton.CENTER);
        backButton.setFont(new Font("Dialog", Font.BOLD, 36));

        this.add(label, BorderLayout.NORTH);
        this.add(backButton, BorderLayout.SOUTH);

        panel.setLayout(new VerticalLayout(1477, 75));

        scrollPane = new JScrollPane(panel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        this.add(scrollPane, BorderLayout.CENTER);

        setVisible(true);
    }
}
