package com.sudoku.model;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GameState implements Serializable {
    private static final long serialVersionUID = 1L;
    private int[][] board;
    private final int size;
    private int currentPlayerId;
    private final Map<Integer, String> players;
    private boolean gameStarted;

    public GameState(int size) {
        this.size = size;
        this.board = new int[size][size];
        this.players = new ConcurrentHashMap<>();
        this.currentPlayerId = -1;
        this.gameStarted = false;
        initializeBoard();
    }

    public void initializeBoard() {
        this.board = SudokuGenerator.generate(size);
    }

    // Métodos para manejar jugadores
    public Map<Integer, String> getPlayers() {
        return players;
    }

    public synchronized void addPlayer(String sessionId, int playerId) {
        players.put(playerId, sessionId);
        if (players.size() == 1) {
            this.currentPlayerId = playerId;
        }
        if (players.size() == 2) {
            this.gameStarted = true;
        }
        System.out.println("Jugador " + playerId + " añadido. Total jugadores: " + players.size());
    }
    public void removePlayer(String sessionId) {
        players.entrySet().removeIf(entry -> entry.getValue().equals(sessionId));
        if (players.isEmpty()) {
            this.currentPlayerId = -1;
            this.gameStarted = false;
        }
    }

    public synchronized boolean isFull() {
        return players.size() >= 2;
    }

    // Métodos para manejar el juego
    public boolean makeMove(int row, int col, int value) {
        if (!isValidMove(row, col, value)) {
            return false;
        }
        board[row][col] = value;
        return true;
    }

    public boolean isValidMove(int row, int col, int value) {
        // Verificar rangos
        if (row < 0 || row >= size || col < 0 || col >= size || value < 1 || value > size) {
            return false;
        }
        
        // Celda ocupada
        if (board[row][col] != 0) return false;
        
        // Verificar fila, columna y cuadro
        return isValidInRow(row, value) 
            && isValidInColumn(col, value) 
            && isValidInBox(row, col, value);
    }
    // Métodos de validación de Sudoku
    private boolean isValidInRow(int row, int value) {
        for (int col = 0; col < size; col++) {
            if (board[row][col] == value) return false;
        }
        return true;
    }

    private boolean isValidInColumn(int col, int value) {
        for (int row = 0; row < size; row++) {
            if (board[row][col] == value) return false;
        }
        return true;
    }

    private boolean isValidInBox(int row, int col, int value) {
        int boxSize = (int) Math.sqrt(size);
        int boxRow = row - (row % boxSize); // Ejemplo: Si row=5 y boxSize=3 → 5 - 2 = 3
        int boxCol = col - (col % boxSize);
    
        for (int i = boxRow; i < boxRow + boxSize; i++) {
            for (int j = boxCol; j < boxCol + boxSize; j++) {
                if (board[i][j] == value) {
                    return false;
                }
            }
        }
        return true;
    }
    // Métodos para verificar victoria
    public boolean isComplete() {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (board[i][j] == 0) return false;
            }
        }
        return true;
    }

    // Getters y setters
    public int[][] getBoard() { 
        return board; 
    }
    
    public void setBoard(int[][] board) { 
        this.board = board; 
    }
    
    public int getCurrentPlayerId() { 
        return currentPlayerId; 
    }
    
    public void setCurrentPlayer(int id) { 
        this.currentPlayerId = id; 
    }
    
    public synchronized boolean isGameStarted() {
        return gameStarted && isFull();
    }
    
    public void setGameStarted(boolean started) { 
        this.gameStarted = started; 
    }
    
    public void switchTurn() { 
        currentPlayerId = (currentPlayerId == 1) ? 2 : 1; 
    }

    public int getSize() {
        return size;
    }
}