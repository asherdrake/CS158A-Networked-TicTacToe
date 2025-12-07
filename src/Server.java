import java.util.Collections;
import java.net.Socket;
import java.net.ServerSocket;
import java.util.Map;
import java.util.HashMap;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

public class Server {
    private static final int PORT = 12345;
    private Map<String, Room> rooms = Collections.synchronizedMap(new HashMap<>());

    public static void main(String[] args) {
        new Server().start();
    }

    private void start() {
        try (ServerSocket ss = new ServerSocket(PORT)) {
            while (true) {
                Socket client = ss.accept();
                ClientHandler handler = new ClientHandler(client);
                handler.start();
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        }
    }

    private class Room {

    }

    private class ClientHandler extends Thread {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private String playerName;
        private Room room;

        public ClientHandler(Socket socket) {
            this.socket = socket;
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        }

        public void run() {
            try {
                send("ENTER_NAME");
                playerName = in.readLine();
                if (playerName == null) return;
                send("NAME_SET " + playerName);

                // Read Commands
                while (true) {
                    String line = in.readLine();
                    if (line == null) break;
                    String[] args = line.split(" ");
                    
                    if (args.length > 0) {
                        switch (args[0]) {
                            case "C":
                                // Create Room
                                break;
                            case "J":
                                // List Rooms
                                break;
                            case "JOIN":
                                // Join Room
                                break;
                            case "MOVE":
                                // Make Move
                                break;
                            default:
                                send("Invalid Command.");
                        }
                    }
                }
            } catch (IOException e) {
                System.err.println("Client disconnected: " + e.getMessage());
            } finally {
                try {
                    if (in != null) in.close();
                    if (out != null) out.close();
                    if (socket != null) socket.close();
                } catch (IOException e) {
                    // ignore errors during cleanup
                }
            }
        }

        public void send(String msg) {
            out.println(msg);
        }
    }
}