import java.net.*;
import java.io.*;

public class Server {
    private static final String IP = "127.0.0.1";
    private static final int PORT = 12345;

    public static void main(String[] args) throws IOException {
        ServerSocket sock = null;
        try {
            sock = new ServerSocket(PORT, 50, InetAddress.getByName(IP));
            System.out.println("Server started at " + IP + ":" + PORT);

            while (true) {
                Socket client = sock.accept();
                ClientHandler c = new ClientHandler(client);
                c.start();
            }
        } catch (IOException ioe) {
            System.err.println(ioe);
        } finally {
            if (sock != null) sock.close();
        }
    }
}
