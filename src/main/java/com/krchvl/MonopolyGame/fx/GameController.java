package com.krchvl.MonopolyGame.fx;

import com.krchvl.MonopolyGame.core.*;
import com.krchvl.MonopolyGame.core.control.*;
import com.krchvl.MonopolyGame.core.engine.*;
import com.krchvl.MonopolyGame.core.engine.events.*;
import com.krchvl.MonopolyGame.core.tiles.*;
import com.krchvl.MonopolyGame.fx.anim.DiceOverlay;
import com.krchvl.MonopolyGame.fx.anim.MovementAnimator;
import com.krchvl.MonopolyGame.fx.util.DiceUtils;
import com.krchvl.MonopolyGame.fx.util.ImageCache;
import com.krchvl.MonopolyGame.fx.util.MoneyUtils;
import com.krchvl.MonopolyGame.fx.view.BoardView;
import com.krchvl.MonopolyGame.fx.view.TileContextMenu;
import com.krchvl.MonopolyGame.fx.setup.NewGameDialog;
import com.krchvl.MonopolyGame.fx.setup.NewGameSettings;
import com.krchvl.MonopolyGame.fx.setup.NewGameSettings.PlayerCfg;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static com.krchvl.MonopolyGame.fx.theme.Theme.GROUP_COLORS;
import static com.krchvl.MonopolyGame.fx.theme.Theme.PLAYER_COLORS;
import static com.krchvl.MonopolyGame.fx.util.DiceUtils.dieChar;

public class GameController {

    @FXML
    private StackPane boardRoot;
    @FXML
    private Pane boardPane;

    @FXML
    private TextArea logArea;
    @FXML
    private Label playersLabel, currentLabel, phaseLabel;
    @FXML
    private Button rollBtn, buyBtn, passBtn;

    private EventBus bus;
    private GameEngine engine;
    private Board board;

    private BoardView boardView;
    private final ImageCache imageCache = new ImageCache();

    private MovementAnimator moveAnim;
    private DiceOverlay diceOverlay;

    private boolean autoEndScheduled = false;
    private final Map<Player, Integer> visualPos = new HashMap<>();
    private final Map<Integer, PlayerGroup> ownedByIndex = new HashMap<>();
    private ContextMenu monopolyMenu;

    @FXML
    public void initialize() {
        // Листенеры размеров — можно повесить заранее
        boardPane.widthProperty().addListener((o, ov, nv) -> Platform.runLater(this::refreshBoardLayers));
        boardPane.heightProperty().addListener((o, ov, nv) -> Platform.runLater(this::refreshBoardLayers));

        setButtonsState(false, false, false);
        updateHeader();

        // Показать диалог новой игры после инициализации UI
        Platform.runLater(() -> startNewGameWithDialog(true));
    }

    private void startNewGameWithDialog(boolean firstLaunch) {
        Board baseBoard = DefaultBoards.sampleClassicLikeBoard();
        List<PlayerCfg> defaults = Arrays.asList(
                new PlayerCfg("Игрок 1", PlayerGroup.RED, false, 0),
                new PlayerCfg("Bot JOHN", PlayerGroup.GREEN, true, 200),
                new PlayerCfg("Bot BON", PlayerGroup.LIGHT_BLUE, true, 200)
        );

        NewGameDialog dlg = new NewGameDialog(baseBoard, defaults);
        var owner = boardRoot != null && boardRoot.getScene() != null ? boardRoot.getScene().getWindow() : null;
        var res = dlg.show(owner);

        if (res.isEmpty()) {
            if (firstLaunch) Platform.exit();
            return;
        }
        startGame(res.get());
    }

