package UserInterface.NewGamePage;

import javax.swing.*;
import java.awt.*;

public class NewGamePanel extends JPanel {

    public JTextField widthField;
    public JTextField heightField;
    public JTextField foodStaticField;
    public JTextField foodPerPlayerField;
    public JTextField stateDelayField;
    public JTextField deadFoodProbField;
    public JTextField pingDelayField;
    public JTextField nodeTimeoutField;

    public JButton continueButton;
    public JButton backButton;
    public JCheckBox checkBox;

    public NewGamePanel() {
        this.setLayout(null);

        continueButton = new JButton("CONTINUE");
        backButton = new JButton("BACK");
        checkBox = new JCheckBox("SINGLEPLAYER");
        checkBox.setSelected(false);

        backButton.setVerticalTextPosition(AbstractButton.CENTER);
        backButton.setHorizontalTextPosition(AbstractButton.CENTER);

        widthField = new JTextField();
        heightField = new JTextField();
        foodStaticField = new JTextField();
        foodPerPlayerField = new JTextField();
        stateDelayField = new JTextField();
        deadFoodProbField = new JTextField();
        pingDelayField = new JTextField();
        nodeTimeoutField = new JTextField();

        JLabel label = new JLabel("Ширина поля в клетках (от 10 до 100): ");
        label.setFont(new Font("Dialog", Font.PLAIN, 26));
        label.setBounds(50, 50, 1250, 30);
        this.add(label);

        widthField.setBounds(50, 100, 400, 30);
        widthField.setFont(new Font("Dialog", Font.PLAIN, 26));
        widthField.setText("40");

        label = new JLabel("Высота поля в клетках (от 10 до 100): ");
        label.setFont(new Font("Dialog", Font.PLAIN, 26));
        label.setBounds(50, 150, 1250, 30);
        this.add(label);

        heightField.setBounds(50, 200, 400, 30);
        heightField.setFont(new Font("Dialog", Font.PLAIN, 26));
        heightField.setText("30");

        label = new JLabel("Количество клеток с едой, независимо от числа игроков (от 0 до 100): ");
        label.setFont(new Font("Dialog", Font.PLAIN, 26));
        label.setBounds(50, 250, 1250, 30);
        this.add(label);

        foodStaticField.setBounds(50, 300, 400, 30);
        foodStaticField.setFont(new Font("Dialog", Font.PLAIN, 26));
        foodStaticField.setText("1");

        label = new JLabel("Количество клеток с едой, на каждого игрока (вещественный коэффициент от 0 до 100): ");
        label.setFont(new Font("Dialog", Font.PLAIN, 26));
        label.setBounds(50, 350, 1250, 30);
        this.add(label);

        foodPerPlayerField.setBounds(50, 400, 400, 30);
        foodPerPlayerField.setFont(new Font("Dialog", Font.PLAIN, 26));
        foodPerPlayerField.setText("1");

        label = new JLabel("Задержка между ходами (сменой состояний) в игре, в миллисекундах (от 1 до 10000): ");
        label.setFont(new Font("Dialog", Font.PLAIN, 26));
        label.setBounds(50, 450, 1250, 30);
        this.add(label);

        stateDelayField.setBounds(50, 500, 400, 30);
        stateDelayField.setFont(new Font("Dialog", Font.PLAIN, 26));
        stateDelayField.setText("1000");

        label = new JLabel("Вероятность превращения мёртвой клетки в еду (от 0 до 1): ");
        label.setFont(new Font("Dialog", Font.PLAIN, 26));
        label.setBounds(50, 550, 1250, 30);
        this.add(label);

        deadFoodProbField.setBounds(50, 600, 400, 30);
        deadFoodProbField.setFont(new Font("Dialog", Font.PLAIN, 26));
        deadFoodProbField.setText("0.1");

        label = new JLabel("Задержка между отправкой ping-сообщений, в миллисекундах (от 1 до 10000): ");
        label.setFont(new Font("Dialog", Font.PLAIN, 26));
        label.setBounds(50, 650, 1050, 30);
        this.add(label);

        pingDelayField.setBounds(50, 700, 400, 30);
        pingDelayField.setFont(new Font("Dialog", Font.PLAIN, 26));
        pingDelayField.setText("100");

        label = new JLabel("Таймаут, после которого считаем что узел-сосед отпал, в миллисекундах (от 1 до 10000): ");
        label.setFont(new Font("Dialog", Font.PLAIN, 26));
        label.setBounds(50, 750, 1250, 30);
        this.add(label);

        nodeTimeoutField.setBounds(50, 800, 400, 30);
        nodeTimeoutField.setFont(new Font("Dialog", Font.PLAIN, 26));
        nodeTimeoutField.setText("800");

        checkBox.setFont(new Font("Dialog", Font.PLAIN, 26));
        checkBox.setBounds(600, 900, 300, 30);

        continueButton.setBounds(1250, 850, 200, 100);
        backButton.setBounds(50, 850, 200, 100);

        this.add(checkBox);
        this.add(backButton);
        this.add(continueButton);
        this.add(widthField);
        this.add(heightField);
        this.add(foodStaticField);
        this.add(foodPerPlayerField);
        this.add(stateDelayField);
        this.add(deadFoodProbField);
        this.add(pingDelayField);
        this.add(nodeTimeoutField);
        setVisible(true);
    }
}

