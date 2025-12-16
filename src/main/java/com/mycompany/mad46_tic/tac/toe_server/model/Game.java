/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.mad46_tic.tac.toe_server.model;

/**
 *
 * @author emada
 */
public class Game {
    private int id;
    private String player1;
    private String player2;
    private String winner;

    public Game() {}

    public Game(int id, String player1, String player2, String winner) {
        this.id = id;
        this.player1 = player1;
        this.player2 = player2;
        this.winner = winner;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getPlayer1() { return player1; }
    public void setPlayer1(String player1) { this.player1 = player1; }

    public String getPlayer2() { return player2; }
    public void setPlayer2(String player2) { this.player2 = player2; }

    public String getWinner() { return winner; }
    public void setWinner(String winner) { this.winner = winner; }
    
}