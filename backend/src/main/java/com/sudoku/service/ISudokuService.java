// ISudokuService.java
package com.sudoku.service;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ISudokuService extends Remote {
    // Métodos para generar tableros
    int[][] generateBoard4x4() throws RemoteException;
    int[][] generateBoard9x9() throws RemoteException;
    int[][] generateBoard16x16() throws RemoteException;
    
    // Métodos para el juego multijugador
    int joinGame() throws RemoteException;
    boolean makeMove(int playerId, int row, int col, int value) throws RemoteException;
    boolean isGameReady() throws RemoteException;
    int getCurrentPlayerId() throws RemoteException;
    int[][] getCurrentBoard() throws RemoteException;
    String getGameStatus(int playerId) throws RemoteException;
    void leaveGame(int playerId) throws RemoteException;
    GameUpdate getUpdate(int playerId) throws RemoteException;
}