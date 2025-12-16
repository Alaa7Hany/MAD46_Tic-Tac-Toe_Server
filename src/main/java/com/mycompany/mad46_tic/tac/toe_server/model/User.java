/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.mad46_tic.tac.toe_server.model;

/**
 *
 * @author emada
 */
public class User {
    private int id;
    private String name;
    private String password;
    private int score;

    public User() {}

    public User(int id, String name, String password, int score) {
        this.id = id;
        this.name = name;
        this.password = password;
        this.score = score;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
    
}