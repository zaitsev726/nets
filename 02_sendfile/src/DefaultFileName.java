public class DefaultFileName {
    static private int i = 0;
    static private String s = "NewFile";
    public DefaultFileName(){

    }
    static public String namer(String r){
        synchronized (DefaultFileName.class) {
            i++;
            return s+ i + "."+ r;
        }

    }
}
