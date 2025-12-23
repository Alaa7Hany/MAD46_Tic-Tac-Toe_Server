/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.mad46_tic.tac.toe_server;

/**
 *
 * @author siam
 */
public class GameSession {
    public String sessionID;
    public ClientHandler playerX;
    public ClientHandler playerO;

    public GameSession(String id, ClientHandler x, ClientHandler o){
        this.sessionID = id;
        this.playerX = x;
        this.playerO = o;
    }
}