    private void startGame(NewGameSettings settings) {
        // Остановка старых анимаций/меню (если были)
        if (moveAnim != null && moveAnim.isRunning()) moveAnim.stop();
        if (diceOverlay != null && diceOverlay.isRunning()) diceOverlay.stop();
        if (monopolyMenu != null) monopolyMenu.hide();

        // Сброс UI
        logArea.clear();
        ownedByIndex.clear();
        visualPos.clear();
        autoEndScheduled = false;

        // Новые объекты игры
        bus = new EventBus();
        board = DefaultBoards.sampleClassicLikeBoard();

        // Применяем изменения цен и стартовых звёзд ДО старта игры
        settings.getPriceOverrides().forEach((idx, price) -> {
            Tile t = board.getTile(idx);
            if (t instanceof CompanyTile ct) {
                ct.setPrice(price); // требуется сеттер в CompanyTile
            }
        });
        settings.getInitialStars().forEach((idx, stars) -> {
            Tile t = board.getTile(idx);
            if (t instanceof CompanyTile ct) {
                ct.setStars(stars); // безопасно до старта
            }
        });

        // Игроки и контроллеры
        List<Player> players = new ArrayList<>();
        Map<Player, PlayerController> controllers = new HashMap<>();
        for (PlayerCfg pc : settings.getPlayers()) {
            Player p = new Player(pc.name, pc.color);
            players.add(p);
            controllers.put(p, pc.bot ? new BotController(pc.botReserve) : new HumanController());
            visualPos.put(p, p.getPosition());
        }

        engine = new GameEngine(board, players, controllers, bus);

        // BoardView и оверлеи
        boardView = new BoardView(board, imageCache, PLAYER_COLORS, GROUP_COLORS, this::onTileClicked);
        boardView.bindTo(boardPane);

        refreshBoardLayers();
        renderTokens();

        moveAnim = new MovementAnimator(
                board.size(),
                (player, pos) -> visualPos.put(player, pos),
                this::renderTokens,
                idx -> boardView.addLandingEffect(idx)
        );
        diceOverlay = new DiceOverlay(boardRoot, DiceUtils::parseDiceValues, v -> dieChar(v));

        // Подписка на события
        subscribeEvents();

        updateHeader();
        setButtonsState(false, false, false);

        Platform.runLater(engine::start);
    }

    private void refreshBoardLayers() {
        if (boardView == null) return;
        renderStarsOverlays();
        renderOwnershipOverlays();
        renderTokens();
    }

    private void subscribeEvents() {
        bus.on(DiceRolled.class, e -> Platform.runLater(() -> {
            diceOverlay.show(e.roll);
            appendLog(e.player.getName() + " выбросил: " + e.roll + " очков");
            updatePhaseAndButtons();
        }));

        bus.on(CompanyUpgraded.class, e -> Platform.runLater(() -> {
            appendLog(String.format("⭐ %s улучшает %s до уровня %d за %s",
                    e.player.getName(), e.tile.getName(), e.newLevel, MoneyUtils.formatMoney(e.cost)));
            updatePlayersLabel();
            int idx = indexOfTile(e.tile);
            if (idx >= 0) boardView.applyStars(idx, e.newLevel);
            updatePhaseAndButtons();
        }));

        bus.on(CompanyDowngraded.class, e -> Platform.runLater(() -> {
            appendLog(String.format("🔻 %s понижает %s до уровня %d и получает %s",
                    e.player.getName(), e.tile.getName(), e.newLevel, MoneyUtils.formatMoney(e.refund)));
            updatePlayersLabel();
            int idx = indexOfTile(e.tile);
            if (idx >= 0) boardView.applyStars(idx, e.newLevel);
            updatePhaseAndButtons();
        }));

        bus.on(LogEvent.class, e -> {
            appendLog(e.message);
            updatePhaseAndButtons();
        });

        bus.on(TurnStarted.class, e -> Platform.runLater(() -> {
            autoEndScheduled = false;
            updateHeader();
            renderTokens();
            updatePhaseAndButtons();
        }));

        bus.on(PlayerMoved.class, e -> Platform.runLater(() -> moveAnim.animate(e.player, e.from, e.to)));

        bus.on(LandedOnTile.class, e -> {
            appendLog(e.player.getName() + " на клетке: " + e.tile.getName());
            updatePhaseAndButtons();
        });

        bus.on(PurchaseOffered.class, e -> Platform.runLater(() -> {
            appendLog("Предложение покупки: " + e.company.getName() + " за " + MoneyUtils.formatMoney(e.company.getPrice()));
            updatePhaseAndButtons();
        }));

        bus.on(CompanyBought.class, e -> Platform.runLater(() -> {
            appendLog("\uD83D\uDCB0 " + e.buyer.getName() + " купил " + e.company.getName() + " за " + MoneyUtils.formatMoney(e.price));
            updatePlayersLabel();
            int idx = indexOfTile(e.company);
            if (idx >= 0 && e.buyer != null) {
                ownedByIndex.put(idx, e.buyer.getColor());
                boardView.applyOwnership(idx, e.buyer.getColor());
            }
            renderStarsOverlays();
            updatePhaseAndButtons();
        }));

        bus.on(RentPaid.class, e -> Platform.runLater(() -> {
            appendLog(String.format("\uD83D\uDCB8 %s платит аренду %s игроку %s (%s)",
                    e.from.getName(), MoneyUtils.formatMoney(e.amount), e.to.getName(), e.subject));
            updatePlayersLabel();
            updatePhaseAndButtons();
        }));

        bus.on(TaxPaid.class, e -> Platform.runLater(() -> {
            appendLog("\uD83D\uDCB8 " + e.player.getName() + " платит налог " + MoneyUtils.formatMoney(e.amount));
            updatePlayersLabel();
            updatePhaseAndButtons();
        }));

        bus.on(SentToJail.class, e -> Platform.runLater(() -> {
            appendLog(e.player.getName() + " отправлен в тюрьму");
            updatePlayersLabel();
            visualPos.put(e.player, e.player.getPosition());
            renderTokens();
            updatePhaseAndButtons();
        }));

        bus.on(GameOver.class, e -> Platform.runLater(() -> {
            appendLog("Игра окончена. Победитель: " + e.winner.getName());
            setButtonsState(false, false, false);
            showGameOverDialog(e.winner);
        }));
    }

