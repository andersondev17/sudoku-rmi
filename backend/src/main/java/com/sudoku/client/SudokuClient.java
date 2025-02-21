package com.sudoku.client;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

import com.sudoku.service.GameUpdate;
import com.sudoku.service.ISudokuService;

public class SudokuClient {
    private ISudokuService service;
    private Scanner scanner;
    private int playerId;
    private boolean inGame = false;
    private volatile boolean gameRunning = true;

    public SudokuClient() {
        scanner = new Scanner(System.in);
    }

    public void connect() {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            service = (ISudokuService) registry.lookup("SudokuService");
            System.out.println("Conectado al servidor RMI");
            showMenu();
        } catch (Exception e) {
            System.err.println("Error de conexión: " + e.getMessage());
        }
    }

    public void showMenu() {
        while (true) {
            if (!inGame) {
                System.out.println("\nGENERADOR SUDOKU");
                System.out.println("1. Matriz 4x4");
                System.out.println("2. Matriz 9x9");
                System.out.println("3. Matriz 16x16");
                System.out.println("4. Unirse a juego multijugador");
                System.out.println("5. Salir");
            }

            try {
                int option = scanner.nextInt();
                handleOption(option);
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
                scanner.nextLine();
            }
        }
    }

    private void handleOption(int option) {
        try {
            switch (option) {
                case 1:
                    displayBoard(service.generateBoard4x4());
                    break;
                case 2:
                    displayBoard(service.generateBoard9x9());
                    break;
                case 3:
                    displayBoard(service.generateBoard16x16());
                    break;
                case 4:
                    joinMultiplayerGame();
                    break;
                case 5:
                    if (inGame) {
                        service.leaveGame(playerId);
                    }
                    System.exit(0);
                    break;
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private void joinMultiplayerGame() throws Exception {
        playerId = service.joinGame();
        inGame = true;
        gameRunning = true;
        System.out.println("Te has unido como jugador " + playerId);

        while (!service.isGameReady()) {
            System.out.println("Esperando otro jugador...");
            Thread.sleep(1000);
        }

        System.out.println("\n¡Juego iniciado!");
        System.out.println("Eres el jugador " + playerId);

        // Iniciar thread de actualizaciones
        Thread updateThread = new Thread(this::handleUpdates);
        updateThread.setDaemon(true);
        updateThread.start();

        playGame();
    }

    private void handleUpdates() {
        try {
            while (gameRunning) {
                GameUpdate update = service.getUpdate(playerId);
                if (update != null) {
                    handleGameUpdate(update);
                }
                Thread.sleep(100);
            }
        } catch (Exception e) {
            System.err.println("Error en actualizaciones: " + e.getMessage());
            gameRunning = false;
        }
    }

    private void handleGameUpdate(GameUpdate update) {
        try {
            switch (update.type) {
                case "MOVE_MADE":
                    if (update.board != null) {
                        displayBoard(update.board);
                        System.out.println(service.getGameStatus(playerId));
                    }
                    break;
                case "GAME_OVER":
                    if (update.message != null) {
                        int winnerId = Integer.parseInt(update.message.split(":")[1]);
                        if (winnerId == playerId) {
                            System.out.println("\n¡Felicidades! Has ganado!");
                        } else {
                            System.out.println("\nEl juego ha terminado. Ha ganado el jugador " + winnerId);
                        }
                    }
                    gameRunning = false;
                    inGame = false;
                    showMenu();
                    break;
                case "PLAYER_DISCONNECTED":
                    System.out.println("\nEl otro jugador se ha desconectado");
                    gameRunning = false;
                    inGame = false;
                    showMenu();
                    break;
            }
        } catch (Exception e) {
            System.err.println("Error procesando actualización: " + e.getMessage());
        }
    }

    private void playGame() {
        try {
            displayBoard(service.getCurrentBoard());

            while (gameRunning) {
                String status = service.getGameStatus(playerId);
                System.out.println("\n" + status);

                if (service.getCurrentPlayerId() == playerId) {
                    makeMove();
                } else {
                    System.out.println("Esperando al otro jugador...");
                    Thread.sleep(1000);
                }
            }
        } catch (Exception e) {
            System.err.println("Error en el juego: " + e.getMessage());
            gameRunning = false;
        }
    }

    private void makeMove() {
        try {
            boolean validMove = false;
            while (!validMove && gameRunning) {
                try {
                    System.out.print("\nIngrese fila (0-8): ");
                    int row = scanner.nextInt();
                    System.out.print("Ingrese columna (0-8): ");
                    int col = scanner.nextInt();
                    System.out.print("Ingrese valor (1-9): ");
                    int value = scanner.nextInt();

                    validMove = service.makeMove(playerId, row, col, value);
                    if (!validMove) {
                        System.out.println("Movimiento inválido, intente de nuevo.");
                    }
                } catch (Exception e) {
                    System.out.println("Entrada inválida. Use números dentro del rango correcto.");
                    scanner.nextLine(); // Limpiar el buffer
                }
            }
        } catch (Exception e) {
            System.err.println("Error al realizar movimiento: " + e.getMessage());
        }
    }

    private void displayBoard(int[][] board) {
        System.out.println("\n         ********   Tablero Sudoku:    ********\n" + //
                        "");
        int size = board.length;
        int boxSize = (int) Math.sqrt(size);
        
        
    
        // Imprimir números de columna
        System.out.print("     "); // Espacio inicial alineado
        for (int j = 0; j < size; j++) {
            System.out.printf("%2d ", j);
            if ((j + 1) % boxSize == 0 && j < size - 1) {
                System.out.print("| ");
            }
        }
        System.out.println();
        
        // Línea separadora después de los números de columna
        printBorderLine(size, boxSize);
    
        // Imprimir el tablero
        for (int i = 0; i < size; i++) {
            // Imprimir número de fila con padding
            System.out.printf("%2d | ", i);
    
            // Imprimir valores de la fila
            for (int j = 0; j < size; j++) {
                System.out.printf("%2s ", board[i][j] == 0 ? "·" : board[i][j]);
                // Separador vertical entre cajas
                if ((j + 1) % boxSize == 0 && j < size - 1) {
                    System.out.print("| ");
                }
            }
            System.out.println();
    
            // Línea horizontal después de cada caja
            if ((i + 1) % boxSize == 0 && i < size - 1) {
                printBorderLine(size, boxSize);
            }
        }
        
        // Línea inferior
        printBorderLine(size, boxSize);
    }
    
    private void printBorderLine(int size, int boxSize) {
        System.out.print("     "); // Alineación con el contenido
        for (int i = 0; i < size * 3 + boxSize - 1; i++) {
            System.out.print("-");
        }
        System.out.println();
        
    }
        public static void main(String[] args) {
        new SudokuClient().connect();
    }
}