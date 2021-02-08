
import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.sql.SQLOutput;
import java.util.Arrays;
import java.util.Date;

public class ServerSomething extends Thread{
    private Socket socket; //сокет клиента

    private byte[] buffer = new byte[256];
    private InputStream in;
    private OutputStream out;
    private String fileName;
    private long sizeOfFile;
    private String UTF8 = "UTF-8";
    private String path = File.separator + "Users" + File.separator + "arski" + File.separator + "Desktop" + File.separator + "lab2Server3 — копия 4" +
            File.separator +"uploads" +File.separator;
    private FileOutputStream fileOut;

    public ServerSomething(Socket socket){
        this.socket = socket;
        System.out.println("Socket is created");
        try {
            in = socket.getInputStream();
            out = socket.getOutputStream();
        }catch (IOException e){
            e.printStackTrace();
            System.out.println("Bad socket");
        }
        start();
    }

    @Override
    public void run() {


        String word;
        long countOfBytes=0;
        long num=0;
        int i =1;

        try {
            socket.setSoTimeout(30000);
            in.readNBytes(buffer,0,4);
            int lengthOfFileName = ByteBuffer.wrap(buffer).getInt();
            /* Длина имени получена */
            //
            byte[] nameOfFile = new byte[lengthOfFileName];
            in.readNBytes(nameOfFile,0,lengthOfFileName);
            fileName = new String(nameOfFile,UTF8);
            creatingFile(fileName);
            /* Имя получено */
            //
            in.readNBytes(buffer,0,8);
            sizeOfFile = ByteBuffer.wrap(buffer).getLong();
            System.out.println(sizeOfFile);
            /* Длина файла получена */
            //
            out.write("VSE OK".getBytes());// конец приема метаинформации

            Date old = new Date();
            Date d = old;
            while (sizeOfFile>0) {
                if(sizeOfFile >=256) {
                    buffer = in.readNBytes(256);
                    sizeOfFile = sizeOfFile - 256;
                    fileOut.write(buffer,0,buffer.length);
                    num = num + 256;
                    countOfBytes += 256;
                }
                else{
                    int a = (int) sizeOfFile;
                    buffer = in.readNBytes(a);
                    fileOut.write(buffer,0,buffer.length);
                    num = num + sizeOfFile;
                    countOfBytes += a;
                    sizeOfFile = 0;

                }
                Date now = new Date();
                if(now.getTime() - old.getTime() > 3000){
                    System.out.println("***************");
                    System.out.println("Cur speed is " + (double)num/3/(1048576));
                    //System.out.println("Aver speed is " + countOfBytes/(now.getTime()-d.getTime()));
                    //System.out.println("Aver speed is " + (double)countOfBytes/(now.getTime()-d.getTime())/1048576*1000);
                    System.out.println("Aver speed is " + (double)countOfBytes/(3*i)/1048576);
                    i++;
                    System.out.println(countOfBytes);
                    System.out.println(now.getTime()-d.getTime());
                    num = 0;
                    old = now;
                    System.out.println("***************");
                }

            }
            out.write("HOROSH".getBytes()); //конец общения с клиентом
            fileOut.close();
            socket.close();
            System.out.println("*** ПЕРЕДАЧА ЗАКОНЧЕНА ***");
        }
        catch (SocketTimeoutException e){
            try {
                socket.close();
                fileOut.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        catch (IOException e){
            e.printStackTrace();
        }

    }


    private void creatingFile(String fileName){ 
        String filePath = path + fileName;
        File f = new File(filePath);
        f.getParentFile().mkdir();
        if(fileName.split(File.separator).length!=1){
            System.out.println("That's not a name of file");
        }
        try {
            if(f.createNewFile()){
                fileOut =new FileOutputStream(filePath);

            }else{
                String k = fileName.split("\\.")[1];
                System.out.println("eto DAAA" +k);
                fileOut= new FileOutputStream(path+DefaultFileName.namer(k));
            }

        }catch (IOException e) {
            e.printStackTrace();
        }
    }

}

