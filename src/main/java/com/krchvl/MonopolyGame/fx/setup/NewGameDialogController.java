package com.krchvl.MonopolyGame.fx.setup;

import com.krchvl.MonopolyGame.core.Board;
import com.krchvl.MonopolyGame.core.PlayerGroup;
import com.krchvl.MonopolyGame.core.tiles.CompanyTile;
import com.krchvl.MonopolyGame.core.tiles.Tile;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.converter.IntegerStringConverter;

import java.util.*;
import java.util.stream.Collectors;

public class NewGameDialogController {

    @FXML private TableView<PlayerRow> playersTable;
    @FXML private TableColumn<PlayerRow, String> colType;
    @FXML private TableColumn<PlayerRow, String> colName;
    @FXML private TableColumn<PlayerRow, PlayerGroup> colColor;
    @FXML private TableColumn<PlayerRow, Integer> colReserve;

    @FXML private TableView<TileRow> tilesTable;
    @FXML private TableColumn<TileRow, Integer> colIdx;
    @FXML private TableColumn<TileRow, String> colTName;
    @FXML private TableColumn<TileRow, String> colTypeName;
    @FXML private TableColumn<TileRow, String> colGroup;
    @FXML private TableColumn<TileRow, Integer> colPrice;
    @FXML private TableColumn<TileRow, Integer> colStars;

    private final ObservableList<PlayerRow> players = FXCollections.observableArrayList();
    private final ObservableList<TileRow> tileRows = FXCollections.observableArrayList();

    private Board sourceBoard;

    @FXML
    private void initialize() {
        playersTable.setEditable(true);
        tilesTable.setEditable(true);

        playersTable.setItems(players);
        playersTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        colType.setCellValueFactory(c ->
                Bindings.createStringBinding(
                        () -> c.getValue().bot.get() ? "Бот" : "Человек",
                        c.getValue().bot
                )
        );
        colType.setCellFactory(ComboBoxTableCell.forTableColumn(FXCollections.observableArrayList("Человек", "Бот")));
        colType.setOnEditCommit(e -> e.getRowValue().bot.set("Бот".equals(e.getNewValue())));
        colType.setEditable(true);

        colName.setCellValueFactory(c -> c.getValue().name);
        colName.setCellFactory(TextFieldTableCell.forTableColumn());
        colName.setOnEditCommit(e -> e.getRowValue().name.set(e.getNewValue()));
        colName.setEditable(true);

        colColor.setCellValueFactory(c -> c.getValue().color);
        colColor.setCellFactory(ComboBoxTableCell.forTableColumn(FXCollections.observableArrayList(PlayerGroup.values())));
        colColor.setOnEditCommit(e -> e.getRowValue().color.set(e.getNewValue()));
        colColor.setEditable(true);

        colReserve.setCellValueFactory(c -> c.getValue().reserve.asObject());
        colReserve.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        colReserve.setOnEditCommit(e -> e.getRowValue().reserve.set(Math.max(0, e.getNewValue())));
        colReserve.setEditable(true);

        tilesTable.setItems(tileRows);

        colIdx.setCellValueFactory(c -> c.getValue().index.asObject());
        colTName.setCellValueFactory(c -> c.getValue().name);
        colTypeName.setCellValueFactory(c -> c.getValue().typeName);
        colGroup.setCellValueFactory(c -> c.getValue().group);
        colPrice.setCellValueFactory(c -> c.getValue().price.asObject());
        colStars.setCellValueFactory(c -> c.getValue().stars.asObject());

        colPrice.setCellFactory(tc -> new EditingIntegerCell(row -> row.priceEditable));
        colPrice.setOnEditCommit(e -> {
            TileRow r = e.getRowValue();
            if (r.priceEditable.get()) r.price.set(Math.max(0, e.getNewValue()));
        });
        colPrice.setEditable(true);

        colStars.setCellFactory(tc -> new EditingIntegerCell(row -> row.starsEditable));
        colStars.setOnEditCommit(e -> {
            TileRow r = e.getRowValue();
            if (r.starsEditable.get()) {
                int v = Math.max(0, Math.min(5, e.getNewValue()));
                r.stars.set(v);
            }
        });
        colStars.setEditable(true);
    }

    public void init(Board board, List<NewGameSettings.PlayerCfg> defaultPlayers) {
        this.sourceBoard = board;
        players.setAll(defaultPlayers.stream().map(PlayerRow::fromCfg).collect(Collectors.toList()));

        tileRows.clear();
        for (int i = 0; i < board.size(); i++) {
            Tile t = board.getTile(i);
            TileRow row = TileRow.fromTile(i, t);
            tileRows.add(row);
        }
    }

    @FXML
    private void onAddHuman() {
        PlayerRow pr = new PlayerRow(
                "Игрок " + (players.size() + 1),
                PlayerGroup.values()[players.size() % PlayerGroup.values().length],
                false,
                0
        );
        players.add(pr);
        playersTable.getSelectionModel().select(pr);
        playersTable.scrollTo(pr);
    }

    @FXML
    private void onAddBot() {
        PlayerRow pr = new PlayerRow(
                "Bot " + (players.size() + 1),
                PlayerGroup.values()[players.size() % PlayerGroup.values().length],
                true,
                200
        );
        players.add(pr);
        playersTable.getSelectionModel().select(pr);
        playersTable.scrollTo(pr);
    }

