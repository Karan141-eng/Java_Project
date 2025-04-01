import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.TimerTask;
import java.util.Timer;

public class Client2 extends JFrame {
    private CardLayout cardLayout;
    private JPanel cardPanel;
    private LoginPanel loginPanel;
    private ExamPanel examPanel;
    private ScorePanel scorePanel;

    private Socket socket;
    private BufferedReader networkBin;
    private OutputStreamWriter networkPout;

    private String serverHost = "127.0.0.1";
    private int serverPort = 12345;

    public Client2() {
        setTitle("Online Examination App");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        loginPanel = new LoginPanel();
        examPanel = new ExamPanel();
        scorePanel = new ScorePanel();

        cardPanel.add(loginPanel, "Login");
        cardPanel.add(examPanel, "Exam");
        cardPanel.add(scorePanel, "Score");

        add(cardPanel);
    }

    private class LoginPanel extends JPanel {
        private JTextField usernameField;
        private JPasswordField passwordField;
        private JButton loginButton;
        private JLabel statusLabel;

        public LoginPanel() {
            setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.fill = GridBagConstraints.HORIZONTAL;

            JLabel userLabel = new JLabel("Username:");
            gbc.gridx = 0;
            gbc.gridy = 0;
            add(userLabel, gbc);

            usernameField = new JTextField(15);
            gbc.gridx = 1;
            add(usernameField, gbc);

            JLabel passLabel = new JLabel("Password:");
            gbc.gridx = 0;
            gbc.gridy = 1;
            add(passLabel, gbc);

            passwordField = new JPasswordField(15);
            gbc.gridx = 1;
            add(passwordField, gbc);

            loginButton = new JButton("Login");
            gbc.gridx = 0;
            gbc.gridy = 2;
            gbc.gridwidth = 2;
            add(loginButton, gbc);

            statusLabel = new JLabel("");
            gbc.gridy = 3;
            add(statusLabel, gbc);

            loginButton.addActionListener(e -> {
                new Thread(() -> {
                    try {
                        socket = new Socket(serverHost, serverPort);
                        networkBin = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        networkPout = new OutputStreamWriter(socket.getOutputStream());

                        final String username = usernameField.getText().trim();
                        final String password = new String(passwordField.getPassword()).trim();

                        networkPout.write(username + "\r\n");
                        networkPout.write(password + "\r\n");
                        networkPout.flush();

                        String response = networkBin.readLine();
                        if (response != null && response.contains("successful")) {
                            SwingUtilities.invokeLater(() -> {
                                statusLabel.setText("Login successful!");
                                examPanel.startExam();
                                cardLayout.show(cardPanel, "Exam");
                            });
                        } else {
                            SwingUtilities.invokeLater(() -> {
                                statusLabel.setText("Login failed. Please try again.");
                            });
                            socket.close();
                        }
                    } catch (IOException ex) {
                        SwingUtilities.invokeLater(() -> {
                            statusLabel.setText("Connection error: " + ex.getMessage());
                        });
                        ex.printStackTrace();
                    }
                }).start();
            });
        }
    }

    private class ExamPanel extends JPanel {
        private JTextArea questionArea;
        private JRadioButton optionA, optionB, optionC, optionD;
        private ButtonGroup optionsGroup;
        private JButton submitButton;
        private JButton finishTestButton;
        private JLabel timerLabel;

        private Timer examTimer;
        private int remainingTime;

        public ExamPanel() {
            setLayout(new BorderLayout());

            JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            timerLabel = new JLabel("Time remaining: ");
            topPanel.add(timerLabel);
            add(topPanel, BorderLayout.NORTH);

            JPanel centerPanel = new JPanel(new BorderLayout());
            questionArea = new JTextArea(5, 40);
            questionArea.setEditable(false);
            questionArea.setLineWrap(true);
            questionArea.setWrapStyleWord(true);
            centerPanel.add(new JScrollPane(questionArea), BorderLayout.NORTH);

            JPanel optionsPanel = new JPanel(new GridLayout(4, 1));
            optionA = new JRadioButton();
            optionB = new JRadioButton();
            optionC = new JRadioButton();
            optionD = new JRadioButton();

            optionsGroup = new ButtonGroup();
            optionsGroup.add(optionA);
            optionsGroup.add(optionB);
            optionsGroup.add(optionC);
            optionsGroup.add(optionD);

            optionsPanel.add(optionA);
            optionsPanel.add(optionB);
            optionsPanel.add(optionC);
            optionsPanel.add(optionD);

            centerPanel.add(optionsPanel, BorderLayout.CENTER);
            add(centerPanel, BorderLayout.CENTER);

            JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            submitButton = new JButton("Submit Answer");
            bottomPanel.add(submitButton);
            finishTestButton = new JButton("Finish Test");
            bottomPanel.add(finishTestButton);
            add(bottomPanel, BorderLayout.SOUTH);

            submitButton.addActionListener(e -> submitAnswer());

            finishTestButton.addActionListener(e -> {
                if (examTimer != null) {
                    examTimer.cancel();
                }
                SwingUtilities.invokeLater(() -> disableExamInputs());
                new Thread(() -> finishExam()).start();
            });
        }

