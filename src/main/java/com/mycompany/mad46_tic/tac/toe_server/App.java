package com.mycompany.mad46_tic.tac.toe_server;

import com.mycompany.mad46_tic_tac_toe_server.db.DBManager;
import com.mycompany.tictactoeshared.PlayerDTO;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * JavaFX App
 */
public class App extends Application {

    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        scene = new Scene(loadFXML("primary"), 640, 480);
        stage.setScene(scene);
        stage.show();
       // Start server in background thread
       new Thread(() -> new TicTacToeServer()).start();
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
        // TODO: Close connections with all clients
        super.stop(); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/OverriddenMethodBody
    }
    
    

    public static void main(String[] args) {
          
        launch();
        try {
            System.out.println("Starting server...");
            DBManager.getConnection();
            System.out.println("DB CONNECTED SUCCESSFULLY ✅");
        } catch (Exception e) {
            System.out.println("DB CONNECTION FAILED ❌");
            e.printStackTrace();
        }
    }

}