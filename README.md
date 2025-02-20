### Sudoku RMI Multiplayer
A multiplayer Sudoku game using Java RMI for game logic and WebSockets for real-time updates. The backend is built in Java, while the frontend (in development) uses React/Next.js.
## Architecture & Design Patterns
The project follows a distributed architecture using RMI and WebSockets:

## RMI (SudokuServiceImpl.java) 
– Handles core game logic (moves, turns, board state).
## WebSocket (GameWebSocketServer.java)
– Notifies clients in real time about game updates.
## Game State (GameState.java)
– Centralizes game logic, ensuring consistent updates.
## RMI Interface (ISudokuService.java)
– RMI Interface defining game operations.
## GameUpdate 
- Class for messages between server and client

Provides UI to create/join games and interact with the game state.

## Running the Project
## 1️⃣ Start the Backend Server (RMI & WebSocket)
Open a terminal and run:

### cd sudoku-rmi/backend
mvn exec:java -Dexec.mainClass="com.sudoku.server.SudokuServer"

- This starts the RMI server and the WebSocket server.

## 2️⃣ Run the RMI Client
Open a second terminal and start a client instance:
 
cd sudoku-rmi/backend
mvn exec:java -Dexec.mainClass="com.sudoku.client.SudokuClient"

- This client will connect to the RMI server and allow you to interact with the Sudoku game.

## 3️⃣ Add Another Client (Multiplayer Mode)
To simulate multiplayer, open a third terminal and run another client:

cd sudoku-rmi/backend
mvn exec:java -Dexec.mainClass="com.sudoku.client.SudokuClient"
- This connects an additional player to the same game session.

Frontend (Next.js) Setup
Coming Soon: The React/Next.js client is under development. Once integrated, it will allow users to interact with the game via a browser.

🚀
