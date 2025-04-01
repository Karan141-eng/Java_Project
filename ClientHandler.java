import java.io.*;
import java.net.*;
import java.sql.*;

public class ClientHandler extends Thread {
    private Socket client;
    private BufferedReader networkBin;
    private OutputStreamWriter networkPout;
    private Connection conn;

    public ClientHandler(Socket c) {
        client = c;
    }

    public void run() {
        try {
            networkBin = new BufferedReader(new InputStreamReader(client.getInputStream()));
            networkPout = new OutputStreamWriter(client.getOutputStream());

            if (!handleLogin()) {
                networkPout.write("Login failed. Closing connection.\r\n");
                networkPout.flush();
                return;
            }

            networkPout.write("TIMER 300\r\n");
            networkPout.flush();

            sendQuestions();

        } catch (IOException ioe) {
            System.err.println(ioe);
        } finally {
            try {
                if (networkBin != null) networkBin.close();
                if (networkPout != null) networkPout.close();
                if (client != null) client.close();
                if (conn != null) conn.close();
            } catch (IOException | SQLException e) {
                System.err.println(e);
            }
        }
    }

    private boolean handleLogin() {
        try {
            conn = DriverManager.getConnection("jdbc:mysql://localhost/tutorialspoint", "root", "123456");
            System.out.println("Database connection successful!");

            String username = networkBin.readLine();
            String password = networkBin.readLine();

            String query = "SELECT username, password FROM Users WHERE username = ? AND password = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, username);
                stmt.setString(2, password);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    String dbUsername = rs.getString("username");
                    String dbPassword = rs.getString("password");
                    System.out.println("Fetched from DB - Username: " + dbUsername + ", Password: " + dbPassword);

                    if (username.equals(dbUsername) && password.equals(dbPassword)) {
                        networkPout.write("Login successful!\r\n");
                        networkPout.flush();
                        return true;
                    }
                }
            }

            networkPout.write("Invalid credentials.\r\n");
            networkPout.flush();
            return false;
        } catch (SQLException | IOException e) {
            System.err.println("Login error: " + e);
            return false;
        }
    }

    private void sendQuestions() {
        try {
            String query = "SELECT id, question, option_a, option_b, option_c, option_d, correct_answer, category, time_limit FROM QuizQuestions";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            int score = 0;

            while (rs.next()) {
                String question = rs.getString("question");
                String optionA = rs.getString("option_a");
                String optionB = rs.getString("option_b");
                String optionC = rs.getString("option_c");
                String optionD = rs.getString("option_d");
                String correctAnswer = rs.getString("correct_answer");
                int timeLimit = rs.getInt("time_limit");

                networkPout.write("QUESTION " + question + "\r\n");
                networkPout.write("A: " + optionA + "\r\n");
                networkPout.write("B: " + optionB + "\r\n");
                networkPout.write("C: " + optionC + "\r\n");
                networkPout.write("D: " + optionD + "\r\n");
                networkPout.write("TIME_LIMIT " + timeLimit + "\r\n");
                networkPout.flush();

                String response = networkBin.readLine();

                if (response.equalsIgnoreCase("FINISH")) {
                    break;
                }

                if (response.equalsIgnoreCase(correctAnswer)) {
                    score++;
                }
            }

            networkPout.write("TEST_COMPLETED\r\n");
            networkPout.write("Your score: " + score + "\r\n");
            networkPout.flush();

        } catch (SQLException | IOException e) {
            System.err.println("Error while sending questions: " + e);
        }
    }
}
