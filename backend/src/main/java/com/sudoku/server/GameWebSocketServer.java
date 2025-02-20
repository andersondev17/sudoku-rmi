package com.sudoku.server;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import com.google.gson.Gson;
import com.sudoku.model.GameState;

@ServerEndpoint(value = "/game")
public class GameWebSocketServer {
    private static final Map<String, Session> sessions = new ConcurrentHashMap<>();
    private static final Map<String, String> sessionToGameMap = new ConcurrentHashMap<>();
    private static final Map<String, GameState> games = new ConcurrentHashMap<>();
    private static final Gson gson = new Gson();
    
    @OnOpen
    public void onOpen(Session session) {
        sessions.put(session.getId(), session);
        sendAvailableGames(session);
    }
    
    @OnMessage
    public void onMessage(String message, Session session) {
        try {
            GameMessage gameMessage = gson.fromJson(message, GameMessage.class);
            handleGameMessage(gameMessage, session);
        } catch (Exception e) {
            sendError(session, "Error processing message: " + e.getMessage());
        }
    }
    
    @OnClose
    public void onClose(Session session) {
        String gameId = sessionToGameMap.get(session.getId());
        if (gameId != null) {
            handlePlayerDisconnect(session, gameId);
        }
        sessions.remove(session.getId());
        sessionToGameMap.remove(session.getId());
    }
    
    @OnError
    public void onError(Session session, Throwable throwable) {
        System.err.println("Error in session " + session.getId() + ": " + throwable.getMessage());
    }

    private void handleGameMessage(GameMessage message, Session session) {
        switch (message.type) {
            case "CREATE_GAME":
                createGame(message, session);
                break;
            case "JOIN_GAME":
                joinGame(session);
                break;
            case "MAKE_MOVE":
                handleMove(message, session);
                break;
            case "GET_AVAILABLE_GAMES":
                sendAvailableGames(session);
                break;
        }
    }

    private void createGame(GameMessage message, Session session) {
        String gameId = UUID.randomUUID().toString();
        GameState gameState = new GameState(message.size);
        gameState.addPlayer(session.getId(), 1);
        games.put(gameId, gameState);
        sessionToGameMap.put(session.getId(), gameId);
        
        // Notify creator
        sendToSession(session, new GameMessage("GAME_CREATED", gameId, 1));
        broadcastAvailableGames();
    }

    private void joinGame(Session session) {
        // Find available game
        for (Map.Entry<String, GameState> entry : games.entrySet()) {
            GameState game = entry.getValue();
            if (!game.isFull()) {
                String gameId = entry.getKey();
                game.addPlayer(session.getId(), 2);
                sessionToGameMap.put(session.getId(), gameId);
                
                // Start game
                startGame(gameId);
                return;
            }
        }
        sendError(session, "No hay partidas disponibles");
    }

    private void startGame(String gameId) {
        GameState game = games.get(gameId);
        if (game == null) return;

        // Generate board and start game
        game.initializeBoard();
        game.setCurrentPlayer(1);
        game.setGameStarted(true);

        // Notify both players
        game.getPlayers().forEach((playerId, sessionId) -> {
            Session playerSession = sessions.get(sessionId);
            if (playerSession != null) {
                GameMessage startMessage = new GameMessage("GAME_START");
                startMessage.playerId = playerId;
                startMessage.board = game.getBoard();
                startMessage.isMyTurn = playerId == 1;
                sendToSession(playerSession, startMessage);
            }
        });
        
        broadcastAvailableGames();
    }

    private void handleMove(GameMessage message, Session session) {
        String gameId = sessionToGameMap.get(session.getId());
        if (gameId == null) return;

        
        GameState game = games.get(gameId);
        if (game == null) return;

        if (game.getCurrentPlayerId() != message.playerId) {
            sendError(session, "No es tu turno");
            return;
        }

        if (game.makeMove(message.row, message.col, message.value)) {
            game.switchTurn(); // Cambiar turno antes de notificar
            broadcastGameState(gameId); // Notificar a ambos jugadores
        } else {
            sendError(session, "Movimiento inválido");
        }
    }

    private void sendAvailableGames(Session session) {
        int availableGames = (int) games.values().stream()
            .filter(game -> !game.isFull())
            .count();
        
        GameMessage message = new GameMessage("AVAILABLE_GAMES");
        message.availableGames = availableGames;
        sendToSession(session, message);
    }

    private void broadcastAvailableGames() {
        sessions.values().forEach(this::sendAvailableGames);
    }

    public void broadcastGameState(String gameId) {
        GameState game = games.get(gameId);
        if (game == null) return;
    
        GameMessage stateMessage = new GameMessage("GAME_UPDATE");
        stateMessage.board = game.getBoard();
        stateMessage.currentPlayer = game.getCurrentPlayerId();
    
        // Enviar a todos los jugadores en el juego
        game.getPlayers().forEach((playerId, sessionId) -> {
            Session playerSession = sessions.get(sessionId);
            if (playerSession != null) {
                stateMessage.isMyTurn = (game.getCurrentPlayerId() == playerId);
                sendToSession(playerSession, stateMessage);
            }
        });
    }
    private void handlePlayerDisconnect(Session session, String gameId) {
        GameState game = games.get(gameId);
        if (game != null) {
            game.removePlayer(session.getId());
            if (game.getPlayers().isEmpty()) {
                games.remove(gameId);
            } else {

                // Notify remaining player
                game.getPlayers().forEach((playerId, sessionId) -> {
                    Session playerSession = sessions.get(sessionId);
                    if (playerSession != null) {
                        sendToSession(playerSession, new GameMessage("PLAYER_DISCONNECTED"));
                    }
                });
            }
            broadcastAvailableGames();
        }
    }

    private void sendToSession(Session session, GameMessage message) {
        try {
            session.getBasicRemote().sendText(gson.toJson(message));
        } catch (IOException e) {
            System.err.println("Error sending message to session " + session.getId() + ": " + e.getMessage());
        }
    }

    private void sendError(Session session, String error) {
        GameMessage errorMessage = new GameMessage("ERROR");
        errorMessage.error = error;
        sendToSession(session, errorMessage);
    }

    
    // Clase interna para mensajes del juego
    private static class GameMessage {
        String type;
        int size;
        String gameId;
        int playerId;
        int[][] board;
        int row;
        int col;
        int value;
        boolean isMyTurn;
        int currentPlayer;
        int availableGames;
        String error;

        GameMessage(String type) {
            this.type = type;
        }

        GameMessage(String type, String gameId, int playerId) {
            this.type = type;
            this.gameId = gameId;
            this.playerId = playerId;
        }
    }
}