    private void updatePhaseAndButtons() {
        Platform.runLater(() -> {
            if (engine == null) return;
            phaseLabel.setText(engine.getPhase().name());

            if (engine.isCurrentHuman()) {
                GameEngine.Phase phase = engine.getPhase();
                boolean canRoll = phase == GameEngine.Phase.AWAITING_ROLL ||
                        phase == GameEngine.Phase.AWAITING_ROLL_AGAIN;
                boolean canBuy = phase == GameEngine.Phase.AWAITING_BUY_DECISION;
                boolean canEnd = phase == GameEngine.Phase.AWAITING_END_TURN;

                setButtonsState(canRoll, canBuy, canBuy);
                if (!canRoll && !canBuy && canEnd) {
                    maybeAutoEndTurn();
                }
            } else {
                setButtonsState(false, false, false);
            }
        });
    }

    private void renderTokens() {
        if (engine == null) return;
        if (moveAnim == null || !moveAnim.isRunning()) {
            engine.getPlayers().forEach(p -> visualPos.put(p, p.getPosition()));
        }
        boardView.renderTokens(visualPos, engine.getPlayers());
    }

    private void renderStarsOverlays() {
        if (board == null || boardView == null) return;
        for (int i = 0; i < board.size(); i++) {
            Tile t = board.getTile(i);
            if (t instanceof CompanyTile ct) {
                boardView.applyStars(i, ct.getStars());
            } else {
                boardView.clearStars(i);
            }
        }
    }

    private void renderOwnershipOverlays() {
        if (boardView == null) return;
        ownedByIndex.forEach(boardView::applyOwnership);
    }

    private void onTileClicked(int index) {
        if (engine == null) return;
        Tile tile = board.getTile(index);
        if (!(tile instanceof CompanyTile companyTile)) return;

        if (monopolyMenu != null) monopolyMenu.hide();

        monopolyMenu = new TileContextMenu(engine).build(companyTile);
        monopolyMenu.setAutoHide(true);
        monopolyMenu.setOnHidden(e -> monopolyMenu = null);

        Node tileNode = boardView.tilePaneAt(index);
        Node anchor = (tileNode != null && tileNode.getParent() != null) ? tileNode.getParent() : boardPane;

        Platform.runLater(() -> {
            if (anchor.getScene() == null) return;
            var bounds = anchor.localToScreen(anchor.getBoundsInLocal());
            if (bounds != null) {
                double x = bounds.getMaxX() + 6;
                double y = bounds.getMinY();
                monopolyMenu.show(boardPane.getScene().getWindow(), x, y);
            }
        });
    }

