// SudokuServiceImpl.java
package com.sudoku.service;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import com.sudoku.model.GameState;

public class SudokuServiceImpl extends UnicastRemoteObject implements ISudokuService {
    private static final long serialVersionUID = 1L;
    private final Map<String, GameState> activeGames = new ConcurrentHashMap<>();
    private final Map<Integer, String> playerToGameMap = new ConcurrentHashMap<>();
    private final Map<Integer, BlockingQueue<GameUpdate>> playerUpdates = new ConcurrentHashMap<>();
    private int lastPlayerId = 0;

    public SudokuServiceImpl() throws RemoteException {
        super();
    }

    @Override
    public int[][] generateBoard4x4() throws RemoteException {
        GameState game = new GameState(4);
        game.initializeBoard();
        return game.getBoard();
    }

    @Override
    public int[][] generateBoard9x9() throws RemoteException {
        GameState game = new GameState(9);
        game.initializeBoard();
        return game.getBoard();
    }

    @Override
    public int[][] generateBoard16x16() throws RemoteException {
        GameState game = new GameState(16);
        game.initializeBoard();
        return game.getBoard();
    }

    @Override
    public synchronized int joinGame() throws RemoteException {
        try {
            int playerId = ++lastPlayerId;
            playerUpdates.put(playerId, new LinkedBlockingQueue<>());

            String gameId = findOrCreateGame();
            GameState game = activeGames.get(gameId);
            game.addPlayer(String.valueOf(playerId), playerId);
            playerToGameMap.put(playerId, gameId);

            if (game.isFull()) {
                game.initializeBoard();
                game.setGameStarted(true);
                notifyAllPlayers(gameId, "GAME_START");
            }

            return playerId;
        } catch (Exception e) {
            throw new RemoteException("Error al unirse al juego", e);
        }
    }

    @Override
    public synchronized boolean makeMove(int playerId, int row, int col, int value) throws RemoteException {
        try {
            String gameId = playerToGameMap.get(playerId);
            if (gameId == null) return false;

            GameState game = activeGames.get(gameId);
            if (game == null || !game.isGameStarted()) return false;

            if (game.getCurrentPlayerId() != playerId) return false;

            if (game.makeMove(row, col, value)) {
                game.switchTurn();
                notifyAllPlayers(gameId, "MOVE_MADE");
                
                if (game.isComplete()) {
                    notifyAllPlayers(gameId, "GAME_OVER:" + playerId);
                }
                return true;
            }
            return false;
        } catch (Exception e) {
            throw new RemoteException("Error al realizar movimiento", e);
        }
    }

    @Override
    public synchronized boolean isGameReady() throws RemoteException {
        try {
            for (GameState game : activeGames.values()) {
                if (game.isGameStarted() && game.isFull()) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            throw new RemoteException("Error al verificar estado del juego", e);
        }
    }

    @Override
    public synchronized int getCurrentPlayerId() throws RemoteException {
        try {
            for (GameState game : activeGames.values()) {
                if (game.isGameStarted()) {
                    return game.getCurrentPlayerId();
                }
            }
            return -1;
        } catch (Exception e) {
            throw new RemoteException("Error al obtener jugador actual", e);
        }
    }

    @Override
    public synchronized int[][] getCurrentBoard() throws RemoteException {
        try {
            for (Map.Entry<String, GameState> entry : activeGames.entrySet()) {
                if (entry.getValue().isGameStarted()) {
                    return entry.getValue().getBoard();
                }
            }
            throw new RemoteException("No hay juego activo");
        } catch (Exception e) {
            throw new RemoteException("Error al obtener tablero", e);
        }
    }

    @Override
    public String getGameStatus(int playerId) throws RemoteException {
        try {
            String gameId = playerToGameMap.get(playerId);
            if (gameId == null) return "No estás en ningún juego";

            GameState game = activeGames.get(gameId);
            if (game == null) return "El juego no existe";

            if (!game.isGameStarted()) return "Esperando a otro jugador...";

            return game.getCurrentPlayerId() == playerId ? 
                   "Es tu turno" : 
                   "📄🔢 Esperando al otro jugador...";
        } catch (Exception e) {
            throw new RemoteException("Error al obtener estado del juego", e);
        }
    }

    @Override
    public void leaveGame(int playerId) throws RemoteException {
        try {
            String gameId = playerToGameMap.remove(playerId);
            if (gameId != null) {
                GameState game = activeGames.get(gameId);
                if (game != null) {
                    game.removePlayer(String.valueOf(playerId));
                    notifyAllPlayers(gameId, "PLAYER_DISCONNECTED");
                    if (game.getPlayers().isEmpty()) {
                        activeGames.remove(gameId);
                    }
                }
            }
            playerUpdates.remove(playerId);
        } catch (Exception e) {
            throw new RemoteException("Error al abandonar el juego", e);
        }
    }
    @Override
    public GameUpdate getUpdate(int playerId) throws RemoteException {
        try {
            BlockingQueue<GameUpdate> updates = playerUpdates.get(playerId);
            if (updates != null) {
                GameUpdate update = updates.poll(100, TimeUnit.MILLISECONDS);
                if (update != null) {
                    return update;
                }
            }
            return GameUpdate.createEmptyUpdate("NO_UPDATE");
        } catch (InterruptedException e) {
            return GameUpdate.createMessageUpdate("ERROR", e.getMessage());
        }
    }

    private void notifyAllPlayers(String gameId, String type) {
        GameState game = activeGames.get(gameId);
        if (game == null) return;

        GameUpdate update;
        if (type.startsWith("GAME_OVER:")) {
            update = GameUpdate.createMessageUpdate(type, type);
        } else {
            update = GameUpdate.createBoardUpdate(type, game.getBoard());
        }

        game.getPlayers().forEach((playerId, sessionId) -> {
            BlockingQueue<GameUpdate> updates = playerUpdates.get(Integer.parseInt(sessionId));
            if (updates != null) {
                updates.offer(update);
            }
        });
    }

    private String findOrCreateGame() {
        for (Map.Entry<String, GameState> entry : activeGames.entrySet()) {
            if (!entry.getValue().isFull()) {
                return entry.getKey();
            }
        }
        String gameId = UUID.randomUUID().toString();
        activeGames.put(gameId, new GameState(9));
        return gameId;
    }

}
