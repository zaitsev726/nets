package UserInterface;

import Global.GlobalController;
import UserInterface.ConnectionPage.ConnectionPanel;
import UserInterface.Frames.Window;
import UserInterface.GamePage.GamePanel;
import UserInterface.MenuPage.MenuPanel;
import UserInterface.NetInfoEntryPage.NetInfoEntryPanel;
import UserInterface.NewGamePage.NewGamePanel;
import com.google.protobuf.InvalidProtocolBufferException;
import me.ippolitov.fit.snakes.SnakesProto.GameMessage;
import me.ippolitov.fit.snakes.SnakesProto.GamePlayer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.DatagramPacket;
import java.util.List;
import java.util.*;

public class InterfaceController {
    private static Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    private int sizeWidth = 1520;
    private int sizeHeight = 1045;
    private int locationX = (screenSize.width - sizeWidth) / 2;
    private int locationY = (screenSize.height - sizeHeight) / 2 - 20;

    private UserInterface.Frames.Window window;
    private MenuPanel menuPanel;
    private NewGamePanel newGamePanel;
    private GamePanel gamePanel;
    private ConnectionPanel connectionPanel;
    private NetInfoEntryPanel netInfoEntryPanel;

    private GlobalController controller;

    private volatile HashMap<GameMessage, DatagramPacket> multicastMessages;
    private volatile HashMap<GameMessage, Date> lastDate;
    private volatile HashMap<GameMessage, JButton> connectButtons;
    private volatile HashMap<JButton, DatagramPacket> hosts;

    public InterfaceController(GlobalController controller) {
        multicastMessages = new HashMap<>();
        connectButtons = new HashMap<>();
        lastDate = new HashMap<>();
        hosts = new HashMap<>();

        window = new Window(sizeWidth, sizeHeight, locationX, locationY);
        menuPanel = new MenuPanel(sizeWidth, sizeHeight);
        newGamePanel = new NewGamePanel();
        gamePanel = new GamePanel(controller);
        connectionPanel = new ConnectionPanel();
        netInfoEntryPanel = new NetInfoEntryPanel();
        this.controller = controller;

        window.add(menuPanel);
        initializationListeners();
        window.setVisible(true);
    }

    private void initializationListeners() {
        initializationMenuListeners();
        initializationNewGamePanelListeners();
        initializationGameListeners();
        initializationConnectionListeners();
        initializationNetInfoEntryListeners();

    }

