package com.sudoku.client;

import java.rmi.registry.LocateRegistry;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

import com.sudoku.service.GameUpdate;
import com.sudoku.service.ISudokuService;

public class SudokuClient {
    private ISudokuService service;
    private Scanner scanner = new Scanner(System.in);
    private int playerId;
    private AtomicBoolean inGame = new AtomicBoolean(false);
    private AtomicBoolean gameRunning = new AtomicBoolean(false);

    public static void main(String[] args) {
        new SudokuClient().connect();
    }

    public void connect() {
        try {
            service = (ISudokuService) LocateRegistry.getRegistry("localhost", 1099).lookup("SudokuService");
            System.out.println("Conectado al servidor RMI");
            showMenu();
        } catch (Exception e) {
            System.err.println("Error de conexión: " + e.getMessage());
        }
    }

    private void showMenu() {
        while (true) {
            if (!inGame.get()) {
                System.out.println("\nGENERADOR SUDOKU");
                System.out.println("1. Matriz 4x4\n2. Matriz 9x9\n3. Matriz 16x16");
                System.out.println("4. Unirse a juego multijugador\n5. Salir");
            }

            try {
                handleOption(scanner.nextInt());
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
                scanner.nextLine(); // Limpiar buffer
            }
        }
    }

    private void handleOption(int option) {
        try {
            switch (option) {
                case 1: displayBoard(service.generateBoard4x4()); break;
                case 2: displayBoard(service.generateBoard9x9()); break;
                case 3: displayBoard(service.generateBoard16x16()); break;
                case 4: joinMultiplayerGame(); break;
                case 5: 
                    if (inGame.get()) service.leaveGame(playerId);
                    System.exit(0);
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private void joinMultiplayerGame() throws Exception {
        playerId = service.joinGame();
        inGame.set(true);
        gameRunning.set(true);
        System.out.println("Te has unido como jugador " + playerId);

        // Esperar a que se una otro jugador
        while (!service.isGameReady()) {
            System.out.println("Esperando otro jugador...");
            Thread.sleep(1000);
        }

        System.out.println("\n¡Juego iniciado! Eres el jugador " + playerId);

        // Iniciar thread de actualizaciones y comenzar juego
        new Thread(this::handleUpdates).start();
        playGame();
    }

    private void handleUpdates() {
        try {
            while (gameRunning.get()) {
                GameUpdate update = service.getUpdate(playerId);
                if (update != null && !"NO_UPDATE".equals(update.type)) {
                    processUpdate(update);
                }
                Thread.sleep(100);
            }
        } catch (Exception e) {
            System.err.println("Error en actualizaciones: " + e.getMessage());
            gameRunning.set(false);
        }
    }

    private void processUpdate(GameUpdate update) {
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
                        System.out.println(winnerId == playerId 
                            ? "\n¡Felicidades! Has ganado!" 
                            : "\nEl juego ha terminado. Ha ganado el jugador " + winnerId);
                    }
                    endGame();
                    break;
                case "PLAYER_DISCONNECTED":
                    System.out.println("\nEl otro jugador se ha desconectado");
                    endGame();
                    break;
            }
        } catch (Exception e) {
            System.err.println("Error procesando actualización: " + e.getMessage());
        }
    }

    private void endGame() {
        gameRunning.set(false);
        inGame.set(false);
    }

    private void playGame() {
        try {
            displayBoard(service.getCurrentBoard());

            while (gameRunning.get()) {
                System.out.println("\n" + service.getGameStatus(playerId));

                if (service.getCurrentPlayerId() == playerId) {
                    makeMove();
                } else {
                    Thread.sleep(5000); // Esperar menos tiempo para mejor respuesta
                }
            }
        } catch (Exception e) {
            System.err.println("Error en el juego: " + e.getMessage());
            gameRunning.set(false);
        }
    }

    private void makeMove() {
        try {
            while (gameRunning.get()) {
                System.out.print("\nIngrese fila, columna y valor (ej: 0 1 5): ");
                int row = scanner.nextInt();
                int col = scanner.nextInt();
                int value = scanner.nextInt();

                if (service.makeMove(playerId, row, col, value)) {
                    break;
                }
                System.out.println("Movimiento inválido, intente de nuevo.");
            }
        } catch (Exception e) {
            System.out.println("Entrada inválida. Use números dentro del rango correcto.");
            scanner.nextLine(); // Limpiar buffer
        }
    }

    private void displayBoard(int[][] board) {
        System.out.println("\n********   Tablero Sudoku:   ********\n");
        int size = board.length;
        int boxSize = (int) Math.sqrt(size);
        
        // Imprimir números de columna
        System.out.print("    ");
        for (int j = 0; j < size; j++) {
            System.out.printf("%2d ", j);
            if ((j + 1) % boxSize == 0 && j < size - 1) System.out.print("| ");
        }
        System.out.println();
        
        printBorderLine(size, boxSize);
    
        // Imprimir el tablero
        for (int i = 0; i < size; i++) {
            System.out.printf("%2d | ", i);
            for (int j = 0; j < size; j++) {
                System.out.printf("%2s ", board[i][j] == 0 ? "·" : board[i][j]);
                if ((j + 1) % boxSize == 0 && j < size - 1) System.out.print("| ");
            }
            System.out.println();
            
            if ((i + 1) % boxSize == 0 && i < size - 1) printBorderLine(size, boxSize);
        }
        
        printBorderLine(size, boxSize);
    }
    
    private void printBorderLine(int size, int boxSize) {
        System.out.print("    ");
        for (int i = 0; i < size * 3 + boxSize - 1; i++) System.out.print("-");
        System.out.println();
    }
}