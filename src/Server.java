import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Server {
    private static final int PORT = 12345;
    protected Map<String, Room> rooms = Collections.synchronizedMap(new HashMap<>());

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
        public String name;
        private List<ClientHandler> players = new ArrayList<>();
        private char[] board = new char[9];
        private int currentPlayer;
        public boolean gameStarted;
        private char[] symbols = {'O', 'X'};

        public Room(String name) {
            this.name = name;
            for (int i = 0; i < board.length; i++) {
                board[i] = ' ';
            }
        }

        public synchronized void addPlayer(ClientHandler player) {
            if (!isFull()) {
                players.add(player);
                player.room = this;
                player.send("JOINED " + name);
            } else {
                player.send("ERROR Room is full.");
                return;
            }

            if (isFull()) {
                gameStart();
            } else {
                player.send("WAITING");
            }
        }

        public synchronized void gameStart() {
            gameStarted = true;
            String gameStartMsg = String.format("GAME_START %s %c %s %c", players.get(0).playerName, symbols[0], players.get(1).playerName, symbols[1]);
            broadcast(gameStartMsg);
            broadcastBoard();
            currentPlayer = 0;
            broadcast("TURN " + players.get(0).playerName);
        }

        public synchronized void makeMove(ClientHandler player, int pos) {
            // Verify it's the player's turn
            int playerIndex = players.indexOf(player);
            if (playerIndex != currentPlayer) {
                player.send("ERROR Not your turn.");
                return;
            }

            // Check if position is already occupied
            if (board[pos] != ' ') {
                player.send("ERROR Position already taken.");
                return;
            }

            // Make the move
            board[pos] = symbols[playerIndex];
            broadcastBoard();

            // Check for winner or draw
            char winner = checkWinner();
            if (winner != ' ') {
                if (winner == 'D') {
                    broadcast("GAME_OVER DRAW");
                } else {
                    // Find which player has this symbol
                    for (int i = 0; i < players.size(); i++) {
                        if (symbols[i] == winner) {
                            broadcast("GAME_OVER WIN " + players.get(i).playerName);
                            break;
                        }
                    }
                }
                gameStarted = false;
            } else {
                // Switch turns
                currentPlayer = 1 - currentPlayer;
                broadcast("TURN " + players.get(currentPlayer).playerName);
            }
        }

        public char checkWinner() {
            // All possible winning combinations (rows, columns, diagonals)
            int[][] winPatterns = {
                {0, 1, 2}, {3, 4, 5}, {6, 7, 8}, // rows
                {0, 3, 6}, {1, 4, 7}, {2, 5, 8}, // columns
                {0, 4, 8}, {2, 4, 6}             // diagonals
            };

            // Check each winning pattern
            for (int[] pattern : winPatterns) {
                char first = board[pattern[0]];
                if (first != ' ' && 
                    first == board[pattern[1]] && 
                    first == board[pattern[2]]) {
                    return first; // Return the winning symbol
                }
            }

            // Check for draw (board full)
            boolean boardFull = true;
            for (char cell : board) {
                if (cell == ' ') {
                    boardFull = false;
                    break;
                }
            }
            if (boardFull) {
                return 'D'; // Return 'D' for draw
            }

            return ' '; // Game continues
        }

        public synchronized boolean isFull() {
            return players.size() == 2;
        }

        public synchronized void broadcast(String msg) {
            for (ClientHandler player: players) {
                player.send(msg);
            }
        }

        public synchronized void broadcastBoard() {
            String boardString = formatBoard();
            broadcast("BOARD");
            broadcast(boardString);
        }
        public String formatBoard() {
            StringBuilder sb = new StringBuilder();
            sb.append(" ").append(board[0]).append(" | ").append(board[1]).append(" | ").append(board[2]).append(" \n");
            sb.append("-----------\n");
            sb.append(" ").append(board[3]).append(" | ").append(board[4]).append(" | ").append(board[5]).append(" \n");
            sb.append("-----------\n");
            sb.append(" ").append(board[6]).append(" | ").append(board[7]).append(" | ").append(board[8]).append(" ");
            return sb.toString();
        }
    }

    private class ClientHandler extends Thread {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        public String playerName;
        public Room room;

        public ClientHandler(Socket socket) {
            this.socket = socket;
            try {
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            } catch (IOException e) {
                System.err.println("Error creating ClientHandler: " + e.getMessage());
            }
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
                            case "CREATE": // Create Room
                                if (args.length != 2) {
                                    send("ERROR Invalid # of arguments.");
                                    break;
                                } 

                                Room newRoom = new Room(args[1]);
                                rooms.put(args[1], newRoom);
                                newRoom.addPlayer(this);
                                room = newRoom;
                                break;
                            case "LIST": // List Rooms
                                send("ROOMLIST");
                                for (Room r : rooms.values()) {
                                    send(r.name);
                                }
                                break;
                            case "JOIN": // Join Room
                                if (args.length != 2) {
                                    send("ERROR Invalid # of arguments.");
                                    break;
                                }

                                Room targetRoom = rooms.get(args[1]); 
                                if (targetRoom == null) {
                                    send("ERROR Room does not exist.");
                                    break;
                                }
                                
                                targetRoom.addPlayer(this);
                                room = targetRoom;
                                break;
                            case "MOVE": // Make Move
                                if (this.room == null) {
                                    send("ERROR Not in a room.");
                                    break;
                                } else if (!this.room.gameStarted) {
                                    send("ERROR Game hasn't started.");
                                    break;
                                } else if (args.length != 2) {
                                    send("ERROR Invalid # of arguments.");
                                    break;
                                }
                                int boardBox; 
                                try {
                                    boardBox = Integer.parseInt(args[1]);
                                    if (boardBox < 0 || boardBox > 8) {
                                        send("ERROR Invalid Move. Please use a number 0-8.");
                                        break;
                                    }
                                } catch (NumberFormatException e) {
                                    send("ERROR Invalid Move. Please use a number 0-8.");
                                    break;
                                }
                                
                                this.room.makeMove(this, boardBox);
                                break;
                            default:
                                send("ERROR Invalid Command.");
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