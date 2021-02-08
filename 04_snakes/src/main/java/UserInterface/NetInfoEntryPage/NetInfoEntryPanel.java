package UserInterface.NetInfoEntryPage;

import javax.swing.*;
import java.awt.*;

public class NetInfoEntryPanel extends JPanel {
    public JTextField nameField;

    public JButton continueButton;

    public NetInfoEntryPanel(){
        this.setLayout(null);
        nameField = new JTextField();
        continueButton = new JButton("CONTINUE");
        continueButton.setBounds(668, 500, 200, 100);

        JLabel label = new JLabel("Введите имя игрока: ");
        label.setFont(new Font("Dialog", Font.PLAIN, 26));

        label.setBounds(5,10,680,30);

        nameField.setBounds(5,50,680,30);


        this.add(label);
        this.add(nameField);
        this.add(continueButton);
    }
}
