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

    public PlayerDTO login(LoginDTO loginDTO) throws UserAuthException {
        
        // Check first if the user exist or not, if not then throw exception
        if(!isUserExist(loginDTO.getUsername())){
            throw new UserAuthException("Player Doesn't exist");
        }

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
            }else{
                // the user does exist but the password is wrong
                throw new UserAuthException("Wrong Password");
            }
            

        } catch (SQLException e) {
        }
        
        throw new UserAuthException("Something went Wrong");
    }

    public PlayerDTO register(LoginDTO loginDTO) throws UserAuthException {
        
        if(isUserExist(loginDTO.getUsername())){
            throw new UserAuthException("Player already exist");
        }

        String sql =
        "INSERT INTO USERS (ID ,NAME, password, score, STATUS) " +
        "VALUES (?,?, ?, 0, 1)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            
            
            ps.setInt(1, getTotalPlayers()+1);
            ps.setString(2, loginDTO.getUsername());
            ps.setString(3, loginDTO.getPassword());
            ps.executeUpdate();

            return new PlayerDTO(
                    loginDTO.getUsername(),
                    0,
                    true
            );

        } catch (SQLException e) {
        }
        throw new UserAuthException("Something went wrong");
    }
    

    //for lobby
    public List<PlayerDTO> getOnlinePlayersForLobby() {

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
    
    public int getOnlinePlayers() {
        int onlineNum = 0;
        
        String sql = "SELECT COUNT(*) FROM USERS WHERE STATUS = 1"; 

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                onlineNum = rs.getInt(1); 
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return onlineNum;
    }
    
    public int getTotalPlayers() {
        int offlineNum = 0;
        String sql = "SELECT COUNT(*) FROM USERS"; 

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                offlineNum = rs.getInt(1);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return offlineNum;
    }
    
    
    private boolean isUserExist(String userName){
        String sql =  "SELECT NAME FROM USERS WHERE NAME=?";
        try(PreparedStatement ps = conn.prepareStatement(sql)){
            ps.setString(1, userName);
              
            ResultSet rs = ps.executeQuery();
            boolean exist = rs.next();
            System.out.println("in isUserExist" + exist);
            return exist;
        } catch (SQLException ex) {
            System.getLogger(DatabaseHandler.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
            return false;
        }
    }
    
}
