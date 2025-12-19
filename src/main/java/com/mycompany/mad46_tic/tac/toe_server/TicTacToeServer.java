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


}
