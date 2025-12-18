module com.mycompany.mad46_tic.tac.toe_server {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.mycompany.mad46_tic.tac.toe_server to javafx.fxml;
    exports com.mycompany.mad46_tic.tac.toe_server;
    requires com.mycompany.tictactoeshared;
}
