import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class ClientSocket extends Thread{

    private Socket socket;
    private InputStream in; //поток чтения из сокета
    private OutputStream out; //поток записи в сокет
    private int port; //порт
    private String addr; //ip адрес
    private String fileName;
    private String filePath;

    public ClientSocket(String addr, int port, String filePath){
        this.addr = addr;
        this.port = port;
        this.filePath = filePath;

        try{
            this.socket = new Socket(addr, port);
        }catch (IOException e) {
            e.printStackTrace();
        }
        try{
            in = socket.getInputStream();
            out = socket.getOutputStream();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    private void parse(String s){
        String[] kek = s.split(File.separator);
        fileName = kek[kek.length-1];
    }

    @Override
    public void run(){
        try{
            long sizeOfFile = (new File(filePath).length());
            InputStream stream = new FileInputStream(filePath);
            byte[] buffer = new byte[256];
            parse(filePath);
            int len = fileName.getBytes().length;


            ByteBuffer bb = ByteBuffer.allocate(Integer.BYTES);
            byte[] failNameSize = bb.putInt(len).array();
            System.out.println(failNameSize[3]);
            out.write(failNameSize,0,4);//отправляем длину имени


            byte[] fn1 = new byte[256];
            byte[] filename = fileName.getBytes();
            out.write(filename,0,len);//конец отправки имени


            ByteBuffer fl = ByteBuffer.allocate(Long.BYTES);
            byte[] fn2 = fl.putLong(sizeOfFile).array();
            out.write(fn2,0,8);//конец отправки длины файла

            byte[] answer = new byte[6];
            answer=in.readNBytes(6);//подтвердили

            out.flush();
            int num;
            while((num=stream.read(buffer) )!= -1) {
                out.write(buffer,0,num);
            }

            answer = in.readNBytes(6);
            System.out.print(new String(answer, StandardCharsets.UTF_8));//вот так корректно переводим массив байт в строку. Arrays.toString(answer) дает массив байткодов, а нам такое не нужно
            System.out.println();
            out.close();
        }catch (FileNotFoundException e){
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

}

