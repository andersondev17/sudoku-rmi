### Sudoku RMI Multiplayer
A multiplayer Sudoku game using Java RMI for game logic and WebSockets for real-time updates. The backend is built in Java, while the frontend (in development) uses React/Next.js.

```mermaid
classDiagram
    class ISudokuService {
        <<interface>>
        +generateBoard4x4() int[][]
        +generateBoard9x9() int[][]
        +generateBoard16x16() int[][]
        +joinGame() int
        +makeMove(int playerId, int row, int col, int value) boolean
        +isGameReady() boolean
        +getCurrentPlayerId() int
        +getCurrentBoard() int[][]
        +getGameStatus(int playerId) String
        +leaveGame(int playerId) void
        +getUpdate(int playerId) GameUpdate
    }
    
    class SudokuServiceImpl {
        -Map~String, GameState~ activeGames
        -Map~Integer, String~ playerToGameMap
        -Map~Integer, BlockingQueue~ playerUpdates
        -int lastPlayerId
        +SudokuServiceImpl()
        +generateBoard4x4() int[][]
        +generateBoard9x9() int[][]
        +generateBoard16x16() int[][]
        +joinGame() int
        +makeMove(int playerId, int row, int col, int value) boolean
        +isGameReady() boolean
        +getCurrentPlayerId() int
        +getCurrentBoard() int[][]
        +getGameStatus(int playerId) String
        +leaveGame(int playerId) void
        +getUpdate(int playerId) GameUpdate
        -notifyAllPlayers(String gameId, String type) void
        -findOrCreateGame() String
    }
    
    class GameState {
        -int[][] board
        -int size
        -int currentPlayerId
        -Map~Integer, String~ players
        -boolean gameStarted
        +GameState(int size)
        +initializeBoard() void
        +getPlayers() Map~Integer, String~
        +addPlayer(String sessionId, int playerId) void
        +removePlayer(String sessionId) void
        +isFull() boolean
        +makeMove(int row, int col, int value) boolean
        +isValidMove(int row, int col, int value) boolean
        +isComplete() boolean
        +getBoard() int[][]
        +setBoard(int[][] board) void
        +getCurrentPlayerId() int
        +setCurrentPlayer(int id) void
        +isGameStarted() boolean
        +setGameStarted(boolean started) void
        +switchTurn() void
    }
    
    class SudokuGenerator {
        <<utility>>
        +generate(int size) int[][]
        -fillDiagonal(int[][] board, int size) void
        -fillBox(int[][] board, int row, int col, int size) void
        -isValidInBox(int[][] board, int startRow, int startCol, int num, int sqrt) boolean
        -removeNumbers(int[][] board, int size) void
        -solveSudoku(int[][] board, int size) boolean
        -isSafe(int[][] board, int row, int col, int num, int size) boolean
    }
    
    class GameUpdate {
        +String type
        +int[][] board
        +String message
        +GameUpdate(String type, int[][] board, String message)
        +createBoardUpdate(String type, int[][] board) GameUpdate$
        +createMessageUpdate(String type, String message) GameUpdate$
        +createEmptyUpdate(String type) GameUpdate$
    }
    
    class SudokuClient {
        -ISudokuService service
        -Scanner scanner
        -int playerId
        -boolean inGame
        -boolean gameRunning
        +SudokuClient()
        +connect() void
        +showMenu() void
        -handleOption(int option) void
        -joinMultiplayerGame() void
        -handleUpdates() void
        -handleGameUpdate(GameUpdate update) void
        -playGame() void
        -makeMove() void
        -displayBoard(int[][] board) void
        -printBorderLine(int size, int boxSize) void
        +main(String[] args) void$
    }
    
    ISudokuService <|.. SudokuServiceImpl : implements
    SudokuServiceImpl --> GameState : uses
    SudokuServiceImpl --> GameUpdate : uses
    GameState --> SudokuGenerator : uses
    SudokuClient --> ISudokuService : uses
    SudokuClient --> GameUpdate : uses


```



## Running the Project
## 1️⃣ Start the Backend Server (RMI & WebSocket)
Open a terminal and run:

### Server
```bash
cd backend

mvn exec:java -Dexec.mainClass="com.sudoku.server.SudokuServer"

```

- This starts the RMI server and the WebSocket server.


## 2️⃣ Run the RMI Client
Open a second terminal and start a client instance:
 
 ```bash

cd backend

mvn exec:java -Dexec.mainClass="com.sudoku.client.SudokuClient"
```

- This client will connect to the RMI server and allow you to interact with the Sudoku game.

## 3️⃣ Add Another Client (Multiplayer Mode)
To simulate multiplayer, open a third terminal and run another client:

```bash

cd backend
mvn exec:java -Dexec.mainClass="com.sudoku.client.SudokuClient"
```

- This connects an additional player to the same game session.



## Architecture & Design Patterns
The project follows a distributed architecture using RMI and WebSockets:
# 1. Modelo (Model)
## Game State (GameState.java)
– Centralizes game logic, ensuring consistent updates within players.

##  (SudokuGenerator.java)
- Generates random Sudoku tables 

# 2. Servicio (Service)
## RMI Interface (ISudokuService.java)
– RMI Interface defining game operations.

## RMI (SudokuServiceImpl.java) 
– Handles core game logic (moves, turns, board state).

## GameUpdate 
- Class for messages between server and client


# 3. Server

## WebSocket (GameWebSocketServer.java)
– Notifies clients in real time about game updates.

## SudokuServer.java

- Server entry point that starts RMI and WebSocket server.

# 4 Client

## SudokuClient.java

- Client connected to server to play Sudoku



Provides UI to create/join games and interact with the game state.

Frontend (Next.js) Setup
Coming Soon: The React/Next.js client is under development. Once integrated, it will allow users to interact with the game via a browser.

🚀
