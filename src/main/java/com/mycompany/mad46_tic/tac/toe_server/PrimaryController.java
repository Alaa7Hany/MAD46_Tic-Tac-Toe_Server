package com.mycompany.mad46_tic.tac.toe_server;

import com.mycompany.mad46_tic_tac_toe_server.db.DatabaseHandler;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

/**
 * FXML Controller class
 *
 * @author siam
 */
public class PrimaryController implements Initializable {

    @FXML
    private ImageView offOnImage;
    @FXML
    private Label totalUsersLbl;
    @FXML
    private Label onlineUsersLbl;
    @FXML
    private Label offlineUsersLbl;
    @FXML
    private PieChart pieChart;

    private PieChart.Data onlineSlice;
    private PieChart.Data offlineSlice;

    private int onlineNum;
    private int offlineNum;
    
    private DatabaseHandler dbh;
    
    private TicTacToeServer server;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        server = new TicTacToeServer();
         
        Image offlineImg = new Image(getClass().getResourceAsStream("/images/offline_icon.png"));
        offOnImage.setImage(offlineImg);
        
        resetUI();
        
        try {
            dbh = new DatabaseHandler();
        } catch (SQLException ex) {
            System.getLogger(PrimaryController.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }

    }
    
    private void initData(){
        totalUsersLbl.setText(Integer.toString(onlineNum+offlineNum));
        onlineUsersLbl.setText(Integer.toString(onlineNum));
        offlineUsersLbl.setText(Integer.toString(offlineNum));

        onlineSlice = new PieChart.Data("Online", onlineNum);
        offlineSlice = new PieChart.Data("Offline", offlineNum);

        pieChart.setData(FXCollections.observableArrayList(onlineSlice, offlineSlice));

        // tooltip for hover 
        for (PieChart.Data data : pieChart.getData()) {
            Node sliceNode = data.getNode(); 

            if (sliceNode != null) {
                Tooltip tooltip = new Tooltip();
                // Bind: assign the text of tooltip to change whenever the pieSlice value changes (Like state in flutter)
                tooltip.textProperty().bind(data.pieValueProperty().asString(data.getName() + ": %.0f"));
                tooltip.setShowDelay(Duration.millis(100));
                Tooltip.install(sliceNode, tooltip);

                // hover effect on the lice itself
                sliceNode.setOnMouseEntered(e -> {
                    sliceNode.setStyle("-fx-opacity: 0.8; -fx-cursor: hand;");
                });
                sliceNode.setOnMouseExited(e -> {
                    sliceNode.setStyle("-fx-opacity: 1.0;");
                });
            }
        }
    }
    
    private void resetUI() {
        Platform.runLater(() -> {
            
            totalUsersLbl.setText("--");
            onlineUsersLbl.setText("--");
            offlineUsersLbl.setText("--");

            pieChart.getData().clear();
        });
    }

    public void updateData(int onlineUsers, int offlineUsers) {
        
        onlineNum = onlineUsers;
        offlineNum = offlineUsers;
        
        Platform.runLater(()->{
            onlineSlice.setPieValue(onlineNum);
            offlineSlice.setPieValue(offlineNum);

            onlineUsersLbl.setText(String.valueOf(onlineNum));
            offlineUsersLbl.setText(String.valueOf(offlineNum));
            totalUsersLbl.setText(String.valueOf(onlineNum + offlineNum));
        });
    }

    @FXML
    private void onStartPressed(ActionEvent event) {
        if(!TicTacToeServer.isRunning){
            Thread serverThread = new Thread(() -> {
                if (server == null) server = new TicTacToeServer();
                server.startServer();
            });
            // This ensures the thread dies when the app closes
            serverThread.setDaemon(true); 
            serverThread.start();
            
            onlineNum = dbh.getOnlinePlayers(); 
            offlineNum = dbh.getTotalPlayers() - onlineNum;
            Platform.runLater(() -> initData());
            try {
                Image onlineImg = new Image(getClass().getResourceAsStream("/images/online_icon.png"));
                offOnImage.setImage(onlineImg);
            } catch (Exception e) {
                System.out.println("Error loading online image: " + e.getMessage());
            }
        }
    }

    @FXML
    private void onStopPressed(ActionEvent event) {
       if(TicTacToeServer.isRunning){
            if (server != null) {
               server.stopServer();
            }
            
            resetUI();
           
            try {
                Image offlineImg = new Image(getClass().getResourceAsStream("/images/offline_icon.png"));
                offOnImage.setImage(offlineImg);
            } catch (Exception e) {
                System.out.println("Error loading offline image: " + e.getMessage());
            }
       }
    }
}