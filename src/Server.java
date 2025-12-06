import java.util.Collections;
import java.net.Socket;
import java.util.Map;
import java.util.HashMap;

public class Server {
    private static final int PORT = 12345;
    private Map<String, Room> rooms = Collections.synchronizedMap(new HashMap<>());

    public static void main(String[] args) {
        new Server().start();
    }

    private void start() {

    }

    private class Room {

    }

    private class ClientHandler extends Thread {

    }
}