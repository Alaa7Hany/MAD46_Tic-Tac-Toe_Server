/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.mad46_tic.tac.toe_server;

import com.mycompany.tictactoeshared.PlayerDTO;
import com.mycompany.tictactoeshared.Request;
import com.mycompany.tictactoeshared.RequestType;
import com.mycompany.tictactoeshared.Response;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

/**
 *
 * @author siam
 */

public class TicTacToeServer {

    private ServerSocket serverSocket;
    public static Vector<ClientHandler> clients = new Vector<>();
    public static HashMap<String, GameSession> sessions = new HashMap<>();
    public static boolean isRunning;
    public static int connectionCount = 0;

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
    
    public static void broadCastPlayerList(){
        List<PlayerDTO> players = new ArrayList<>();
        Vector<ClientHandler> currentClients = new Vector<>(clients);
        
        for(ClientHandler client : currentClients){
            if (client.getUsername() != null) { 
                players.add(client.getPlayer());
            }
        }
        
        Request request = new Request(RequestType.GET_ONLINE_PLAYERS, players);
        for(ClientHandler client : currentClients){
            try{
                if(client.getUsername() != null){
                    client.getOutput().writeObject(request);
                    client.getOutput().flush();
                }
            }catch(IOException e){
                System.out.println("Failed to broadcast to " + client.getUsername());
            }
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