    private void initializationNetInfoEntryListeners() {
        netInfoEntryPanel.continueButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                window.remove(netInfoEntryPanel);
                window.add(menuPanel);
                window.revalidate();
                window.repaint();
            }
        });

        netInfoEntryPanel.nameField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Отображение введенного текста
                String w = netInfoEntryPanel.nameField.getText();
                controller.setName(w);
            }
        });
    }

    private void initializationConnectionListeners() {
        connectionPanel.backButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                window.remove(connectionPanel);
                window.add(menuPanel);
                window.revalidate();
                window.repaint();
            }
        });
    }

    private void initializationGameListeners() {
        gamePanel.backButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                controller.sendRoleChange();
                controller.removeGame();
                window.remove(gamePanel);
                window.add(connectionPanel);
                window.revalidate();
                window.repaint();
            }
        });
        gamePanel.viewModButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                controller.exitSM();
                controller.setOurId(0);
            }
        });
    }

    private void initializationMenuListeners() {
        menuPanel.newGameButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                window.remove(menuPanel);
                window.add(newGamePanel);
                window.revalidate();
                window.repaint();
            }
        });

        menuPanel.connectButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                window.remove(menuPanel);
                window.add(connectionPanel);
                window.revalidate();
                window.repaint();
            }
        });

        menuPanel.netInfoButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                window.remove(menuPanel);
                window.add(netInfoEntryPanel);
                window.revalidate();
                window.repaint();
            }
        });
    }

    private void initializationNewGamePanelListeners() {

        newGamePanel.widthField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Отображение введенного текста
                try {
                    int w = Integer.parseInt(newGamePanel.widthField.getText());
                    if (w > 100 || w < 10) {
                        JOptionPane.showMessageDialog(window,
                                "Введите число в диапазоне от 10 до 100, а не " + w);
                    } else {
                        controller.setWidth(w);
                    }
                } catch (NumberFormatException r) {
                    JOptionPane.showMessageDialog(window, "Вы некорректно ввели цифры!");
                }
            }
        });

        newGamePanel.heightField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    int w = Integer.parseInt(newGamePanel.heightField.getText());
                    if (w > 100 || w < 10) {
                        JOptionPane.showMessageDialog(window,
                                "Введите число в диапазоне от 10 до 100, а не " + w);
                    } else
                        controller.setHeight(w);
                } catch (NumberFormatException r) {
                    JOptionPane.showMessageDialog(window, "Вы некорректно ввели цифры!");
                }
            }
        });

        newGamePanel.foodStaticField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    int w = Integer.parseInt(newGamePanel.foodStaticField.getText());
                    if (w > 100 || w < 0) {
                        JOptionPane.showMessageDialog(window,
                                "Введите число в диапазоне от 0 до 100, а не " + w);
                    } else
                        controller.setFoodStatic(w);
                } catch (NumberFormatException r) {
                    JOptionPane.showMessageDialog(window, "Вы некорректно ввели цифры!");
                }
            }
        });

        newGamePanel.foodPerPlayerField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    float w = Float.parseFloat(newGamePanel.foodPerPlayerField.getText());
                    if (w > 100 || w < 0) {
                        JOptionPane.showMessageDialog(window,
                                "Введите число в диапазоне от 0 до 100, а не " + w);
                    } else
                        controller.setFoodPerPlayer(w);
                } catch (NumberFormatException r) {
                    JOptionPane.showMessageDialog(window, "Вы некорректно ввели вещественное число!");
                }
            }
        });

        newGamePanel.stateDelayField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    int w = Integer.parseInt(newGamePanel.stateDelayField.getText());
                    if (w > 10000 || w < 1) {
                        JOptionPane.showMessageDialog(window,
                                "Введите число в диапазоне от 1 до 10000, а не " + w);
                    } else
                        controller.setStateDelay(w);
                } catch (NumberFormatException r) {
                    JOptionPane.showMessageDialog(window, "Вы некорректно ввели цифры!");
                }
            }
        });

        newGamePanel.deadFoodProbField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    float w = Float.parseFloat(newGamePanel.deadFoodProbField.getText());
                    if (w > 1 || w < 0) {
                        JOptionPane.showMessageDialog(window,
                                "Введите число в диапазоне от 0 до 1, а не " + w);
                    } else
                        controller.setDeadFoodProb(w);
                } catch (NumberFormatException r) {
                    JOptionPane.showMessageDialog(window, "Вы некорректно ввели вещественное число!");
                }
            }
        });

        newGamePanel.pingDelayField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    int w = Integer.parseInt(newGamePanel.pingDelayField.getText());
                    if (w > 10000 || w < 1) {
                        JOptionPane.showMessageDialog(window,
                                "Введите число в диапазоне от 1 до 10000, а не " + w);
                    } else
                        controller.setPingDelay(w);
                } catch (NumberFormatException r) {
                    JOptionPane.showMessageDialog(window, "Вы некорректно ввели цифры!");
                }
            }
        });

        newGamePanel.nodeTimeoutField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    int w = Integer.parseInt(newGamePanel.nodeTimeoutField.getText());
                    if (w > 10000 || w < 1) {
                        JOptionPane.showMessageDialog(window,
                                "Введите число в диапазоне от 1 до 10000, а не " + w);
                    } else
                        controller.setNodeTimeout(w);
                } catch (NumberFormatException r) {
                    JOptionPane.showMessageDialog(window, "Вы некорректно ввели цифры!");
                }
            }
        });

        newGamePanel.backButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                window.remove(newGamePanel);
                window.add(menuPanel);
                window.revalidate();
                window.repaint();
            }
        });

        newGamePanel.continueButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                window.remove(newGamePanel);
                window.add(gamePanel);
                controller.initializationGame();
                gamePanel.addGameField(controller.getWidth(), controller.getHeight());
                gamePanel.setFocusable(true);
                gamePanel.requestFocus();
                window.revalidate();
                window.repaint();
            }
            //}
        });
    }

    public synchronized void addNewConnectButton(DatagramPacket dp) {

        byte[] a1 = Arrays.copyOf(dp.getData(), dp.getLength());
        GameMessage.AnnouncementMsg announcementMsg = null;
        GameMessage message = null;
        try {
            message = GameMessage.parseFrom(a1);
            announcementMsg = message.getAnnouncement();
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }

        if (announcementMsg == null)
            return;

        //приходит ли нам один и тот же Announcement
        for(Map.Entry<GameMessage, DatagramPacket> entry : multicastMessages.entrySet()){
            if(entry.getValue().getAddress().toString().equals(dp.getAddress().toString()) &&
                entry.getValue().getPort() == dp.getPort()){
                if(entry.getKey().getAnnouncement().equals(announcementMsg)) {
                    lastDate.remove(entry.getKey());
                    lastDate.put(message,new Date());
                    return;
                }
            }
        }

        //если нету такого пакета
     //   if(!needToSwap){

         //   return;
       // }
    /*
    private HashMap<GameMessage, DatagramPacket> multicastMessages;
    private HashMap<GameMessage, Date> lastDate;
    private HashMap<GameMessage, JButton> connectButtons;
    private HashMap<JButton, DatagramPacket> hosts;
     */
        Iterator<Map.Entry<GameMessage, DatagramPacket>> iterator
                = multicastMessages.entrySet().iterator();
        //если Announcement поменялся
        while (iterator.hasNext()) {
            Map.Entry<GameMessage, DatagramPacket> next = iterator.next();
            //находим старый месседж
            if (next.getValue().getAddress().equals(dp.getAddress()) &&
                    next.getValue().getPort() == dp.getPort()) {
                lastDate.remove(next.getKey());
                hosts.remove(connectButtons.get(next.getKey()));
                connectionPanel.panel.remove(connectButtons.get(next.getKey()));
                connectButtons.remove(next.getKey());
                iterator.remove();
            }
        }

        multicastMessages.put(message, dp);
        lastDate.put(message, new Date());

        List<GamePlayer> players = announcementMsg.getPlayers().getPlayersList();
        String hostName = "unknown";
        for (GamePlayer player : players) {
            if (player.getId() == 0)
                hostName = player.getName();
        }

         JButton button = new JButton("Ширина: " + announcementMsg.getConfig().getWidth() + " " +
                "Длина: " + announcementMsg.getConfig().getHeight() + " " +
                "StaticFood: " + announcementMsg.getConfig().getFoodStatic() + " " +
                "FoodPerPlayer: " + announcementMsg.getConfig().getFoodPerPlayer() + " " +
                "Delay: " + announcementMsg.getConfig().getPingDelayMs() + " " +
                "Prob: " + announcementMsg.getConfig().getDeadFoodProb() + " " +
                "Host: " + hostName + " " +
                "Can join: " + announcementMsg.getCanJoin() +  " " +
                "IP: " + dp.getAddress() + " " +
                "Port: " + dp.getPort());

     //   button.addActionListener(new Listener(button)

        button.addActionListener(new Listener(button));

        connectButtons.put(message, button);
        hosts.put(button,dp);

        connectionPanel.panel.add(button,BorderLayout.CENTER);
      //  connectionPanel.panel.add(button,BorderLayout.CENTER);
        connectionPanel.panel.revalidate();
        window.revalidate();
        //window.repaint();
    }

    public synchronized void removeButton() {
        Iterator<Map.Entry<GameMessage, Date>> iterator = lastDate.entrySet().iterator();
        Date d = new Date();
        while (iterator.hasNext()) {
            Map.Entry<GameMessage, Date> next = iterator.next();
            if (d.getTime() - next.getValue().getTime() > 5000) {
                connectionPanel.panel.remove(connectButtons.get(next.getKey()));
                hosts.remove(connectButtons.get(next.getKey()));
                connectButtons.remove(next.getKey());
                multicastMessages.remove(next.getKey());
                iterator.remove();
                window.revalidate();
                window.repaint();
            }
        }
    }

    public void repaintField(int[][] a, int width, int height, int ID, List<GamePlayer> players) {
        gamePanel.repaintScore(players);
        gamePanel.gameField.repaintField(a, width, height, ID);

        gamePanel.repaint();
        gamePanel.setFocusable(true);
        gamePanel.requestFocus();

    }

    public void showMessage(String message) {
        JOptionPane.showMessageDialog(window, message);
    }

    public class Listener implements ActionListener {
        private JButton button;
        public Listener(JButton button){
            this.button = button;
        }
        public void actionPerformed(ActionEvent e) {
            DatagramPacket dp = hosts.get(button);
            byte[] a1 = Arrays.copyOf(dp.getData(), dp.getLength());
            GameMessage.AnnouncementMsg message = null;
            try {
                message = GameMessage.parseFrom(a1).getAnnouncement();
            } catch (InvalidProtocolBufferException m) {
                m.printStackTrace();
            }
            controller.initializationConnect(dp);
            window.remove(connectionPanel);
            window.add(gamePanel);

            gamePanel.addGameField(message.getConfig().getWidth(),message.getConfig().getHeight());
            gamePanel.setFocusable(true);
            gamePanel.requestFocus();
            window.revalidate();
            window.repaint();
        }
    }
}