        public void startExam() {
            try {
                String timerMsg = networkBin.readLine();
                if (timerMsg != null && timerMsg.startsWith("TIMER")) {
                    remainingTime = Integer.parseInt(timerMsg.split(" ")[1]);
                } else {
                    remainingTime = 300;
                }
                startTimer();

                new Thread(() -> readServerMessages()).start();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        private void startTimer() {
            examTimer = new Timer();
            examTimer.scheduleAtFixedRate(new TimerTask() {
                public void run() {
                    SwingUtilities.invokeLater(() -> timerLabel.setText("Time remaining: " + remainingTime + " seconds"));
                    remainingTime--;
                    if (remainingTime < 0) {
                        examTimer.cancel();
                        SwingUtilities.invokeLater(() -> disableExamInputs());
                        new Thread(() -> finishExam()).start();
                    }
                }
            }, 0, 1000);
        }

        private void readServerMessages() {
            try {
                String line;
                while ((line = networkBin.readLine()) != null) {
                    if (line.startsWith("QUESTION")) {
                        final String questionText = line.substring("QUESTION ".length());
                        SwingUtilities.invokeLater(() -> {
                            questionArea.setText(questionText);
                            optionsGroup.clearSelection();
                        });
                    } else if (line.startsWith("A:")) {
                        final String text = line;
                        SwingUtilities.invokeLater(() -> optionA.setText(text));
                    } else if (line.startsWith("B:")) {
                        final String text = line;
                        SwingUtilities.invokeLater(() -> optionB.setText(text));
                    } else if (line.startsWith("C:")) {
                        final String text = line;
                        SwingUtilities.invokeLater(() -> optionC.setText(text));
                    } else if (line.startsWith("D:")) {
                        final String text = line;
                        SwingUtilities.invokeLater(() -> optionD.setText(text));
                    } else if (line.startsWith("TIME_LIMIT")) {
                    } else if (line.startsWith("TEST_COMPLETED")) {
                        break;
                    }
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        private void submitAnswer() {
            try {
                String selectedAnswer = "";
                if (optionA.isSelected()) {
                    selectedAnswer = "A";
                } else if (optionB.isSelected()) {
                    selectedAnswer = "B";
                } else if (optionC.isSelected()) {
                    selectedAnswer = "C";
                } else if (optionD.isSelected()) {
                    selectedAnswer = "D";
                }
                if (!selectedAnswer.isEmpty()) {
                    networkPout.write(selectedAnswer + "\r\n");
                    networkPout.flush();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        private void finishExam() {
            try {
                networkPout.write("FINISH\r\n");
                networkPout.flush();

                String line;
                while ((line = networkBin.readLine()) != null) {
                    if (line.startsWith("Your score:")) {
                        final String finalScore = line;
                        SwingUtilities.invokeLater(() -> {
                            scorePanel.setScore(finalScore);
                            cardLayout.show(cardPanel, "Score");
                        });
                        break;
                    }
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        private void disableExamInputs() {
            submitButton.setEnabled(false);
            finishTestButton.setEnabled(false);
            optionA.setEnabled(false);
            optionB.setEnabled(false);
            optionC.setEnabled(false);
            optionD.setEnabled(false);
        }
    }

    private class ScorePanel extends JPanel {
        private JLabel scoreLabel;
        private JButton closeButton;

        public ScorePanel() {
            setLayout(new BorderLayout());
            scoreLabel = new JLabel("", SwingConstants.CENTER);
            scoreLabel.setFont(new Font("Arial", Font.BOLD, 24));
            add(scoreLabel, BorderLayout.CENTER);

            closeButton = new JButton("Close");
            closeButton.addActionListener(e -> System.exit(0));
            JPanel bottomPanel = new JPanel();
            bottomPanel.add(closeButton);
            add(bottomPanel, BorderLayout.SOUTH);
        }

        public void setScore(String scoreText) {
            scoreLabel.setText(scoreText);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Client app = new Client();
            app.setVisible(true);
        });
    }
}
