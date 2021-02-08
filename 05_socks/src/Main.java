import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        if (args.length == 1){
            Integer port;
            try {
                port = Integer.parseInt(args[0]);
                try {
                    Handler handler = new Handler(port);
                    handler.execute();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }catch (NumberFormatException ex){
                ex.printStackTrace();
            }
        }
    }
}