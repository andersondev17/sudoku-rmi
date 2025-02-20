// src/model/SudokuGenerator.java
package com.sudoku.model;

import java.util.Random;

public class SudokuGenerator {
    private static final Random random = new Random();

    public static int[][] generate(int size) {
        int[][] board = new int[size][size];
        fillDiagonal(board, size);
        if (solveSudoku(board, size)) {
            removeNumbers(board, size);
        }
        return board;
    }

    private static void fillDiagonal(int[][] board, int size) {
        int sqrt = (int) Math.sqrt(size);
        for (int box = 0; box < size; box += sqrt) {
            fillBox(board, box, box, size);
        }
    }

    private static void fillBox(int[][] board, int row, int col, int size) {
        int sqrt = (int) Math.sqrt(size);
        for (int i = 0; i < sqrt; i++) {
            for (int j = 0; j < sqrt; j++) {
                int num;
                do {
                    num = random.nextInt(size) + 1;
                } while (!isValidInBox(board, row, col, num, sqrt));
                board[row + i][col + j] = num;
            }
        }
    }

    private static boolean isValidInBox(int[][] board, int startRow, int startCol, int num, int sqrt) {
        for (int i = 0; i < sqrt; i++) {
            for (int j = 0; j < sqrt; j++) {
                if (board[startRow + i][startCol + j] == num) {
                    return false;
                }
            }
        }
        return true;
    }

    private static void removeNumbers(int[][] board, int size) {
        int numbersToRemove = (size == 9) ? 40 : (size * size) / 2; // Ajuste según tamaño
        while (numbersToRemove > 0) {
            int row = random.nextInt(size);
            int col = random.nextInt(size);
            if (board[row][col] != 0) {
                board[row][col] = 0;
                numbersToRemove--;
            }
        }
    }

    private static boolean solveSudoku(int[][] board, int size) {
        int row = -1, col = -1;
        boolean isEmpty = false;
        
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (board[i][j] == 0) {
                    row = i;
                    col = j;
                    isEmpty = true;
                    break;
                }
            }
            if (isEmpty) break;
        }

        if (!isEmpty) return true;

        for (int num = 1; num <= size; num++) {
            if (isSafe(board, row, col, num, size)) {
                board[row][col] = num;
                if (solveSudoku(board, size)) return true;
                board[row][col] = 0;
            }
        }
        return false;
    }

    private static boolean isSafe(int[][] board, int row, int col, int num, int size) {
        return !usedInRow(board, row, num, size) &&
               !usedInCol(board, col, num, size) &&
               !usedInBox(board, row - row % (int)Math.sqrt(size), 
                         col - col % (int)Math.sqrt(size), num, size);
    }

    private static boolean usedInRow(int[][] board, int row, int num, int size) {
        for (int col = 0; col < size; col++) {
            if (board[row][col] == num) return true;
        }
        return false;
    }

    private static boolean usedInCol(int[][] board, int col, int num, int size) {
        for (int row = 0; row < size; row++) {
            if (board[row][col] == num) return true;
        }
        return false;
    }

    private static boolean usedInBox(int[][] board, int boxStartRow, int boxStartCol, int num, int size) {
        int sqrt = (int) Math.sqrt(size);
        for (int i = 0; i < sqrt; i++) {
            for (int j = 0; j < sqrt; j++) {
                if (board[boxStartRow + i][boxStartCol + j] == num) return true;
            }
        }
        return false;
    }
}