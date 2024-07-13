import java.io.*;
import java.net.*;
import java.sql.*;



public class Server {
    public static void main(String[] args) throws ClassNotFoundException, SQLException {
        try{
        ServerSocket ss=new ServerSocket(6728);
        System.out.println("\tMathematics Challenge System. waiting for the client.....");
        ss.accept();
        Class.forName("com.mysql.cj.jdbc.Driver");
        Connection con=DriverManager.getConnection("jdbc:mysql://localhost:3306/challenge","root","");
        System.out.println("Server has connected to the database.");
        }catch(IOException e){
            e.printStackTrace();
        }
    }}
    
