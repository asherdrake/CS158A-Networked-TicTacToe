import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.Scanner;
import java.net.Socket;

public class Client {
    private static final String SERVER = "localhost";
    private static final int PORT = 12345;

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private Scanner scanner;

    // client state
    private String playerName;
    private boolean myTurn;
    private boolean waitingForName;
    private boolean waitingForRoomIndex;
    private String currentBoard;
    private String mySymbol;
    private String opponentName;
    private String opponentSymbol;
    private String roomName;
    private boolean inGame;

    private static void main(String[] args) {
        new Client().start();
    }

    private void start() {

    }
    
    private void listen() {

    }

    private void clearScreen() {

    }

    private void redraw() {

    }

    private void handleMessage(String msg) {

    }

    private int mapInputToPosition(char c) {
        return 'a'; //pass
    }

    private void printBoard(String board) {

    }
}