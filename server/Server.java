package server;

import java.io.*;
import java.net.*;
import java.sql.*;

public class Server {
    public ServerSocket ss;
    public static Connection con;
    public Socket soc;
    
    public Server(int port) throws IOException, SQLException, ClassNotFoundException{
        ss=new ServerSocket(port);
        Class.forName("com.mysql.cj.jdbc.Driver");
        System.out.println("Server has connected to the database.");
        //connecting to the challenge database
        con=DriverManager.getConnection("jdbc:mysql://localhost:3306/challenge","root","");
    }

   public void start() {
        System.out.println("\tMathematics Challenge System. waiting for the client.....");
        while (true) {
                    try {
                    //accepting request from the client
                    Socket soc = ss.accept(); 
                    // Wait for a client to connect
                    System.out.println("Client connected: " +soc );
                    new Clienthandler(soc, con).start();                
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public static void main(String[] args) throws ClassNotFoundException, SQLException {
        try{
        Server SSS=new Server(6728);
        SSS.start();
        //System.out.println(" Client has connected at " +SSS.soc);
        
        }catch(IOException e){
            e.printStackTrace();
        }
    }

   }
  
