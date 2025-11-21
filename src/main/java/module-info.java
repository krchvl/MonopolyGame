module com.krchvl.MonopolyGame {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.krchvl.MonopolyGame.fx to javafx.fxml;

    exports com.krchvl.MonopolyGame.fx;
}