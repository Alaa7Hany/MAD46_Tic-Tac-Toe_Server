/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.mad46_tic.tac.toe_server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Vector;

/**
 *
 * @author siam
 */

public class TicTacToeServer {

    private ServerSocket serverSocket;
    public static Vector<ClientHandler> clients = new Vector<>();
    public static boolean isRunning;
    
    public TicTacToeServer() {}
    
    public void startServer() {
        try {
            serverSocket = new ServerSocket(5005);
            isRunning = true;
            System.out.println("Server Started, waiting for connections...");

            while (isRunning) {
                try {
                    // This line blocks until a client connects
                    Socket socket = serverSocket.accept();
                    System.out.println("New Client Connected!");

                    ClientHandler handler = new ClientHandler(socket);
                    clients.add(handler);
                    handler.start();
                    
                } catch (SocketException e) {
                    // This exception happens when we close the socket to stop the server
                    if (!isRunning) {
                        System.out.println("Server Stopped Manually");
                        break; 
                    } else {
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    public void stopServer() {
        isRunning = false;
        try {
            // Stop accepting new connections
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            clearClients();
            
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    public static void clearClients(){
        for (ClientHandler client : clients) {
            client.closeConnection();
        }
        clients.clear();
    }


}
