# CS158A-Networked-TicTacToe

A simple networked Tic-Tac-Toe game using Java sockets. Two clients connect to a server and play against each other in the terminal.

## How to Compile

```powershell
javac src/Server.java src/Client.java
```

## How to Run

### 1. Start the Server (Terminal 1)
```powershell
java -cp src Server
```

### 2. Start First Client (Terminal 2)
```powershell
java -cp src Client
```
- Enter your name when prompted
- Type `CREATE roomname` to create a game room
- Wait for second player

### 3. Start Second Client (Terminal 3)
```powershell
java -cp src Client
```
- Enter your name when prompted
- Type `JOIN roomname` to join the game
- Game starts automatically!

## How to Play

- Players take turns entering `MOVE <0-8>` where the number corresponds to board positions:
  ```
  0 | 1 | 2
  -----------
  3 | 4 | 5
  -----------
  6 | 7 | 8
  ```
- First player (room creator) plays as 'O'
- Second player plays as 'X'
- Game ends when someone wins or the board is full (draw)

## Available Commands

- `CREATE <roomname>` - Create a new game room
- `LIST` - List all available rooms
- `JOIN <roomname>` - Join an existing room
- `MOVE <0-8>` - Make a move during the game