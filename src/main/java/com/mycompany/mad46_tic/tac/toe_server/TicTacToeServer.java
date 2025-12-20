/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.mad46_tic.tac.toe_server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

/**
 *
 * @author siam
 */

public class TicTacToeServer {

    private ServerSocket serverSocket;
    public static Vector<ClientHandler> clients = new Vector<>();
    
    public TicTacToeServer() {

        try {
            serverSocket = new ServerSocket(5005);
            System.out.println("Server Started, waiting for connections...");
            
            // FOR TEST ONLY: add 2 fake clients
            addTestClients();

            while (true) {

                Socket socket = serverSocket.accept();
                System.out.println("New Client Connected!");
                
                ClientHandler handler = new ClientHandler(socket);
                clients.add(handler);
                handler.start();
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
     //  TEST ONLY Dont try it at home 
    private void addTestClients() {
        
        //  client 1
        ClientHandler player1 = new ClientHandler(null) {
            @Override
            public void run() {}
        };
        player1.setUsername("player1");

        // fake client 2
        ClientHandler player2 = new ClientHandler(null) {
            @Override
            public void run() {}
        };
        player2.setUsername("player2");

        clients.add(player1);
        clients.add(player2);

        System.out.println(" added: player1, player2");
    }


}
