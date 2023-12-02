import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author User
 */
public class IPS_Server {
    public static void main(String[] args) throws IOException {
        int port = 12347; // Choose the desired port number
        try (ServerSocket serverSocket = new ServerSocket(port)){
            System.out.println("Server listening on port " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                // Handle client request in a separate thread
                Thread thread = new Thread(() -> handleClientRequest(clientSocket));
                thread.start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        // TODO code application logic here
    }

    private static void handleClientRequest(Socket clientSocket) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
            String userInput = reader.readLine();

            if(userInput != null)
                System.out.println(userInput);

        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}