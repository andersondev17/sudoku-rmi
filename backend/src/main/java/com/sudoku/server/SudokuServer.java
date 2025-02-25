package com.sudoku.server;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import com.sudoku.service.SudokuServiceImpl;

public class SudokuServer {
    public static void main(String[] args) {
        try {
            // Crear e iniciar el servicio RMI
            SudokuServiceImpl sudokuService = new SudokuServiceImpl();
            
            // Crear el registro RMI en el puerto 1099
            Registry registry = LocateRegistry.createRegistry(1099);
            
            // Registrar el servicio
            registry.rebind("SudokuService", sudokuService);
            
            System.out.println("Servidor Sudoku RMI iniciado en el puerto 1099");
            System.out.println("Esperando conexiones de clientes...");

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