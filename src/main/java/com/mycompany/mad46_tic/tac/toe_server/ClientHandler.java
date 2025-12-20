/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.mad46_tic.tac.toe_server;

import com.mycompany.mad46_tic_tac_toe_server.db.DatabaseHandler;
import com.mycompany.tictactoeshared.LoginDTO;
import com.mycompany.tictactoeshared.PlayerDTO;
import com.mycompany.tictactoeshared.Request;
import com.mycompany.tictactoeshared.RequestType;
import com.mycompany.tictactoeshared.Response;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.SQLException;

/**
 *
 * @author siam
 */


public class ClientHandler extends Thread {

    private Socket socket;
    private ObjectInputStream input;
    private ObjectOutputStream output;

    public ClientHandler(Socket socket) {
        this.socket = socket;

        try {
            output = new ObjectOutputStream(socket.getOutputStream());
            input = new ObjectInputStream(socket.getInputStream());

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void run() {

        try {
            while (true) {

                Request received = (Request) input.readObject();
                
                
                switch(received.getType()){
                    case LOGIN:
                        login((LoginDTO) received.getData());
                        break;
                    case REGISTER:
                        register((LoginDTO) received.getData());
                        break;
                    default:
                        break;
                }
                
                

            }

        } catch (IOException | ClassNotFoundException ex) {
            System.out.println("Client disconnected!");
        } finally {
            TicTacToeServer.clients.remove(this);
            closeConnection();
        }
    }
    
    
    private void login(LoginDTO loginData){
        try {
            PlayerDTO playerData = new DatabaseHandler().login(loginData);
             Response response;
            if(playerData != null)
                response = new Response(Response.Status.SUCCESS, playerData);
            else
                response = new Response(Response.Status.FAILURE,"Failed to login");
            output.writeObject(response);
            output.flush();
        } catch (SQLException ex) {
            System.getLogger(ClientHandler.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        } catch (IOException ex) {
            System.getLogger(ClientHandler.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
    }
    private void register(LoginDTO loginData){
        try {
            PlayerDTO playerData = new DatabaseHandler().register(loginData);
            System.out.println("Player Data retrieved");
            System.out.println(playerData.getUsername());
            Response response;
            if(playerData != null)
                response = new Response(Response.Status.SUCCESS, playerData);
            else
                response = new Response(Response.Status.FAILURE,"Failed Registeration");
            output.writeObject(response);
            output.flush();
        } catch (SQLException ex) {
            System.getLogger(ClientHandler.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        } catch (IOException ex) {
            System.getLogger(ClientHandler.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
    }
    
    

    public void closeConnection() {

        try {
            input.close();
            output.close();
            socket.close();

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