    private int indexOfTile(Tile t) {
        if (t == null || board == null) return -1;
        for (int i = 0; i < board.size(); i++) {
            if (board.getTile(i).equals(t)) return i;
        }
        return -1;
    }

    private void updateHeader() {
        updatePlayersLabel();
        currentLabel.setText(engine != null ? engine.current().getName() : "-");
        phaseLabel.setText(engine != null ? engine.getPhase().name() : "-");
    }

    private void updatePlayersLabel() {
        if (engine == null) return;
        StringBuilder sb = new StringBuilder();
        List<Player> players = engine.getPlayers();
        for (int i = 0; i < players.size(); i++) {
            Player p = players.get(i);
            String marker = (i == engine.getCurrentIndex()) ? "▶ " : "";
            sb.append(marker).append(p.getName()).append(" ").append(MoneyUtils.formatMoney(p.getBalance()));
            if (p.isInJail()) sb.append(" [В ТЮРЬМЕ]");
            if (i < players.size() - 1) sb.append("  |  ");
        }
        playersLabel.setText(sb.toString());
    }

    private void appendLog(String text) {
        Platform.runLater(() -> logArea.appendText(text + "\n"));
    }

    private void setButtonsState(boolean roll, boolean buy, boolean pass) {
        Platform.runLater(() -> {
            rollBtn.setDisable(!roll);
            buyBtn.setDisable(!buy);
            passBtn.setDisable(!pass);
        });
    }

    private void maybeAutoEndTurn() {
        if (engine == null || !engine.isCurrentHuman() || engine.getPhase() != GameEngine.Phase.AWAITING_END_TURN || autoEndScheduled) {
            return;
        }
        autoEndScheduled = true;
        runAfterAnimations(() -> {
            if (engine != null && engine.isCurrentHuman() && engine.getPhase() == GameEngine.Phase.AWAITING_END_TURN) {
                engine.endTurn();
            }
            autoEndScheduled = false;
        });
    }

    private void runAfterAnimations(Runnable action) {
        boolean isDiceRunning = diceOverlay != null && diceOverlay.isRunning();
        boolean isMoveRunning = moveAnim != null && moveAnim.isRunning();

        if (!isDiceRunning && !isMoveRunning) {
            PauseTransition pause = new PauseTransition(Duration.millis(250));
            pause.setOnFinished(e -> action.run());
            pause.play();
            return;
        }

        int waitCount = (isDiceRunning ? 1 : 0) + (isMoveRunning ? 1 : 0);
        AtomicInteger remainingWaits = new AtomicInteger(waitCount);

        Runnable onAnimationFinish = () -> {
            if (remainingWaits.decrementAndGet() == 0) {
                PauseTransition finalPause = new PauseTransition(Duration.millis(150));
                finalPause.setOnFinished(e -> action.run());
                finalPause.play();
            }
        };

        if (isDiceRunning) {
            PauseTransition wait = new PauseTransition(Duration.millis(20));
            wait.setOnFinished(e -> onAnimationFinish.run());
            wait.play();
        }
        if (isMoveRunning) {
            PauseTransition wait = new PauseTransition(Duration.millis(20));
            wait.setOnFinished(e -> onAnimationFinish.run());
            wait.play();
        }
    }

    @FXML
    private void onRollDice() {
        if (engine != null) engine.roll();
        setButtonsState(false, false, false);
    }

    @FXML
    private void onBuyProperty() {
        if (engine != null) engine.buy();
        setButtonsState(false, false, false);
    }

    @FXML
    private void onPassPurchase() {
        if (engine != null) engine.pass();
        setButtonsState(false, false, false);
    }

    @FXML
    private void onRestart() {
        startNewGameWithDialog(false);
    }

    @FXML
    private void onExit() {
        Platform.exit();
    }

    private void showGameOverDialog(Player winner) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Игра окончена!");
        alert.setHeaderText("Победитель: " + winner.getName());
        alert.setContentText("Баланс: " + MoneyUtils.formatMoney(winner.getBalance()));
        alert.showAndWait();
    }
}