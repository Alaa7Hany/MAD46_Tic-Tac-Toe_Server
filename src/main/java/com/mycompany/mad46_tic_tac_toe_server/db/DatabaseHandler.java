/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.mad46_tic_tac_toe_server.db;

import com.mycompany.tictactoeshared.LoginDTO;
import com.mycompany.tictactoeshared.PlayerDTO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author LAPTOP
 */
public class DatabaseHandler {
    private final Connection conn;

    public DatabaseHandler() throws SQLException {
        this.conn = DBManager.getConnection();
    }

    public PlayerDTO login(LoginDTO loginDTO) {

        String sql =
        "SELECT NAME, SCORE, STATUS " +
        "FROM USERS " +
        "WHERE NAME=? AND PASSWORD=?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, loginDTO.getUsername());
            ps.setString(2, loginDTO.getPassword());

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new PlayerDTO(
                        rs.getString("NAME"),
                        rs.getInt("SCORE"),
                        rs.getBoolean("STATUS")
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null; 
    }

    public PlayerDTO register(LoginDTO loginDTO) {

        String sql =
        "INSERT INTO USERS (NAME, password, score, STATUS) " +
        "VALUES (?, ?, 0, true)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, loginDTO.getUsername());
            ps.setString(2, loginDTO.getPassword());
            ps.executeUpdate();

            return new PlayerDTO(
                    loginDTO.getUsername(),
                    0,
                    true
            );

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public List<PlayerDTO> getOnlinePlayers() {

        List<PlayerDTO> players = new ArrayList<>();

        String sql =
            "SELECT NAME, SCORE, STATUS " +
            "FROM USERS " +
            "WHERE STATUS = 1";

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                players.add(
                    new PlayerDTO(
                        rs.getString("NAME"),
                        rs.getInt("SCORE"),
                        rs.getBoolean("STATUS")
                    )
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return players;
    }
    
}
