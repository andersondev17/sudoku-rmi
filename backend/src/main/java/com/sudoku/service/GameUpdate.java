package com.sudoku.service;

import java.io.Serializable;

public class GameUpdate implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public final String type;
    public final int[][] board;
    public final String message;

    // Constructor para actualizaciones de tablero
    public GameUpdate(String type, int[][] board, String message) {
        this.type = type;
        this.board = board;
        this.message = message;
    }

    // Métodos estáticos de fábrica para crear diferentes tipos de actualizaciones
    public static GameUpdate createBoardUpdate(String type, int[][] board) {
        return new GameUpdate(type, board, null);
    }

    public static GameUpdate createMessageUpdate(String type, String message) {
        return new GameUpdate(type, null, message);
    }

    public static GameUpdate createEmptyUpdate(String type) {
        return new GameUpdate(type, null, null);
    }
}