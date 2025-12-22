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
import com.mycompany.tictactoeshared.Response.Status;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.SQLException;
import java.util.List;

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
                    case ACCEPT_INVITE:
                        InvitationDTO acceptInvite = (InvitationDTO) received.getData();
                        handleAcceptInvite(acceptInvite);
                        break;
                    case REJECT_INVITE:
                        InvitationDTO rejectInvite = (InvitationDTO) received.getData();
                        handleRejectInvite(rejectInvite);
                        break;
                    case GET_ONLINE_PLAYERS:
                        getOnlinePlayersForLobby();
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
            if(playerData != null){
                this.username = playerData.getUsername();
                response = new Response(Response.Status.SUCCESS, playerData);
            }
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
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    
    private void handleInvite(InvitationDTO invite) {

        for (ClientHandler client : TicTacToeServer.clients) {
            System.out.println("checking client: " + client.getUsername());
            if (invite.getToUsername().equals(client.getUsername())) {
                 System.out.println("Found target client: " + client.getUsername());
                try {
                    Request push =new Request(RequestType.INVITE_RECEIVED, invite);
                    client.output.writeObject(push);
                    client.output.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return;
            }
        }
        
        System.out.println("not found");

    }
    
    private void handleAcceptInvite(InvitationDTO invite) { 

        String from = invite.getFromUsername(); 
        String to   = invite.getToUsername();   

        ClientHandler sender = null;
        ClientHandler receiver = null;

        for (ClientHandler client : TicTacToeServer.clients) {
            if (from.equals(client.getUsername()))  sender = client;
            if (to.equals(client.getUsername()))    receiver = client;
        }

        if (sender != null && receiver != null) {
            try {
                //edit : send  request... type invitatin accept 
                Request startGame = new Request(RequestType.START_GAME, invite);

                sender.output.writeObject(startGame);
                sender.output.flush();

                receiver.output.writeObject(startGame);
                receiver.output.flush();

                System.out.println("Game starting: " + from + " vs " + to);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    private void handleRejectInvite(InvitationDTO invite) { 
        String from = invite.getFromUsername(); 

        for (ClientHandler client : TicTacToeServer.clients) {
            if (from.equals(client.getUsername())) {
                try {
                    //edit : send  request... type invitatin reject 
                    Request rejected = new Request(RequestType.INVITE_REJECTED, invite);
                    client.output.writeObject(rejected);
                    client.output.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
        System.out.println("Invite rejected by " + invite.getToUsername());
    }
    

    private void getOnlinePlayersForLobby() {

        try {
            List<PlayerDTO> players =
                new DatabaseHandler().getOnlinePlayersForLobby();

            Response response =
                new Response(Response.Status.SUCCESS, players);

            output.writeObject(response);
            output.flush();

        } catch (SQLException | IOException ex) {
            ex.printStackTrace();
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
