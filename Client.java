import java.io.*;
import java.net.*;

public class Client {
    public Socket soc;
    public BufferedReader B;
    public PrintWriter P;
    public BufferedReader Br;


    public void start(String localhost,int port) throws IOException{
         soc=new Socket(localhost,port);
         B=new BufferedReader(new InputStreamReader(System.in));//reads directly the users input
         P=new PrintWriter(soc.getOutputStream() ,true); //sends to the server
         Br=new BufferedReader(new InputStreamReader(soc.getInputStream())); //reads responses from the socket
    }

    //run method will trigger the user to enter a command
    public void run(){
        try{
            while(true){
              
                System.out.println("menu\n\n Register  username lastname firstname emailAddress date_of_birth  school_registration_number image _file.png\n\n viewChallenges -displays the challenges \n\n attempt challenge challenge number \n\n view applicants\n\n confirm yes/no username\n log in");
                System.out.println("enter command of your choice from the menu");
                System.out.println(">>>");
                //clearing the spaces in the user input
                String command = B.readLine().trim();
                //
                if (command.equalsIgnoreCase("exit")) {
                    break;
                }
                //calling the method that executes the commands
                executeInput(command);
            }}catch(IOException e){
                e.printStackTrace();
            }
            }


        //the method that processes commands entered by the user
    private void executeInput(String command) throws IOException {
      
        String part[]=command.split("");
        String menuitem=part[0].toLowerCase();
        //processing the commands

    if (command.startsWith("register")) {
        if (part.length != 8) {
            System.out.println("Invalid registration format. Use: Register username firstname lastname emailAddress date_of_birth school_registration_number image_file.png");
        } else {
             register(part);
        }
       
    }else if(command.startsWith("login")){
        if (part.length == 3) {
            login(part[1], part[2]);
        } else if (part.length == 2 && part[1].contains("@")) {
            loginSchoolRepresentative(part[1]);
        } else
            System.out.println("Invalid login format. Use: login username password (for regular users) or login email@school.com (for school representatives)");
            logout();
    }else if(command.startsWith("view challenges")){
            viewChallenges();
    }else if(command.startsWith("attemptChallenges")){
        attemptChallenges(part[1]);
    }else if(command.startsWith("confirm")){
        if (part.length != 3 || (!part[1].equals("yes") && !part[1].equals("no"))) {
            System.out.println("Invalid format. Use: confirm yes/no username");
        } else {
            confirmApplicant(part[1], part[2]);
        }
     }else if(command.startsWith("view applicants")){
        viewApplicants();
     }
    }
    
    //receiving view applicants response from the server
    private void viewApplicants() throws IOException {
        String response = sendMessage("VIEW_APPLICANTS");
        System.out.println(response);
        }

    //receiving the confirm applicants request from the server
    private void confirmApplicant(String decision, String username) throws IOException {
        String response = sendMessage("CONFIRM_APPLICANT " + decision + " " + username);
        System.out.println(response);
        }

    //receiving questions from the server that was connected to the database
    private void attemptChallenges(String challengeNumber) throws IOException {
        String response = sendMessage("ATTEMPT_CHALLENGE " + challengeNumber);
        System.out.println(response);
        String prompt= Br.readLine();
        System.out.println(prompt);

        System.out.println("Press Enter to start the challenge...");
        B.readLine();
        P.println("start");
        P.flush();

        while (true) {
            String line = Br.readLine();
            if (line == null || line.equals("END_OF_CHALLENGE")) {
                break;
            }
            System.out.println(line);

            if (line.startsWith("Enter your answer")) {
                System.out.print("Your answer: ");
                String answer = B.readLine();
                P.println(answer);
                P.flush();
            }

            if (line.equals("oh sorry!,time is done")) {
                System.out.println("Challenge ended due to time");
                break;
            }
        }

        String Result = Br.readLine();
        System.out.println(Result);
   
    } 
    //retrieves challenges from the server
    private void viewChallenges() throws IOException {
        String response = sendMessage("VIEW_CHALLENGES");
        if (response.startsWith("No challenges") || response.startsWith("Error")) {
            System.out.println(response);
        } else {
            System.out.println("Available Challenges:");
            System.out.println(response);
        }
        }

    //log out method 
    private void logout() throws IOException {
        String response = sendMessage("LOGOUT");
        System.out.println(response);
    }

    //log in of the rep by providing an email address
    private void loginSchoolRepresentative(String email) throws IOException {
        String response = sendMessage("LOGIN " + email);
        System.out.println(response);
        if (response.contains("password was generated")) {
            System.out.print(" enter the password sent to your email: ");
            String password = B.readLine();
            response = sendMessage("LOGIN " + email + " " + password);
            System.out.println(response);
        } }

    //log in method    
    private void login(String username, String password) throws IOException {
        String response = sendMessage("LOGIN " + username + " " + password);
        System.out.println(response);
         }
    //register method
    private void register(String[] a) throws IOException {
        String message = String.join(" ",a);
        String response = sendMessage("REGISTER " + message);
        System.out.println(response);
     }

    public String sendMessage(String msg) throws IOException {
        P.println(msg);
        return Br.readLine();
        }
    public void stopConnection() throws IOException {
        P.close();
        B.close();
        soc.close();
        Br.close();
        }
    
    
    public static void main(String[] args) {
        Client c=new Client();
        try{
            c.start("localhost", 6728);
            c.run();
        }catch(IOException e){
            System.out.println("error during conection with the server"+e.getMessage());
        }finally {
            try {
                c.stopConnection(); 
            } catch (IOException ex) {
                System.out.println("Error closing connection: " + ex.getMessage());
            }
        }
        
    }
}
    
