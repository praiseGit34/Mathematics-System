package server;
import java.net.*;
import java.io.*;
import java.sql.*;
import java.sql.Date;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import java.util.*;
import javax.mail.*;
import javax.mail.PasswordAuthentication;
import javax.mail.internet.*;

public class Clienthandler extends Thread{
    private final Socket soc;
    private Map<String, Object>[] totalQuestions;
    private final  Connection con;
    private static PrintWriter out;
    private static int participantID;
    private static boolean isSchoolRepresentative;
    private static Object school_Registration_Number;
    private static Object currentSchoolRepresentativeEmail;
    private static Object currentSchoolRepresentativePassword;
    private static BufferedReader reader;
    private static String totalMarks;
    private static String timeTakenInSeconds;

    public Clienthandler(Socket soc, Connection con) {
        this.soc = soc;
        this.con = con;
    }

    public void run() {
        try {
            out = new PrintWriter(soc.getOutputStream(), true);
            reader = new BufferedReader(new InputStreamReader(soc.getInputStream()));
    
            String inputLine;
            while ((inputLine = reader.readLine()) != null) {
                System.out.println("Received from client: " + inputLine);
                String response = processRequest(inputLine);
                sendMessage(response);
                System.out.println("Sent to client: " + response); // Log the response
            }
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                soc.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private Object processRequest(String request) throws SQLException {
        String[] part = request.split(" ");
        String action = part[0].toUpperCase();

        if (action.startsWith("REGISTER")) {
            if (part.length < 8) {
                return "Invalid registration format. Use: REGISTER username first_name last_name email date_of_birth school_registration_number image_file.png";
            }
            try {
                return registerUser(part);
            } catch (SQLException e) {
                e.printStackTrace();
                return "Error registering user.";
            }
        } else if (action.startsWith("LOGIN")) {
            if (part.length == 3) {
                return loginUser(part[1], part[2]);
            } else if (part.length == 2 && part[1].contains("@")) {
                return getRepresentativePassword(part[1]);
            } else {
                return "Invalid login format.";
            }
        } else if (action.startsWith("LOGOUT")) {
            return logoutUser();
        } else if (action.startsWith("VIEWCHALLENGES")) {
            return viewChallenges();
        } else if (action.startsWith("ATTEMPTCHALLENGE")) {
            if (part.length != 2) {
                return "Invalid command format. Use: ATTEMPTCHALLENGE challengeNumber";
            }
            return attemptChallenge(part[1]);
        } else if (action.startsWith("VIEWAPPLICANTS")) {
            return viewApplicants();
        } else if (action.startsWith("CONFIRMAPPLICANT")) {
            if (part.length != 3) {
                return "Invalid command format. Use: CONFIRMAPPLICANT yes/no username";
            }
            return confirmApplicant(part[1], part[2]);
        } else {
            return "Invalid request";
        }
    }

//getting representative email from the schools table 
private String getRepresentativePassword(String email) {
        try {
            String query = "SELECT representative_password FROM schools WHERE representative_email = ?";
            PreparedStatement stmt = con.prepareStatement(query);
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
                
            if (rs.next()) {
                return rs.getString("representative_password");
            } else {
                return "Representative password not found for email: " + email;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "Error fetching representative password.";
        } }

//method to handle the registration command
private String registerUser(String[] part) throws SQLException {
    if (part.length <8) {
        sendMessage("Invalid registration format please. Use: Register username first_name last_name email date_of_birth school_registration_number image_file.png");
        return "register correctly";}
    String dateOfBirth = part[5].replace('/', '-');
    // validate date format yyyy-mm-dd 
    if (!dateOfBirth.matches("\\d{4}/\\d{2}/\\d{2}")) {
    System.out.println("Invalid date format.");
    }
    //check if the school registration number exists
    String checkQuery = "SELECT * FROM schools WHERE registration_number = ?";
    PreparedStatement st = con.prepareStatement(checkQuery);
    st.setString(1, part[6].trim());
    ResultSet rs = st.executeQuery();
    if (!rs.next()) {    
    return "School registration number not found";
    }
    String username = part[1];
    String firstName = part[2];
    String lastName = part[3];
    String emailAddress = part[4];
    String date_of_birth = part[5];
    String school_registration_number = part[6];
    String image_file = part[7];

    // Check if username or email already exists
    if (checkUserExists(username, emailAddress)) {
        return "User with this username or email already exists.";
    }

    // Insert user data into 'users' table
    String insertQuery = "INSERT INTO users (username, first_name, last_name, email, date_of_birth, school_reg_number, image_file_name) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?)";
    try (PreparedStatement pstmt = con.prepareStatement(insertQuery)) {
        pstmt.setString(1, username);
        pstmt.setString(2, firstName);
        pstmt.setString(3, lastName);
        pstmt.setString(4, emailAddress);
        pstmt.setString(5, date_of_birth);
        pstmt.setString(6, school_registration_number);
        pstmt.setString(7, image_file);
        
        int rowsAffected = pstmt.executeUpdate();
        if (rowsAffected > 0) {
            return "Registration successful for " + username;
        } else {
            return "Registration failed. Please try again.";
        }
    }
}
private boolean checkUserExists(String username, String email) throws SQLException {
    String query = "SELECT COUNT(*) AS count FROM users WHERE username = ? OR emailAddress = ?";
    try (PreparedStatement pstmt = con.prepareStatement(query)) {
        pstmt.setString(1, username);
        pstmt.setString(2, email);

        ResultSet rs = pstmt.executeQuery();
        if (rs.next()) {
            int count = rs.getInt("count");
            return count > 0;
        }
    }
    return false;
}


// Sending response to the client
private static void sendMessage(String message) {
    out.println(message);
    out.flush(); // Ensure all data is flushed and sent
}
// Method to handle user login
private static String loginUser(String username, String password) {
    try {
        String query = "SELECT * FROM users WHERE userName = ? AND password = ?";
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

  // Method to handle user logout
private static String logoutUser() {
    if (isAuthenticated()) {
        participantID = 0;
        isSchoolRepresentative = false;
        school_Registration_Number = null;
        currentSchoolRepresentativeEmail = null;
        currentSchoolRepresentativePassword = null;
        return "Logged out successfully.";
    } else {
        return "No user is currently logged in.";
    }
}

private static boolean isAuthenticated() {
    return participantID != 0 || (isSchoolRepresentative && school_Registration_Number != null);
}

//confirm applicant logic
private static String confirmApplicant(String decision, String username) {
    
    if (!isSchoolRepresentative) {
        return "You don't have permission to confirm applicants.";
    }
    boolean isApproved = decision.equalsIgnoreCase("yes");
    String targetTable = isApproved ? "Participant" : "Rejected";
    try {
        con.setAutoCommit(false);//disabling
        // Get applicant details
        String selectSql = "SELECT * FROM Applicant WHERE userName = ? AND school_Registration_Number = ?";
        try (PreparedStatement selectStmt = con.prepareStatement(selectSql)) {
            selectStmt.setString(1, username);
            selectStmt.setString(2, (String) school_Registration_Number);
            ResultSet rs = selectStmt.executeQuery();
            if (!rs.next()) {
                con.rollback();
                return "No applicant found with username: " + username;
            }
            int applicantID = rs.getInt("applicantID");

            // Insert into target table
            String query;
            if (isApproved) {
                query = "INSERT INTO Participant (applicantID, firstName, lastName, emailAddress, dateOfBirth, school_Registration_NUmber, userName, imagePath, password) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            } else {
                query = "INSERT INTO Rejected (firstName, lastName, emailAddress, dateOfBirth, school_Registration_Number, userName, imagePath, password) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            }

            try (PreparedStatement insertStmt = con.prepareStatement(query)) {
                if (isApproved) {
                    insertStmt.setInt(1, applicantID);
                    insertStmt.setString(2, rs.getString("firstName"));
                    insertStmt.setString(3, rs.getString("lastName"));
                    insertStmt.setString(4, rs.getString("emailAddress"));
                    insertStmt.setDate(5, rs.getDate("dateOfBirth"));
                    insertStmt.setInt(6, rs.getInt("school_Registration_Number"));
                    insertStmt.setString(7, rs.getString("userName"));
                    insertStmt.setString(8, rs.getString("imagePath"));
                    insertStmt.setString(9, rs.getString("password"));
                } else {
                    insertStmt.setInt(1, applicantID);
                    insertStmt.setString(2, rs.getString("firstName"));
                    insertStmt.setString(3, rs.getString("lastName"));
                    insertStmt.setString(4, rs.getString("emailAddress"));
                    insertStmt.setDate(5, rs.getDate("dateOfBirth"));
                    insertStmt.setInt(6, rs.getInt("school_Registration_Number"));
                    insertStmt.setString(7, rs.getString("userName"));
                    insertStmt.setString(8, rs.getString("imagePath"));
                    insertStmt.setString(9, rs.getString("password"));
                }

                insertStmt.executeUpdate();
            }
            // Delete from Applicant table
            String deleteSql = "DELETE FROM Applicant WHERE applicantID = ?";
            try (PreparedStatement deleteStmt = con.prepareStatement(deleteSql)) {
                deleteStmt.setInt(1, applicantID);
                deleteStmt.executeUpdate();
            }
            con.commit();

            // Send email notification (implement this method separately)
            sendEmailNotification(rs.getString("emailAddress"), isApproved);

            return "Applicant " + username + " has been " + (isApproved ? "accepted" : "rejected") + ".";
        }
    } catch (SQLException e) {
        try {
            con.rollback();
        } catch (SQLException rollbackEx) {
            rollbackEx.printStackTrace();
        }
        e.printStackTrace();
        return "Error confirming applicant: " + e.getMessage();
    } finally {
        try {
            con.setAutoCommit(true);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
//send email notification  logic
private static void sendEmailNotification(String username, boolean isConfirmed) {
    String host = "smtp.gmail.com";
        final String email = "praiseasiimire38@gmail.com";
        final String password = "xoyngpbxudrzespd";

    Properties props = new Properties();
    props.put("mail.smtp.auth", "true");//Stmp authentication
    props.put("mail.smtp.starttls.enable", "true");//enable the encryption
    props.put("mail.smtp.host", host);//sets the server 
    props.put("mail.smtp.port", "587");//enables/sets the stmp server port

    javax.mail.Session session = Session.getInstance(props, new javax.mail.Authenticator() {
        protected PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(email, password);
        }
    });

    try {
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(email));
        // Fetch the representative's email from the database
       String representativeEmail = getRepresentativeEmail(username);
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse("mepraise2003@gmail.com"));
        
        if (isConfirmed) {
            message.setSubject("Participant Confirmation");
            message.setText("Dear Representative,\n\nApplicant with username: " + username + " has been registered.Please preview and confirm");
        }else{
        message.setSubject("Application Rejection");
        message.setText("Dear Representative,\n\nApplicant with username: " + username + " has been rejected.");
   
        }
        Transport.send(message);
        System.out.println("confirmation email sent successfully");
        
        }catch (MessagingException e) {
        throw new RuntimeException(e);
    
        }}
        

        //fetching the representative email from the schools table
    private static String getRepresentativeEmail(String username) {
        try {
            String query = "SELECT representative_email FROM schools WHERE schoolregistrationumber = " +
                               "(SELECT school_registration_number FROM users WHERE userName = ?)";
            PreparedStatement stmt = con.prepareStatement(query);
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
                
            if (rs.next()) {
                String representativeEmail = rs.getString("representative_email");
                return representativeEmail;
                } else {
                    return "Representative email not found for user: " + username;
                }
            } catch (SQLException e) {
                e.printStackTrace();
                return "Error fetching representative email.";
            }
        }
        

    private static String viewChallenges() {
        try {
            StringBuilder sb = new StringBuilder();
            String query = "SELECT * FROM challenges WHERE end_date>=NOW()";
            PreparedStatement st = con.prepareStatement(query);
            ResultSet rs = st.executeQuery();
    
            while (rs.next()) {
                int challengeNumber = rs.getInt("challenge_number");
                String challengeName = rs.getString("challenge_name");
                Date startDate = rs.getDate("start_date");
                Date endDate = rs.getDate("end_date");
                sb.append("Challenge ").append(challengeNumber).append(": ").append(challengeName)
                  .append(", Start Date: ").append(startDate).append(", End Date: ").append(endDate).append("\n");
            }
    
            if (sb.length() == 0) {
                return "No challenges currently open.";
            } else {
                return sb.toString();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "Error retrieving challenges.";
        }
    }
    
 // Method to handle viewing applicants
private static String viewApplicants() {
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

// Method to handle attempting a challenge
private static Object attemptChallenge(String challengeNumber ) {
// Check if the participant is authenticated and not a school representative
 if (!isAuthenticated() || isSchoolRepresentative) {
       return "You must be logged in as a participant to attempt a challenge.";
   }
   try {
       int challengeNo = Integer.parseInt(challengeNumber);
       
       // Fetch challenge details
       String checkOpenChallenge = "SELECT * FROM Challenge WHERE challengeNo = ? AND openDate <= CURDATE() AND closeDate >= CURDATE()";
       try (PreparedStatement ps = con.prepareStatement(checkOpenChallenge)) {
           ps.setInt(1, challengeNo);
           ResultSet rs = ps.executeQuery();
           if (!rs.next()) {
               return "Challenge is not open or does not exist.";
           }
           
           String challengeName = rs.getString("challengeName");
           String attemptDurationStr = rs.getString("attemptDuration");
           int totalQuestions = rs.getInt("noOfQuestions");
           
           LocalTime attemptDuration = LocalTime.parse(attemptDurationStr, DateTimeFormatter.ofPattern("HH:mm:ss"));
           long durationInSeconds = attemptDuration.toSecondOfDay();

           // Check if the participant has already exceeded maximum attempts
           if (hasExceededAttempts(challengeNo)) {
               return "You have already attempted this challenge 3 times.";
           }
           
           // Fetch random questions for the challenge from database
           List<Map<String, Object>> questions = fetchRandomQuestions(challengeNo);
           String description = String.format("Challenge: %s\nDuration: %s",
           challengeName, attemptDuration.toString());
           out.println(description);
           out.flush();
           
           // Wait for participant to confirm starting the challenge
           String startResponse = reader.readLine();
           if (!startResponse.equalsIgnoreCase("start")) {
               return "Challenge cancelled.";
           }
           int attemptID = storeAttempt(challengeNo);
           return conductChallenge(questions, durationInSeconds, attemptID);
       }
   } catch (SQLException | IOException e) {
       System.err.println("Error during challenge attempt: " + e.getMessage());
       e.printStackTrace();
       return "Error during challenge attempt: " + e.getMessage();
   }
}

// Method to conduct the challenge
@SuppressWarnings("null")
private static Object conductChallenge(List<Map<String, Object>> questions, long durationInSeconds, int attemptID) {
   
    int totalQuestions = questions.size();
    int remainingQuestions = totalQuestions;
    int score = 0;
    long startTime = System.currentTimeMillis();
    // Iterate through each question
    for (Map<String, Object> question : questions) {
        String questionText = (String) question.get("question");
        String answer = (String) question.get("answer");
        int marks = (int) question.get("marks");
        //Display question information to the participant
        out.println(String.format("Remaining questions: %d\nTime remaining: %d seconds\n%s"+remainingQuestions, durationInSeconds, questionText));
        String participantAnswer=null;
  // Calculate score based on the answer
  if (participantAnswer.equalsIgnoreCase(answer)) {
     score += marks;
  } else if (participantAnswer.equals("-")) {
     score += 0; // No marks awarded if participant is unsure
  } else {
     score -= 3; // Deduct 3 marks for wrong answer
  }
  remainingQuestions--;
  // Check if time is up
  long currentTime = System.currentTimeMillis();
  long elapsedTime = (currentTime - startTime) / 1000;
  if (elapsedTime >= durationInSeconds) {
     break; // End challenge if time limit exceeded
  }
  return "Challenge completed.\nTotal marks: " + totalMarks + "\nTime taken: " + timeTakenInSeconds + " seconds";
}
        return currentSchoolRepresentativeEmail;
}
              
private void saveAttemptResult(int attemptID, long startTime, int totalScore, double percentageMark) throws SQLException {
String updateSql = "UPDATE ChallengeAttempt SET totalMarks = ?, attemptEndTime = NOW(), timeTaken = ? WHERE attemptID = ?";
PreparedStatement ps = con.prepareStatement(updateSql);
int totalMarks = 0;
for (Map<String, Object> question : totalQuestions) {
    totalMarks += (int) question.get("marks");
}

// Set parameters for the prepared statement
ps.setInt(1, totalMarks); // Set totalMarks
long timeTakenInSeconds = 120; // Example: you should calculate the actual time taken
ps.setLong(2, timeTakenInSeconds); // Set timeTakenInSeconds
ps.setInt(3, attemptID); // Set attemptID

// Execute the update query
int rowsAffected = ps.executeUpdate();
if (rowsAffected > 0) {
    System.out.println("Update successful.");
} else {
    System.out.println("Update failed.");
}}
    
private static int storeAttempt(int challengeNo) {
    try {
        String insertSql = "INSERT INTO ChallengeAttempt (challengeNo, participantID, attemptStartTime) VALUES (?, ?, NOW())";
        PreparedStatement ps = con.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);
        ps.setInt(1, challengeNo);
        ps.setInt(2, participantID); 
        ps.executeUpdate();

        ResultSet rs = ps.getGeneratedKeys();
        if (rs.next()) {
            // Returns the attemptID
            return rs.getInt(1); 
        } else {
            return -1; // Error in getting the generated key
        }
    } catch (SQLException e) {
        e.printStackTrace();
        return -1; // Error occurred
    }
}

private static List<Map<String, Object>> fetchRandomQuestions(int challengeNo) throws SQLException {
String questionSql = "SELECT q.questionNo, q.question, a.answer, a.marksAwarded " +
                     "FROM Question q JOIN Answer a ON q.questionNo = a.questionNo " +
                     "WHERE q.questionBankID = (SELECT questionBankID FROM Challenge WHERE challengeNo = ?) " +
                     "ORDER BY RAND() LIMIT 10";
List<Map<String, Object>> questions = new ArrayList<>();
try (PreparedStatement questionStmt = con.prepareStatement(questionSql)) {
    questionStmt.setInt(1, challengeNo);
    ResultSet questionRs = questionStmt.executeQuery();
    while (questionRs.next()) {
        Map<String, Object> question = new HashMap<>();
        question.put("questionNo", questionRs.getInt("questionNo"));
        question.put("question", questionRs.getString("question"));
        question.put("answer", questionRs.getString("answer"));
        question.put("marks", questionRs.getInt("marksAwarded"));
        questions.add(question);
    }
}
return questions; 
}

private static boolean hasExceededAttempts(int challengeNo) {
try {
    String query = "SELECT COUNT(*) AS attempts FROM ChallengeAttempt WHERE challengeNo = ? AND participantID = ?";
    PreparedStatement ps = con.prepareStatement(query);
    ps.setInt(1, challengeNo);
    ps.setInt(2, participantID);
    ResultSet rs = ps.executeQuery();
    
    if (rs.next()) {
        int attempts = rs.getInt("attempts");
        return attempts >= 3; // Return true if attempts are equal or more than 3
    } else {
        return false; // No attempts found
    }
} catch (SQLException e) {
    e.printStackTrace();
    return false; // Error occurred
}}}
