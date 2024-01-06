/**
This class acts as the entrypoint for a vulnerability

*/
import java.io.*;
public class Driver{
    public static void main(String[] args) throws Exception{
        File f = new File(args[0]);
        FileInputStream fs = new FileInputStream(f);
        try(ObjectInputStream in = new ObjectInputStream(fs)){
            Object o = in.readObject();
            System.out.println(o);
        }
    }
}