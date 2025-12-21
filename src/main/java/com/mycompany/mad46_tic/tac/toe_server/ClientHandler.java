/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.mad46_tic.tac.toe_server;

import com.mycompany.mad46_tic_tac_toe_server.db.DatabaseHandler;
import com.mycompany.tictactoeshared.LoginDTO;
import com.mycompany.tictactoeshared.MoveDTO;
import com.mycompany.tictactoeshared.PlayerDTO;
import com.mycompany.tictactoeshared.Request;
import com.mycompany.tictactoeshared.RequestType;
import static com.mycompany.tictactoeshared.RequestType.MOVE;
import com.mycompany.tictactoeshared.Response;
import com.mycompany.tictactoeshared.StartGameDTO;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

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
            // to test only will send based on invtion data 
            if (TicTacToeServer.clients.size() >= 2) {
                startGameForTesting();
            }
            while (true) {

                Request received = (Request) input.readObject();

                switch (received.getType()) {
                    case LOGIN:
                        login((LoginDTO) received.getData());
                        break;
                    case GET_ONLINE_PLAYERS:
                        getOnlinePlayersForLobby();
                        break;
                    case REGISTER:
                        register((LoginDTO) received.getData());
                        break;
                    case MOVE:
                        handleMove((MoveDTO) received.getData());
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

    private void login(LoginDTO loginData) {
        try {
            PlayerDTO playerData = new DatabaseHandler().login(loginData);
            Response response;
            if (playerData != null) {
                response = new Response(Response.Status.SUCCESS, playerData);
            } else {
                response = new Response(Response.Status.FAILURE, "Failed to login");
            }
            output.writeObject(response);
            output.flush();
        } catch (SQLException ex) {
            System.getLogger(ClientHandler.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        } catch (IOException ex) {
            System.getLogger(ClientHandler.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
    }

    private void register(LoginDTO loginData) {
        try {
            PlayerDTO playerData = new DatabaseHandler().register(loginData);
            System.out.println("Player Data retrieved");
            System.out.println(playerData.getUsername());
            Response response;
            if (playerData != null) {
                response = new Response(Response.Status.SUCCESS, playerData);
            } else {
                response = new Response(Response.Status.FAILURE, "Failed Registeration");
            }
            output.writeObject(response);
            output.flush();
        } catch (SQLException ex) {
            System.getLogger(ClientHandler.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        } catch (IOException ex) {
            System.getLogger(ClientHandler.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
    }

    private void getOnlinePlayersForLobby() {

        try {
            List<PlayerDTO> players
                    = new DatabaseHandler().getOnlinePlayersForLobby();

            Response response
                    = new Response(Response.Status.SUCCESS, players);

            output.writeObject(response);
            output.flush();

        } catch (SQLException | IOException ex) {
            ex.printStackTrace();
        }
    }

    private void startGameForTesting() {
        ClientHandler p1 = TicTacToeServer.clients.get(0);
        ClientHandler p2 = TicTacToeServer.clients.get(1);

        String sessionID = UUID.randomUUID().toString();

        GameSession session = new GameSession(sessionID, p1, p2);
        TicTacToeServer.sessions.put(sessionID, session);

        try {
            Request r1 = new Request(RequestType.START_GAME, new StartGameDTO(sessionID, "x"));
            p1.output.writeObject(r1);
            p1.output.flush();

            Request r2 = new Request(RequestType.START_GAME, new StartGameDTO(sessionID, "o"));
            p2.output.writeObject(r2);
            p2.output.flush();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void handleMove(MoveDTO moveDTO) {

        GameSession session = TicTacToeServer.sessions.get(moveDTO.sessionId());
        if (session == null) {
            return;
        }

        try {

            if (moveDTO.getSymbol().equalsIgnoreCase("x")) {

                if (session.playerO != null) {

                    Request r = new Request(RequestType.MOVE, moveDTO);
                    session.playerO.output.writeObject(r);
                    session.playerO.output.flush();
                }

              
            } else if (moveDTO.getSymbol().equalsIgnoreCase("o")) {

                if (session.playerX != null) {

                    Request r = new Request(RequestType.MOVE, moveDTO);
                    session.playerX.output.writeObject(r);
                    session.playerX.output.flush();
                }

     
            }

        } catch (IOException e) {
            e.printStackTrace();
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
