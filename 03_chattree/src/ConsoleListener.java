import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ConsoleListener extends Thread {
    private ChatNode node;
    private String nodeName;

    public ConsoleListener(ChatNode node, String nodeName) {
        this.node = node;
        this.nodeName = nodeName + ":";

    }

    @Override
    public void run() {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        String str = "";
        int maxLenght = (2048 - 5 - nodeName.length()) / 2;
        while (true) {
            try {

                str = bufferedReader.readLine();
                System.out.println(str);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (str.startsWith("/")) {

                switch (str.substring(1)) {
                    case "exit":
                        node.exit();
                        System.out.println("ВСЕ ЗАКРЫТО");
                        this.interrupt();
                        System.exit(1);
                        break;
                    case "users":
                        UserManagement.printUsers();
                        break;
                    case "queue":

                        break;
                    default:
                        System.out.println("WRONG COMMAND");
                        break;
                }

            } else {

                MessageCreator.createNewMessage(nodeName + str);
                str = "";
            }
        }
    }
}

