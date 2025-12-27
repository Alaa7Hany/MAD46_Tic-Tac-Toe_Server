/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.mad46_tic.tac.toe_server;

import com.mycompany.mad46_tic_tac_toe_server.db.DatabaseHandler;
import com.mycompany.mad46_tic_tac_toe_server.db.UserAuthException;
import com.mycompany.tictactoeshared.InvitationDTO;
import com.mycompany.tictactoeshared.LoginDTO;
import com.mycompany.tictactoeshared.MoveDTO;
import com.mycompany.tictactoeshared.PlayerDTO;
import com.mycompany.tictactoeshared.RematchDTO;
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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.Vector;

/**
 *
 * @author siam
 */
public class ClientHandler extends Thread {

    private Socket socket;
    private ObjectInputStream input;
    private ObjectOutputStream output;
//    private String username; 
    private PlayerDTO currentPlayer;

    public ClientHandler(Socket socket) {
        this.socket = socket;

        try {

            if (socket != null) {   // this is guard for null test emad throws here  when the connection is ready feel free to remove 
                output = new ObjectOutputStream(socket.getOutputStream());
                input = new ObjectInputStream(socket.getInputStream());
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

                switch (received.getType()) {
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
                        System.out.println("##################I'm hereeeeeeeeeeeeeeeee");
                        getOnlinePlayersForLobby();
                        break;
                    case REGISTER:
                        register((LoginDTO) received.getData());
                        break;
                    case MOVE:
                        handleMove((MoveDTO) received.getData());
                        break;
                    case LOGOUT:
                        handleLogout(); 
                         break;
                    case REMATCH_REQUEST:
                        handleRematch((RematchDTO) received.getData());
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
            TicTacToeServer.broadCastPlayerList();
        }
    }

    private void login(LoginDTO loginData) {
        Response response;
        try {
            PlayerDTO playerData = new DatabaseHandler().login(loginData);
            this.currentPlayer = playerData;
            response = new Response(Response.Status.SUCCESS, playerData);

        } catch (UserAuthException ex) {
            response = new Response(Response.Status.FAILURE, ex.getMessage());
        } catch (SQLException ex) {
            System.getLogger(ClientHandler.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
            response = new Response(Response.Status.FAILURE, "Something went wrong");
        }

        try {
            output.writeObject(response);
            output.flush();
            if (response.getStatus() == Response.Status.SUCCESS) {
                // add the client only if they manages to login
                TicTacToeServer.clients.add(this);
                TicTacToeServer.broadCastPlayerList();
            }
        } catch (Exception e) {
        }
    }

    private void register(LoginDTO loginData) {
        Response response;
        try {
            PlayerDTO playerData = new DatabaseHandler().register(loginData);

            System.out.println("Player Data retrieved");
            System.out.println(playerData.getUsername());

            response = new Response(Response.Status.SUCCESS, playerData);
            this.currentPlayer = playerData;
        } catch (UserAuthException ex) {
            response = new Response(Response.Status.FAILURE, ex.getMessage());
        } catch (SQLException ex) {
            System.getLogger(ClientHandler.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
            response = new Response(Response.Status.FAILURE, "Something went wrong");

        }

        try {
            output.writeObject(response);
            output.flush();
            if (response.getStatus() == Response.Status.SUCCESS) {
                TicTacToeServer.clients.add(this);
                TicTacToeServer.broadCastPlayerList();
            }
        } catch (Exception e) {
        }
    }

    public String getUsername() {
        return currentPlayer.getUsername();
    }

    public PlayerDTO getPlayer() {
        return currentPlayer;
    }

    public ObjectOutputStream getOutput() {
        return output;
    }
//    public void setUsername(String username) { this.currentPlayer = username; }

    private void handleInvite(InvitationDTO invite) {
        System.out.println("server side handle invite");
        System.out.println("Invite: from " + invite.getFromUsername().getUsername() + " to " + invite.getToUsername().getUsername());

        ClientHandler receiver = null;
        System.out.println("Clients count: " + TicTacToeServer.clients.size());
        for (ClientHandler client : TicTacToeServer.clients) {
            System.out.println("  Checking client: '" + client.getUsername() + "' (null=" + (client.getUsername() == null) + ")");
            if (client.getUsername() != null
                    && invite.getToUsername().getUsername().equals(client.getUsername())) {
                receiver = client;
                System.out.println(" MATCH FOUND: " + receiver.getUsername());
                break;
            }
        }

        if (receiver != null) {
            try {
                System.out.println(" Sending INVITE_RECEIVED to " + receiver.getUsername());
                Request push = new Request(RequestType.INVITE_RECEIVED, invite);
                receiver.output.writeObject(push);
                receiver.output.flush();

                Response response = new Response(Response.Status.SUCCESS, "Invite sent");
                this.output.writeObject(response);
                this.output.flush();
                System.out.println("Invite flow COMPLETE");
            } catch (IOException e) {
                System.out.println(" Send error: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("NO RECEIVER - DUMPING ALL CLIENTS:");
            for (ClientHandler client : TicTacToeServer.clients) {
                System.out.println("  Client object: " + client);
                System.out.println("  Client username: '" + client.getUsername() + "'");
            }
        }
    }

    private void handleAcceptInvite(InvitationDTO invite) throws IOException { // return 2 client handler 

        String from = invite.getFromUsername().getUsername();
        String to = invite.getToUsername().getUsername();

        ClientHandler sender = null;
        ClientHandler receiver = null;

        for (ClientHandler client : TicTacToeServer.clients) {
            if (from.equals(client.getUsername())) {
                sender = client;
            }
            if (to.equals(client.getUsername())) {
                receiver = client;
            }
        }

        if (sender != null && receiver != null) {
            Request r1 = new Request(RequestType.ACCEPT_INVITE, new InvitationDTO(sender.getPlayer(), receiver.getPlayer()));
            Request r2 = new Request(RequestType.ACCEPT_INVITE, new InvitationDTO(receiver.getPlayer(), sender.getPlayer()));
            sender.output.writeObject(r1);
            receiver.output.writeObject(r2);
            startGameInOnlineMode(sender, receiver);
        }
    }

    private void handleRejectInvite(InvitationDTO invite) {
        String from = invite.getFromUsername().getUsername();

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
//            List<PlayerDTO> initialPlayers
//                    = new DatabaseHandler().getOnlinePlayersForLobby();

            Vector<ClientHandler> currentClients = new Vector<>(TicTacToeServer.clients);

            List<PlayerDTO> players = new ArrayList<>();
            for (ClientHandler client : currentClients) {
                if (client.currentPlayer != null) {
                    players.add(client.currentPlayer);
                }
            }

            System.out.println("##################Number of players: " + players.size());

            Request request = new Request(RequestType.GET_ONLINE_PLAYERS, players);
            output.writeObject(request);
            output.flush();

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void startGameInOnlineMode(ClientHandler p1, ClientHandler p2) {

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
    
    private void handleLogout(){
        System.out.println(currentPlayer.getUsername()+"logout");
        TicTacToeServer.clients.remove(this);
        TicTacToeServer.broadCastPlayerList();
        closeConnection();
    }

    private void handleRematch(RematchDTO dto) {

        GameSession session = TicTacToeServer.sessions.get(dto.getSessionId());

        if (session == null) {
            return;
        }

        if (session.playerX.getUsername().equals(dto.getUsername())) {
            session.xRematch = true;
        }

        if (session.playerO.getUsername().equals(dto.getUsername())) {
            session.oRematch = true;
        }

        if (session.xRematch && session.oRematch) {
            startRematch(session);
        }
    }

    private void startRematch(GameSession oldSession) {

        String oldId = oldSession.sessionID;

        String newSessionId = UUID.randomUUID().toString();

        GameSession newSession = new GameSession(
                newSessionId,
                oldSession.playerX,
                oldSession.playerO
        );

        TicTacToeServer.sessions.put(newSessionId, newSession);

        try {
            oldSession.playerX.getOutput().writeObject(
                    new Request(
                            RequestType.START_GAME,
                            new StartGameDTO(newSessionId, "x")
                    )
            );

            oldSession.playerO.getOutput().writeObject(
                    new Request(
                            RequestType.START_GAME,
                            new StartGameDTO(newSessionId, "o")
                    )
            );

        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        TicTacToeServer.sessions.remove(oldId);
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
