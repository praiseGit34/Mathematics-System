package Client;
import java.io.*;
import java.net.*;

public class Client {
    private Socket soc;
    private BufferedReader userInputReader;
    private PrintWriter serverOutput;
    private BufferedReader serverInput;

    public void start(String localhost, int port) throws IOException {
        soc = new Socket(localhost, port);
        userInputReader = new BufferedReader(new InputStreamReader(System.in));
        serverOutput = new PrintWriter(soc.getOutputStream(), true);
        serverInput = new BufferedReader(new InputStreamReader(soc.getInputStream()));
    }

    public void run() {
        try {
            while (true) {
                System.out.println("WELCOME TO THE MATHEMATICS CHALLENGE AND COMPETITION SYSTEM");
                System.out.println("Commands: ");
                System.out.println("  Register username lastname firstname emailAddress date_of_birth school_registration_number image_file.png");
                System.out.println("  ViewChallenges - displays the challenges");
                System.out.println("  Attempt challenge challenge_number");
                System.out.println("  View applicants");
                System.out.println("  Confirm yes/no username");
                System.out.println("  Log in");
                System.out.println("  Log out");
                System.out.println("Enter a command: ");

                String command = userInputReader.readLine().trim();
                serverOutput.println(command);

                if (command.equalsIgnoreCase("exit")) {
                    break;
                }

                executeInput(command);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void executeInput(String command) throws IOException {
    serverOutput.println(command);
    String response = receiveMessage();
    System.out.println("Server response: " + response);
        String[] parts = command.split("\\s+");
        if (command.startsWith("register")) {
            if (parts.length < 8) {
                System.out.println("Invalid registration format. Use: Register username firstname lastname emailAddress date_of_birth school_registration_number image_file.png");
            } else {
                register(parts);
            }
        } else if (command.startsWith("login")) {
            if (parts.length == 3) {
                login(parts[1], parts[2]);
            } else if (parts.length == 2 && parts[1].contains("@")) {
                loginSchoolRepresentative(parts[1]);
            } else {
                System.out.println("Invalid login format. Use: login username password (for regular users) or login email@school.com (for school representatives)");
            }
        } else if (command.startsWith("viewChallenges")) {
            viewChallenges();
        } else if (command.startsWith("attempt challenge")) {
            if (parts.length == 3) {
                attemptChallenge(parts[2]);
            } else {
                System.out.println("Invalid format. Use: attempt challenge challenge_number");
            }
        } else if (command.startsWith("view applicants")) {
            viewApplicants();
        } else if (command.startsWith("confirm")) {
            if (parts.length == 3 && (parts[1].equalsIgnoreCase("yes") || parts[1].equalsIgnoreCase("no"))) {
                confirmApplicant(parts[1], parts[2]);
            } else {
                System.out.println("Invalid format. Use: confirm yes/no username");
            }
        } else if (command.equalsIgnoreCase("logout")) {
            logout();
        } else {
            System.out.println("Command not recognized");
        }
    }

    private void register(String[] parts) throws IOException {
        String response = sendMessage("REGISTER " + String.join(" ", parts));
        System.out.println(response);
    }

    private void login(String username, String password) throws IOException {
        String response = sendMessage("LOGIN " + username + " " + password);
        System.out.println(response);
    }

    private void loginSchoolRepresentative(String email) throws IOException {
        String response = sendMessage("LOGIN " + email);
        System.out.println(response);
    }

    private void viewChallenges() throws IOException {
        String response = receiveMessage();
        System.out.println(response);
    }

    private void attemptChallenge(String challengeNumber) throws IOException {
        String response = receiveMessage();
        System.out.println(response);

        String prompt = serverInput.readLine();
        System.out.println(prompt);

        System.out.println("Press Enter to start the challenge...");
        userInputReader.readLine();

        serverOutput.println("start");

        String line;
        while ((line = serverInput.readLine()) != null) {
            if (line.equals("END_OF_CHALLENGE")) {
                break;
            }
            System.out.println(line);

            if (line.startsWith("Enter your answer")) {
                System.out.print("Your answer: ");
                String answer = userInputReader.readLine().trim();
                serverOutput.println(answer);
            }

            if (line.equals("oh sorry!,time is done")) {
                System.out.println("Challenge ended due to time");
                break;
            }
        }

        String result = serverInput.readLine();
        System.out.println(result);
    }

    private void viewApplicants() throws IOException {
        String response = receiveMessage();
        System.out.println(response);
    }

    private void confirmApplicant(String decision, String username) throws IOException {
        String response = sendMessage("CONFIRM " + decision + " " + username);
        System.out.println(response);
    }

    private void logout() throws IOException {
        String response = sendMessage("LOGOUT");
        System.out.println(response);
    }

    private String sendMessage(String message) throws IOException {
        serverOutput.println(message);
        return receiveMessage();
    }

    private String receiveMessage() throws IOException {
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = serverInput.readLine()) != null) {
            if (line.isEmpty()) {
                break;
            }
            response.append(line).append("\n");
        }
        return response.toString().trim();
    }

    public void stopConnection() throws IOException {
        serverOutput.close();
        userInputReader.close();
        soc.close();
        serverInput.close();
    }

    public static void main(String[] args) {
        Client client = new Client();
        try {
            client.start("localhost", 6728);
            client.run();
        } catch (IOException e) {
            System.out.println("Error during connection with the server: " + e.getMessage());
        } finally {
            try {
                client.stopConnection();
            } catch (IOException ex) {
                System.out.println("Error closing connection: " + ex.getMessage());
            }
        }
    }
}
