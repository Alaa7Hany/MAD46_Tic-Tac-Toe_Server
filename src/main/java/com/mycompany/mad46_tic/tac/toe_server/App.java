package com.mycompany.mad46_tic.tac.toe_server;

import com.mycompany.mad46_tic_tac_toe_server.db.DBManager;
import com.mycompany.tictactoeshared.PlayerDTO;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.DriverManager;
import java.sql.SQLException;
import javafx.scene.image.Image;

/**
 * JavaFX App
 */
public class App extends Application {

    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        scene = new Scene(loadFXML("primary"), 640, 480);
        stage.setScene(scene);
        stage.setTitle("TicTacToe-Server");
        stage.getIcons().add(new Image(App.class.getResourceAsStream("/images/xo.png")));

        stage.setResizable(false);
        stage.show();
        
    }

    static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    @Override
    public void stop() throws Exception {
        TicTacToeServer.clearClients();
        try {
            DriverManager.getConnection("jdbc:derby:;shutdown=true");
        } catch (SQLException e) {
            if ("XJ015".equals(e.getSQLState())) {
                System.out.println("Derby shut down normally");
            }
        }
        super.stop(); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/OverriddenMethodBody
    }

    public static void main(String[] args) {
        try {
            System.out.println("Starting server...");
            DBManager.getConnection();
            System.out.println("DB CONNECTED SUCCESSFULLY ✅");
        } catch (Exception e) {
            System.out.println("DB CONNECTION FAILED ❌");
            e.printStackTrace();
        }
        launch();

    }

}
