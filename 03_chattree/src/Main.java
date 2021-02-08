import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        try {
            ChatNode chatNode = new ChatNode(args);
            new ConsoleListener(chatNode, args[0]).start();
        } catch (IOException | ArgsNotFoundException e) {
            e.printStackTrace();
        }
    }
}
