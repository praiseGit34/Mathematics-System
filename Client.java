import java.io.*;
import java.net.*;

public class Client {
    public static void main(String[] args) {
        try{
            Socket soc=new Socket("localhost", 6728);
            System.out.println("\t Welcome to mathematics Challenge System ");
        }catch(IOException e){
            e.printStackTrace();
        }
    }}
    
