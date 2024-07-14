import java.io.*;
import java.net.*;
import java.sql.*;
public class Server {
    public ServerSocket ss;
    public Connection con;
    public Socket soc;
    
    public Server(int port) throws IOException, SQLException, ClassNotFoundException{
        ss=new ServerSocket(port);
        Class.forName("com.mysql.cj.jdbc.Driver");
        System.out.println("Server has connected to the database.");
        con=DriverManager.getConnection("jdbc:mysql://localhost:3306/challenge","root","");
    }

    public void start() {
        System.out.println("\tMathematics Challenge System. waiting for the client.....");
        while (true) {
                    try {
                    Socket soc = ss.accept(); // Wait for a client to connect
                    System.out.println("Client connected: " +soc );
                    new ClientHandler(soc, con).start(); 
                   
                
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
    private static class ClientHandler extends Thread{
    private final Socket soc;
    private final Connection con;
    private PrintWriter out;
    private BufferedReader reader;
    private 
    
     ClientHandler(Socket soc, Connection con) {
       this.soc=soc;
       this.con=con;

    }
    public void run(){
        try{
        out = new PrintWriter(soc.getOutputStream(), true);
        reader= new BufferedReader(new InputStreamReader(soc.getInputStream()));
        String inputLine;
        while ((inputLine = reader.readLine()) != null) {
            String response = processRequest(inputLine);
            out.println(response);
            out.flush();
            
        }
    }catch(IOException e){
        e.printStackTrace();
    }
    }
    private String processRequest(String request) {
        String[] part = request.split(" ");
        String action = part[0].toUpperCase();
        if(action.startsWith("register")){
            return registerUser(part);
        }else if(action.startsWith("login")){
            if (part.length == 3) {
                return loginUser(part[1], part[2]);
            } else if (part.length == 2 && part[1].contains("@")) {
                return getRepresentativePassword(part[1]);
            } else {
                return "Invalid login format.";
            }
        }else if(action.startsWith("logout")){
            return logoutUser();
        }else if(action.startsWith("Viewchallenges")){
            return viewChallenges();
        }else if(action.startsWith("attemptchallenge")){
            return attemptChallenge(part[1]);
        }else if(action.startsWith("view applicants")){
            return viewApplicants();
        }else if(action.startsWith("confirm applicant")){
            if (part.length != 3) {
                return "Invalid command format. Use: CONFIRM_APPLICANT yes/no username";
            }
            return confirmApplicant(part[1], part[2]);
        }else{
            return "invalid request";
        }

            
    }

    private String confirmApplicant(String string, String string2) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'confirmApplicant'");
    }
    private String loginUser(String username, String password) {
        try {
            String query = "SELECT * FROM users WHERE username = ? AND password = ?";
            PreparedStatement stmt = con.prepareStatement(query);
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return "Login successful for user: " + username;
            } else {
                return "Invalid username or password.";
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "Error during login.";
        }
     }  


    private String viewApplicants() {
    try {
        StringBuilder sb = new StringBuilder();
        String query = "SELECT * FROM applicants";
        PreparedStatement st = con.prepareStatement(query);
        ResultSet rs = st.executeQuery();

        while (rs.next()) {
            String username = rs.getString("username");
            String registrationNumber = rs.getString("registration_number");
            sb.append("Username: ").append(username).append(", Registration Number: ").append(registrationNumber).append("\n");
        }

        if (sb.length() == 0) {
            return "No applicants found.";
        } else {
            return sb.toString();
        }
    } catch (SQLException e) {
        e.printStackTrace();
        return "Error in viewing applicants.";
    }
}




    private String registerUser(String[] part) throws SQLException {
       
    String dateOfBirth = part[4].trim();
    // validate date format yyyy-mm-dd 
    if (!dateOfBirth.matches("\\d{4}/\\d{2}/\\d{2}")) {
    System.out.println("Invalid date format.");
    }
    //replace a hash with a hyphen
    dateOfBirth = dateOfBirth.replace('/', '-'); // Converting to SQL date format
    String checkQuery = "SELECT * FROM schools WHERE schoolregistrationumber = ?";
    PreparedStatement checkSt = con.prepareStatement(checkQuery);
    checkSt.setString(1, part[5].trim());
    ResultSet rs = checkSt.executeQuery();

    if (!rs.next()) {
    return "School registration number not found";
    }
    if (part.length != 8) {
        sendMessage("Invalid registration format. Use: Register username firstname lastname emailAddress date_of_birth school_registration_number image_file.png");
        return "register correctly";
    }

    // Example: Insert registration data into MySQL database
    String query = "INSERT INTO users (username, firstname, lastname, emailAddress, dateOfBirth, school_reg_number, image_file) VALUES (?, ?, ?, ?, ?, ?, ?)";
    PreparedStatement stmt = con.prepareStatement(query);
    stmt.setString(1, part[1]);
    stmt.setString(2, part[2]);
    stmt.setString(3, part[3]);
    stmt.setString(4, part[4]);
    stmt.setString(5, part[5]);
    stmt.setString(6, part[6]);
    stmt.setString(7, part[7]);
    int rowsAffected = stmt.executeUpdate();

    if (rowsAffected > 0) {
        sendMessage("Registration successful for " + part[1]);
    } else {
        sendMessage("Registration failed.");
    }
    return query;

   
    }
    private void sendMessage(String message) {
      out.println(message);
    }
}}
    
