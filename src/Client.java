import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private static final String SERVER = "localhost";
    private static final int PORT = 12345;

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private Scanner scanner;

    // Client state
    private String playerName;
    private boolean myTurn;
    private String currentBoard = "";
    private String mySymbol;
    private String opponentName;
    private String opponentSymbol;
    private boolean inGame;

    public static void main(String[] args) {
        new Client().start();
    }

    private void start() {
        try {
            // Connect to server
            socket = new Socket(SERVER, PORT);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            scanner = new Scanner(System.in);

            // Start listener thread for server messages
            Thread listenerThread = new Thread(() -> listen());
            listenerThread.start();

            // Main input loop - read user commands
            while (scanner.hasNextLine()) {
                String input = scanner.nextLine().trim();
                if (!input.isEmpty()) {
                    out.println(input);
                }
            }
        } catch (IOException e) {
            System.err.println("Connection error: " + e.getMessage());
        } finally {
            try {
                if (socket != null) socket.close();
                if (scanner != null) scanner.close();
            } catch (IOException e) {
                // Ignore cleanup errors
            }
        }
    }
    
    private void listen() {
        try {
            String line;
            while ((line = in.readLine()) != null) {
                handleMessage(line);
            }
        } catch (IOException e) {
            System.err.println("Connection lost: " + e.getMessage());
        }
    }

    private void handleMessage(String msg) {
        String[] parts = msg.split(" ", 2);
        String command = parts[0];

        switch (command) {
            case "ENTER_NAME":
                System.out.print("Enter your name: ");
                break;

            case "NAME_SET":
                playerName = parts.length > 1 ? parts[1] : "";
                System.out.println("Welcome, " + playerName + "!");
                System.out.println("Commands: CREATE <roomname>, LIST, JOIN <roomname>, MOVE <0-8>");
                break;

            case "ROOM_CREATED":
                System.out.println("Room created: " + parts[1]);
                System.out.println("Waiting for another player to join...");
                break;

            case "ROOMLIST":
                System.out.println("Available rooms:");
                break;

            case "JOINED":
                System.out.println("Joined room: " + parts[1]);
                break;

            case "WAITING":
                System.out.println("Waiting for another player...");
                break;

            case "GAME_START":
                // Format: GAME_START player1 symbol1 player2 symbol2
                String[] gameInfo = parts[1].split(" ");
                String player1 = gameInfo[0];
                char symbol1 = gameInfo[1].charAt(0);
                String player2 = gameInfo[2];
                char symbol2 = gameInfo[3].charAt(0);

                if (player1.equals(playerName)) {
                    mySymbol = String.valueOf(symbol1);
                    opponentName = player2;
                    opponentSymbol = String.valueOf(symbol2);
                } else {
                    mySymbol = String.valueOf(symbol2);
                    opponentName = player1;
                    opponentSymbol = String.valueOf(symbol1);
                }

                inGame = true;
                System.out.println("\n=== GAME STARTING ===");
                System.out.println("You are: " + mySymbol);
                System.out.println("Opponent: " + opponentName + " (" + opponentSymbol + ")");
                System.out.println();
                break;

            case "BOARD":
                // Board has 5 lines: row, separator, row, separator, row
                try {
                    StringBuilder boardBuilder = new StringBuilder();
                    for (int i = 0; i < 5; i++) {
                        String boardLine = in.readLine();
                        if (boardLine != null) {
                            boardBuilder.append(boardLine).append("\n");
                        }
                    }
                    currentBoard = boardBuilder.toString();
                    printBoard(currentBoard);
                } catch (IOException e) {
                    System.err.println("Error reading board: " + e.getMessage());
                }
                break;

            case "TURN":
                String currentPlayer = parts[1];
                myTurn = currentPlayer.equals(playerName);
                if (myTurn) {
                    System.out.println("\n>>> YOUR TURN! Enter MOVE <0-8>");
                } else {
                    System.out.println("\n>>> " + currentPlayer + "'s turn...");
                }
                break;

            case "GAME_OVER":
                String[] result = parts[1].split(" ", 2);
                System.out.println("\n=== GAME OVER ===");
                if (result[0].equals("DRAW")) {
                    System.out.println("It's a draw!");
                } else if (result[0].equals("WIN")) {
                    String winner = result[1];
                    if (winner.equals(playerName)) {
                        System.out.println("You won! Congratulations!");
                    } else {
                        System.out.println(winner + " wins!");
                    }
                }
                inGame = false;
                break;

            case "ERROR":
                System.out.println("ERROR: " + (parts.length > 1 ? parts[1] : "Unknown error"));
                break;

            default:
                // Assume it's a room name from ROOMLIST
                System.out.println("  - " + msg);
                break;
        }
    }

    private void printBoard(String board) {
        System.out.println("\nBoard positions:");
        System.out.println(" 0 | 1 | 2");
        System.out.println("-----------");
        System.out.println(" 3 | 4 | 5");
        System.out.println("-----------");
        System.out.println(" 6 | 7 | 8");
        System.out.println("\nCurrent board:");
        System.out.print(board);
    }
}