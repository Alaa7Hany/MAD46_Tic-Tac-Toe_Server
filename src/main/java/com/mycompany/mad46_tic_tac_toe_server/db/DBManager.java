/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.mad46_tic_tac_toe_server.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author LAPTOP
 */


public class DBManager {

    private static Connection conn;

    public static Connection getConnection() throws SQLException {
        if (conn == null) {

            String url = "jdbc:derby:TicTacToeDB;create=true";
            conn = DriverManager.getConnection(url);

            createTablesIfNotExist(conn);
        }
        return conn;
    }

    private static void createTablesIfNotExist(Connection conn) {

        String usersTable =
            "CREATE TABLE USERS (" +
            "ID INTEGER NOT NULL PRIMARY KEY, " +
            "NAME VARCHAR(50) NOT NULL, " +
            "PASSWORD VARCHAR(50) NOT NULL, " +
            "SCORE INTEGER DEFAULT 0 NOT NULL, " +
            "STATUS SMALLINT DEFAULT 0 NOT NULL)";

        String gamesTable =
            "CREATE TABLE GAMES (" +
            "ID INTEGER NOT NULL PRIMARY KEY, " +
            "PLAYER1 VARCHAR(50) NOT NULL, " +
            "PLAYER2 VARCHAR(50) NOT NULL, " +
            "WINNER VARCHAR(50) NOT NULL)";

        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(usersTable);
        } catch (SQLException e) {
            if (!"X0Y32".equals(e.getSQLState())) {
                e.printStackTrace();
            }
        }

        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(gamesTable);
        } catch (SQLException e) {
            if (!"X0Y32".equals(e.getSQLState())) {
                e.printStackTrace();
            }
        }
    }
}