    @FXML
    private void onRemoveSelected() {
        int idx = playersTable.getSelectionModel().getSelectedIndex();
        if (idx >= 0) players.remove(idx);
    }

    @FXML
    private void onMoveUp() {
        int idx = playersTable.getSelectionModel().getSelectedIndex();
        if (idx > 0) {
            Collections.swap(players, idx, idx - 1);
            playersTable.getSelectionModel().select(idx - 1);
            playersTable.scrollTo(idx - 1);
        }
    }

    @FXML
    private void onMoveDown() {
        int idx = playersTable.getSelectionModel().getSelectedIndex();
        if (idx >= 0 && idx < players.size() - 1) {
            Collections.swap(players, idx, idx + 1);
            playersTable.getSelectionModel().select(idx + 1);
            playersTable.scrollTo(idx + 1);
        }
    }

    @FXML
    private void onResetTiles() {
        tileRows.forEach(TileRow::resetToOriginal);
        tilesTable.refresh();
    }

    public void commitEdits() {
        if (playersTable.getEditingCell() != null) {
            playersTable.edit(-1, null);
        }
        if (tilesTable.getEditingCell() != null) {
            tilesTable.edit(-1, null);
        }
    }

    public NewGameSettings buildSettings() {
        validateOrThrow();

        NewGameSettings settings = new NewGameSettings();
        for (PlayerRow pr : players) {
            settings.getPlayers().add(new NewGameSettings.PlayerCfg(
                    pr.name.get(), pr.color.get(), pr.bot.get(), pr.reserve.get()
            ));
        }
        for (TileRow tr : tileRows) {
            if (tr.priceEditable.get() && tr.price.get() != tr.originalPrice) {
                settings.getPriceOverrides().put(tr.index.get(), tr.price.get());
            }
            if (tr.starsEditable.get() && tr.stars.get() != tr.originalStars) {
                settings.getInitialStars().put(tr.index.get(), tr.stars.get());
            }
        }

        settings.setBoardTemplate(sourceBoard);

        return settings;
    }

    private void validateOrThrow() {
        if (players.size() < 2) throw new IllegalStateException("Должно быть минимум 2 игрока");
        Set<PlayerGroup> used = new HashSet<>();
        for (PlayerRow pr : players) {
            if (pr.name.get().isBlank()) throw new IllegalStateException("Имя игрока не может быть пустым");
            if (!used.add(pr.color.get())) throw new IllegalStateException("Цвета игроков должны быть уникальны");
        }
    }

    public static class PlayerRow {
        final StringProperty name = new SimpleStringProperty();
        final ObjectProperty<PlayerGroup> color = new SimpleObjectProperty<>();
        final BooleanProperty bot = new SimpleBooleanProperty();
        final IntegerProperty reserve = new SimpleIntegerProperty();

        PlayerRow(String n, PlayerGroup c, boolean isBot, int res) {
            name.set(n); color.set(c); bot.set(isBot); reserve.set(res);
        }

        static PlayerRow fromCfg(NewGameSettings.PlayerCfg c) {
            return new PlayerRow(c.name, c.color, c.bot, c.botReserve);
        }
    }

    public static class TileRow {
        final IntegerProperty index = new SimpleIntegerProperty();
        final StringProperty name = new SimpleStringProperty();
        final StringProperty typeName = new SimpleStringProperty();
        final StringProperty group = new SimpleStringProperty();

        final IntegerProperty price = new SimpleIntegerProperty();
        final IntegerProperty stars = new SimpleIntegerProperty();

        final BooleanProperty priceEditable = new SimpleBooleanProperty(false);
        final BooleanProperty starsEditable = new SimpleBooleanProperty(false);

        int originalPrice;
        int originalStars;

        static TileRow fromTile(int idx, Tile t) {
            TileRow r = new TileRow();
            r.index.set(idx);
            r.name.set(t.getName());
            r.typeName.set(t.getClass().getSimpleName());

            if (t instanceof CompanyTile ct) {
                r.group.set(ct.getGroup().name());
                r.originalPrice = ct.getPrice();
                r.price.set(ct.getPrice());
                r.originalStars = ct.getStars();
                r.stars.set(ct.getStars());
                r.priceEditable.set(true);
                r.starsEditable.set(true);
            } else {
                r.group.set("-");
                r.originalPrice = 0;
                r.price.set(0);
                r.originalStars = 0;
                r.stars.set(0);
            }
            return r;
        }

        void resetToOriginal() {
            price.set(originalPrice);
            stars.set(originalStars);
        }
    }

    private static class EditingIntegerCell extends TextFieldTableCell<TileRow, Integer> {
        private final java.util.function.Function<TileRow, BooleanProperty> enabledProvider;

        EditingIntegerCell(java.util.function.Function<TileRow, BooleanProperty> enabledProvider) {
            super(new IntegerStringConverter());
            this.enabledProvider = enabledProvider;
        }

        @Override
        public void updateItem(Integer item, boolean empty) {
            super.updateItem(item, empty);
            if (empty) {
                setEditable(false);
                setDisable(true);
                return;
            }
            TileRow row = getTableView().getItems().get(getIndex());
            boolean enabled = enabledProvider.apply(row).get();
            setEditable(enabled);
            setDisable(!enabled);
        }
    }
}