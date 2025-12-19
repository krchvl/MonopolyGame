package com.krchvl.MonopolyGame.fx.setup;

import com.krchvl.MonopolyGame.core.Board;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.stage.Window;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class NewGameDialog {
    private final Board board;
    private final List<NewGameSettings.PlayerCfg> defaults;

    public NewGameDialog(Board board, List<NewGameSettings.PlayerCfg> defaultPlayers) {
        this.board = board;
        this.defaults = defaultPlayers;
    }

    public Optional<NewGameSettings> show(Window owner) {
        try {
            FXMLLoader loader = new FXMLLoader(NewGameDialog.class.getResource("/fxml/new_game_dialog.fxml"));
            DialogPane pane = loader.load();
            NewGameDialogController controller = loader.getController();
            controller.init(board, defaults);

            Dialog<NewGameSettings> d = new Dialog<>();
            d.setDialogPane(pane);
            d.initOwner(owner);
            d.setTitle("Новая игра");

            final NewGameSettings[] prepared = new NewGameSettings[1];

            Button okBtn = (Button) pane.lookupButton(ButtonType.OK);
            okBtn.addEventFilter(javafx.event.ActionEvent.ACTION, evt -> {
                controller.commitEdits();
                try {
                    prepared[0] = controller.buildSettings();
                } catch (Exception ex) {
                    prepared[0] = null;
                    new Alert(Alert.AlertType.ERROR, ex.getMessage(), ButtonType.OK).showAndWait();
                    evt.consume();
                }
            });

            d.setResultConverter(bt -> (bt == ButtonType.OK) ? prepared[0] : null);

            return d.showAndWait();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}