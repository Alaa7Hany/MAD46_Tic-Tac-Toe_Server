/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.mad46_tic.tac.toe_server;

import com.mycompany.mad46_tic_tac_toe_server.db.DatabaseHandler;
import com.mycompany.tictactoeshared.InvitationDTO;
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
    private String username; 
   
    public ClientHandler(Socket socket) {
        this.socket = socket;

        try {
            
            if (socket != null) {   // this is guard for null test emad throws here  when the connection is ready feel free to remove 
                output = new ObjectOutputStream(socket.getOutputStream());
                input  = new ObjectInputStream(socket.getInputStream());
            }

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
                    case INVITE_PLAYER:
                        handleInvite((InvitationDTO) received.getData());
                        break;
                        
                    default:
                        break;
                }
                
                

            }

        } catch (IOException | ClassNotFoundException ex) {
            System.out.println("Client disconnected!");
        } finally {
            closeConnection();
        }
    }
    
    
    private void login(LoginDTO loginData){
        try {
            PlayerDTO playerData = new DatabaseHandler().login(loginData);
            System.out.println("Player Data retrieved");
            Response response;
            if(playerData != null){
                this.username = playerData.getUsername();
                response = new Response(Response.Status.SUCCESS, playerData);
            }
            else
                response = new Response(Response.Status.FAILURE,new PlayerDTO("Test", 4, true));
            output.writeObject(response);
            output.flush();
        } catch (SQLException ex) {
            System.getLogger(ClientHandler.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        } catch (IOException ex) {
            System.getLogger(ClientHandler.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
    }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    
    private void handleInvite(InvitationDTO invite) {

        for (ClientHandler client : TicTacToeServer.clients) {
            System.out.println("checking client: " + client.getUsername());
            if (invite.getToUsername().equals(client.getUsername())) {
                 System.out.println("Found target client: " + client.getUsername());
                // UnComment when finish the connection srry 
                //try {
                    //Response response = new Response(Response.Status.SUCCESS,invite);
                    //client.output.writeObject(response);
                    //client.output.flush();
                //} catch (IOException e) {
                //    e.printStackTrace();
                //}
                return;
            }
        }
        
        System.out.println("not found");

    }
    
    private void closeConnection() {

        try {
            input.close();
            output.close();
            socket.close();

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
