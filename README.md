
# Online Examination System

## Overview
The **Online Examination System** is a Java-based application designed to conduct online exams efficiently. It leverages Java Swing for the user interface, JDBC for database connectivity, multithreading for handling multiple users simultaneously, and networking for remote exam access.

## Features
- **User Authentication**: Secure login for students and administrators.
- **Exam Management**: Create, update, and delete exams.
- **Question Bank**: Store and retrieve questions from a database.
- **Real-time Exam Monitoring**: Handle multiple candidates using multithreading.
- **Networking Support**: Remote access for students via network communication.
- **Result Processing**: Automatic evaluation and score calculation.

## Technologies Used
- **Java Swing**: GUI development
- **JDBC**: Database connectivity
- **Multithreading**: Concurrent exam handling
- **Networking (Sockets/RMI)**: Remote exam access
- **MySQL**: Database management

## Installation
### Prerequisites
- Java Development Kit (JDK 18)
- MySQL Database
- IDE (Eclipse/IntelliJ IDEA/NetBeans)

### Steps to Setup
1. **Clone the Repository**
   ```sh
   git clone https://github.com/Karan141-eng/Java_Project
   ```
2. **Import the Project into an IDE**
3. **Configure Database**
   - Create a database (`tutorialspoint`).
   - Create table (`Users`) with column name (`id`, `username`, `password`)
   - Create another table (`QuizQuestions`)
   - For inserting values into table see screenshots
4. **Compile and Run the Application**
   ```sh
   javac Main.java
   java Main or simply click on Run button
   ```

## Usage
1. **Student Login**
   - Take exams.
   - View scores after submission.
2. **Multithreading & Networking**
   - Handles multiple students taking the exam concurrently.
   - Supports remote exam-taking over a network.

## Screenshots
- Here's screenshots(s1, s2, s3, s4, s5, s6)

## Future Enhancements
- Implement a timer for exams.
- Add email notifications for results.
- Enhance security with encryption.
- Integrate a reporting module for detailed analytics.

## Contributors
- [Karan Kanoujia]
- [Nitin Singh]

## License
This project is licensed under NIT Kurukshetra.

---





