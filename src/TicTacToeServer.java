/* Listens on a port using ServerSocket.
Accepts incoming client connections.
For each accepted Socket, creates a ClientHandler thread.
Keeps track of waiting players and pairs them into games. */