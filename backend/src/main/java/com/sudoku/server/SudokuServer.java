package com.sudoku.server;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import org.glassfish.tyrus.server.Server;

import com.sudoku.service.SudokuServiceImpl;

public class SudokuServer {
    public static void main(String[] args) {
        try {
/*             GameWebSocketServer webSocketServerInstance = new GameWebSocketServer();
 */
            // Crear e iniciar el servicio RMI
            SudokuServiceImpl sudokuService = new SudokuServiceImpl();

/*             SudokuServiceImpl.setWebSocketServer(webSocketServerInstance); // Conexión clave
 */
            
            // Crear el registro RMI en el puerto 1099
            Registry registry = LocateRegistry.createRegistry(1099);
            
            // Registrar el servicio
            registry.rebind("SudokuService", sudokuService);
            
            System.out.println("Servidor Sudoku RMI iniciado en el puerto 1099");

            // Iniciar el servidor WebSocket
            Server webSocketServer = new Server("localhost", 8025, "/websockets", null, GameWebSocketServer.class);
            webSocketServer.start();
            System.out.println("Servidor WebSocket iniciado en ws://localhost:8025/websockets/game");

            // Mantener el servidor corriendo
            while (true) {
                Thread.sleep(1000);
            }

        } catch (Exception e) {
            System.err.println("Error al iniciar el servidor: " + e.getMessage());
            e.printStackTrace();
        }
    }
}