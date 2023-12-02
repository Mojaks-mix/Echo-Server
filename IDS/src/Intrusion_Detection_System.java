import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Intrusion_Detection_System {
    private static Attacks attacksObj = new Attacks();

    public static void main(String[] args) {
        int port = 8080, App_server_Port = 12346, IPS_server_Port = 12347; // Choose the desired port number
        String serverAddress = "localhost";
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        try (ServerSocket serverSocket = new ServerSocket(port)){
            System.out.println("Server listening on port " + port);

            while (true) {
                Socket App_Server_socket = new Socket(serverAddress, App_server_Port);
                Socket IPS_Server_socket = new Socket(serverAddress, IPS_server_Port);
                Socket clientSocket = serverSocket.accept();
                System.out.println("New connection established with client: " + clientSocket.getInetAddress());
                // Handle client request in a separate thread
                Thread thread = new Thread(() -> handleClientRequest(clientSocket,App_Server_socket,IPS_Server_socket));
                thread.start();
                executor.scheduleAtFixedRate(new Runnable() {
                    @Override
                    public void run() {
                        // Your if statement here
                        if(attacksObj.behavior() != null){
                            PrintWriter writer_IPS;
                            try {
                                writer_IPS = new PrintWriter(IPS_Server_socket.getOutputStream(), true);
                                writer_IPS.println("Suspicious Behavoir DOS Attack from user: "+attacksObj.behavior()+" .");
                            } catch (IOException ex) {
                                Logger.getLogger(Intrusion_Detection_System.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            // Code to be executed every 3 seconds
                        }
                    }
                }, 0, 3, TimeUnit.SECONDS);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleClientRequest(Socket clientSocket,Socket App_Server_socket,Socket IPS_Server_socket) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
            String userInput = reader.readLine();
            ConnectionData connection1 = new ConnectionData(clientSocket.getInetAddress(), clientSocket.getPort(),userInput);
            attacksObj.storeConnectionData(connection1);
            if(attacksObj.check(userInput)){
                PrintWriter writer_IPS = new PrintWriter(IPS_Server_socket.getOutputStream(), true);
                writer_IPS.println("Intrusion detected "+attacksObj.FILE_NAME+" Attack will be handled."+"Informations IP Address: "+clientSocket.getInetAddress()+" ,Connection Port: "+clientSocket.getPort()+" ,User Payload: "+userInput);
            }
            else{
                PrintWriter writer_APP = new PrintWriter(App_Server_socket.getOutputStream(), true);
                writer_APP.println(userInput+"\n");
            }

        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            try {
                clientSocket.close();
                App_Server_socket.close();
                IPS_Server_socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static class Attacks {
        public String FILE_NAME;

        private final String[] FILE_NAMES = {
                "C:\\Users\\User\\Desktop\\SQL_PAYLOAD.txt",
                "C:\\Users\\User\\Desktop\\XSS_PAYLOAD.txt",
                "C:\\Users\\User\\Desktop\\OS_PAYLOAD.txt"};

        private ConnectionData[] connectionDataArray;

        public Attacks() {
            connectionDataArray = new ConnectionData[0];
        }

        public void storeConnectionData(ConnectionData connectionData) {
            ConnectionData[] newArray = new ConnectionData[connectionDataArray.length + 1];
            System.arraycopy(connectionDataArray, 0, newArray, 0, connectionDataArray.length);
            newArray[connectionDataArray.length] = connectionData;
            connectionDataArray = newArray;
        }

        public boolean check(String user_payload) {
            for (String fileName : FILE_NAMES) {
                if (isStringInFile(fileName, user_payload)) {
                    return true; // Match found
                }
            }
            return false; // No match found
        }

        public void update(String attackName, String payload) {
            String fileName = getFileNameFromAttackName(attackName);
            if (fileName != null) {
                try (FileWriter writer = new FileWriter(fileName, true);
                     BufferedWriter bufferedWriter = new BufferedWriter(writer)) {
                    bufferedWriter.write(payload);
                    bufferedWriter.newLine();
                    System.out.println("Payload added successfully.");
                } catch (IOException e) {
                    System.out.println("An error occurred while updating the payload: " + e.getMessage());
                }
            } else {
                System.out.println("Invalid attack name.");
            }
        }

        public InetAddress behavior() {
            HashMap<InetAddress, Integer> hashMap = new HashMap<>();
            for (int i = 0; i < connectionDataArray.length; i++) {
                InetAddress key = connectionDataArray[i].getIpAddress();
                int currentValue = hashMap.getOrDefault(key, 0); // Get current value or use 0 if key not present
                int newValue = currentValue + 1;
                hashMap.put(key, newValue);
            }
            for(InetAddress key : hashMap.keySet()){
                if (hashMap.get(key) > 100){
                    return key;
                }
            }
            return null;
        }

        private boolean isStringInFile(String fileName, String searchString) {
            try (FileReader reader = new FileReader(fileName);
                 BufferedReader bufferedReader = new BufferedReader(reader)) {
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    if (line.contains(searchString) || searchString == line.trim() || searchString == line || line.toLowerCase().contains(searchString.toLowerCase())) {
                        FILE_NAME = getAttackNameFromAttackFileName(fileName);
                        return true;
                    }
                }
            } catch (IOException e) {
                System.out.println("An error occurred while reading the file: " + e.getMessage());
            }
            return false;
        }

        private String getFileNameFromAttackName(String attackName) {
            switch (attackName) {
                case "SQL":
                    return FILE_NAMES[0];
                case "XSS":
                    return FILE_NAMES[1];
                case "OS":
                    return FILE_NAMES[2];
                default:
                    return null;
            }
        }

        public String getAttackNameFromAttackFileName(String attackFileName) {
            switch (attackFileName) {
                case "C:\\Users\\User\\Desktop\\SQL_PAYLOAD.txt":
                    return "SQL Injection";
                case "C:\\Users\\User\\Desktop\\XSS_PAYLOAD.txt":
                    return "XSS";
                case "C:\\Users\\User\\Desktop\\OS_PAYLOAD.txt":
                    return "OS Injection";
                default:
                    return "";
            }
        }
    }

    public static class ConnectionData {
        private InetAddress ipAddress;
        private int port;
        private String userPayload;

        public ConnectionData(InetAddress ipAddress, int port, String userPayload) {
            this.ipAddress = ipAddress;
            this.port = port;
            this.userPayload = userPayload;
        }

        // Getters and setters for the variables (optional)

        public InetAddress getIpAddress() {
            return ipAddress;
        }

        public void setIpAddress(InetAddress ipAddress) {
            this.ipAddress = ipAddress;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public String getUserPayload() {
            return userPayload;
        }

        public void setUserPayload(String userPayload) {
            this.userPayload = userPayload;
        }
    }
}
