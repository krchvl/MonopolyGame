package com.krchvl.MonopolyGame.fx.view;

import com.krchvl.MonopolyGame.core.Board;
import com.krchvl.MonopolyGame.core.Player;
import com.krchvl.MonopolyGame.core.PlayerGroup;
import com.krchvl.MonopolyGame.core.tiles.CompanyTile;
import com.krchvl.MonopolyGame.core.tiles.CompanyTileGroup;
import com.krchvl.MonopolyGame.core.tiles.Tile;
import com.krchvl.MonopolyGame.fx.util.ImageCache;
import com.krchvl.MonopolyGame.fx.util.ImageUtils;
import javafx.animation.ScaleTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.util.*;
import java.util.function.Consumer;

public final class BoardView {

    private final Board board;
    private final ImageCache imageCache;
    private final Map<PlayerGroup, String> playerColors;
    private final Map<CompanyTileGroup, String> groupColors;
    private final Consumer<Integer> onTileClick;

    private final Pane root = new Pane();
    private Group tilesGroup;

    private enum TokenAnchor { TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT, LEFT, RIGHT, TOP, BOTTOM }
    private static final double TOKEN_DIAMETER = 20.0;
    private static final double TOKEN_GAP = 4.0;
    private static final double TOKEN_MARGIN = 6.0;
    private static final TokenAnchor TOKEN_ANCHOR = TokenAnchor.BOTTOM_RIGHT;

    private static final String STYLE_TILE =
            "-fx-background-color: transparent;" +
                    "-fx-border-color: #4b4b4b;" +
                    "-fx-border-width: 1;" +
                    "-fx-background-radius: 8;" +
                    "-fx-cursor: hand;" +
                    "-fx-border-radius: 8;";

    public BoardView(Board board,
                     ImageCache imageCache,
                     Map<PlayerGroup, String> playerColors,
                     Map<CompanyTileGroup, String> groupColors,
                     Consumer<Integer> onTileClick) {
        this.board = Objects.requireNonNull(board);
        this.imageCache = Objects.requireNonNull(imageCache);
        this.playerColors = Objects.requireNonNull(playerColors);
        this.groupColors = Objects.requireNonNull(groupColors);
        this.onTileClick = onTileClick;
        root.setPickOnBounds(true);
    }

    public void layout(double w, double h) {
        if (w <= 2 || h <= 2) return;
        root.getChildren().clear();

        int n = board.size();
        if (n <= 0) return;

        double padding = 20;
        double boardSize = Math.min(w, h) - 2 * padding;

        int totalSides = 4;
        int cornerTiles = 4;
        int regularTiles = n - cornerTiles;
        int tilesPerSide = Math.max(1, regularTiles / totalSides);

        double tileSize = boardSize / (tilesPerSide + 2);
        double left = (w - boardSize) / 2.0;
        double top = (h - boardSize) / 2.0;

        tilesGroup = new Group();
        int idx = 0;

        addTileAt(idx++, left + boardSize - tileSize, top + boardSize - tileSize, tileSize, tileSize);

        for (int j = 0; j < tilesPerSide && idx < n; j++) {
            double x = left + boardSize - tileSize - (j + 1) * tileSize;
            double y = top + boardSize - tileSize;
            addTileAt(idx++, x, y, tileSize, tileSize);
        }

        if (idx < n) addTileAt(idx++, left, top + boardSize - tileSize, tileSize, tileSize);

        for (int j = 0; j < tilesPerSide && idx < n; j++) {
            double x = left;
            double y = top + boardSize - tileSize - (j + 1) * tileSize;
            addTileAt(idx++, x, y, tileSize, tileSize);
        }

        if (idx < n) addTileAt(idx++, left, top, tileSize, tileSize);

        for (int j = 0; j < tilesPerSide && idx < n; j++) {
            double x = left + tileSize + j * tileSize;
            double y = top;
            addTileAt(idx++, x, y, tileSize, tileSize);
        }

        if (idx < n) addTileAt(idx++, left + boardSize - tileSize, top, tileSize, tileSize);

        for (int j = 0; j < tilesPerSide && idx < n; j++) {
            double x = left + boardSize - tileSize;
            double y = top + tileSize + j * tileSize;
            addTileAt(idx++, x, y, tileSize, tileSize);
        }

        root.getChildren().add(tilesGroup);
    }

    public void bindTo(Pane container) {
        container.getChildren().setAll(root);
        root.prefWidthProperty().bind(container.widthProperty());
        root.prefHeightProperty().bind(container.heightProperty());
        container.widthProperty().addListener((obs, ov, nv) -> layout());
        container.heightProperty().addListener((obs, ov, nv) -> layout());
        layout();
    }

    public void layout() {
        double w = root.getWidth();
        double h = root.getHeight();

        if ((w <= 2 || h <= 2) && root.getParent() instanceof Region parent) {
            w = parent.getWidth();
            h = parent.getHeight();
        }

        if (w <= 2 || h <= 2) {
            w = Math.max(w, root.getPrefWidth());
            h = Math.max(h, root.getPrefHeight());
        }

        if (w > 2 && h > 2) {
            layout(w, h);
        }
    }

    public void applyOwnership(int index, PlayerGroup group) {
        StackPane tilePane = tilePaneAt(index);
        if (tilePane == null || group == null) return;

        Pane layer = getOrCreateOwnerLayer(tilePane);
        layer.getProperties().put("group", group);
        refreshOwnerLayer(tilePane, layer, index, group);

        findTokensLayer(tilePane).ifPresent(Node::toFront);
    }

    public void applyStars(int index, int stars) {
        StackPane tilePane = tilePaneAt(index);
        if (tilePane == null) return;

        Optional<Label> badgeOpt = findStarsBadge(tilePane);
        if (stars <= 0) {
            badgeOpt.ifPresent(badge -> tilePane.getChildren().remove(badge));
            return;
        }

        String text = "★×" + stars;
        Label badge = badgeOpt.orElseGet(() -> {
            Label newBadge = new Label(text);
            newBadge.setUserData("stars-badge");
            newBadge.setMouseTransparent(true);
            newBadge.setStyle("-fx-background-color: rgba(0,0,0,0.65);" +
                    "-fx-text-fill: gold;" +
                    "-fx-font-weight: bold;" +
                    "-fx-font-size: 11px;" +
                    "-fx-padding: 2 4 2 4;" +
                    "-fx-background-radius: 6;");
            tilePane.getChildren().add(newBadge);
            StackPane.setAlignment(newBadge, Pos.TOP_LEFT);
            StackPane.setMargin(newBadge, new Insets(4, 0, 0, 4));
            return newBadge;
        });
        badge.setText(text);
        badge.toFront();
    }

    public void clearStars(int index) {
        StackPane tilePane = tilePaneAt(index);
        if (tilePane == null) return;
        findStarsBadge(tilePane).ifPresent(badge -> tilePane.getChildren().remove(badge));
    }

    public void renderTokens(Map<Player, Integer> visualPos, List<Player> orderedPlayers) {
        if (tilesGroup == null || board == null) return;

        tilesGroup.getChildren().stream()
                .filter(Group.class::isInstance)
                .map(Group.class::cast)
                .map(g -> g.getChildren().get(0))
                .filter(StackPane.class::isInstance)
                .map(StackPane.class::cast)
                .forEach(sp -> findTokensLayer(sp).ifPresent(layer -> layer.getChildren().clear()));

        Map<Integer, List<Player>> byPos = new HashMap<>();
        for (Player p : orderedPlayers) {
            byPos.computeIfAbsent(visualPos.getOrDefault(p, p.getPosition()), k -> new ArrayList<>()).add(p);
        }

        for (Map.Entry<Integer, List<Player>> entry : byPos.entrySet()) {
            int index = entry.getKey();
            StackPane tilePane = tilePaneAt(index);
            if (tilePane == null) continue;

            Pane layer = getOrCreateTokensLayer(tilePane);
            List<Player> playersOnTile = entry.getValue();

            double w = tilePane.getWidth() > 0 ? tilePane.getWidth() : tilePane.getPrefWidth();
            double h = tilePane.getHeight() > 0 ? tilePane.getHeight() : tilePane.getPrefHeight();
            if (w <= 0 || h <= 0) {
                tilePane.layoutBoundsProperty().addListener((obs, o, nb) -> renderTokens(visualPos, orderedPlayers));
                continue;
            }

            boolean hasBar = board.getTile(index) instanceof CompanyTile;
            double barH = hasBar ? Math.max(10, Math.min(16, h * 0.18)) : 0.0;
            double padding = 4.0;
            double innerX = padding;
            double innerY = padding + barH;
            double innerW = Math.max(0, w - 2 * padding);
            double innerH = Math.max(0, h - 2 * padding - barH);
            int count = playersOnTile.size();

            int colsMax = Math.max(1, (int) Math.floor((innerW + TOKEN_GAP) / (TOKEN_DIAMETER + TOKEN_GAP)));
            int rowsMax = Math.max(1, (int) Math.floor((innerH + TOKEN_GAP) / (TOKEN_DIAMETER + TOKEN_GAP)));
            int cols = Math.min(colsMax, Math.max(1, count));
            int rows = (int) Math.ceil((double) count / cols);
            if (rows > rowsMax) {
                cols = Math.min(colsMax, (int) Math.ceil((double) count / rowsMax));
                rows = (int) Math.ceil((double) count / cols);
            }

            double gridW = cols * TOKEN_DIAMETER + (cols - 1) * TOKEN_GAP;
            double gridH = rows * TOKEN_DIAMETER + (rows - 1) * TOKEN_GAP;

            double startX, startY;
            switch (TOKEN_ANCHOR) {
                case TOP_LEFT -> { startX = innerX + TOKEN_MARGIN; startY = innerY + TOKEN_MARGIN; }
                case TOP_RIGHT -> { startX = innerX + Math.max(0, innerW - gridW - TOKEN_MARGIN); startY = innerY + TOKEN_MARGIN; }
                case BOTTOM_LEFT -> { startX = innerX + TOKEN_MARGIN; startY = innerY + Math.max(0, innerH - gridH - TOKEN_MARGIN); }
                case BOTTOM_RIGHT -> { startX = innerX + Math.max(0, innerW - gridW - TOKEN_MARGIN); startY = innerY + Math.max(0, innerH - gridH - TOKEN_MARGIN); }
                case LEFT -> { startX = innerX + TOKEN_MARGIN; startY = innerY + Math.max(0, (innerH - gridH) / 2.0); }
                case RIGHT -> { startX = innerX + Math.max(0, innerW - gridW - TOKEN_MARGIN); startY = innerY + Math.max(0, (innerH - gridH) / 2.0); }
                case TOP -> { startX = innerX + Math.max(0, (innerW - gridW) / 2.0); startY = innerY + TOKEN_MARGIN; }
                case BOTTOM -> { startX = innerX + Math.max(0, (innerW - gridW) / 2.0); startY = innerY + Math.max(0, innerH - gridH - TOKEN_MARGIN); }
                default -> {
                    startX = innerX + Math.max(0, (innerW - gridW) / 2.0);
                    startY = innerY + Math.max(0, (innerH - gridH) / 2.0);
                }
            }

            for (int i = 0; i < count; i++) {
                Player p = playersOnTile.get(i);
                int r = i / cols;
                int c = i % cols;
                double x = startX + c * (TOKEN_DIAMETER + TOKEN_GAP);
                double y = startY + r * (TOKEN_DIAMETER + TOKEN_GAP);

                Circle token = makeTokenNode(p, TOKEN_DIAMETER);
                token.setLayoutX(x + TOKEN_DIAMETER / 2.0);
                token.setLayoutY(y + TOKEN_DIAMETER / 2.0);
                layer.getChildren().add(token);
            }
        }
    }

    public void addLandingEffect(int index) {
        findTileGroup(index).ifPresent(g -> {
            ScaleTransition pulse = new ScaleTransition(Duration.millis(150), g);
            pulse.setFromX(1.0);
            pulse.setFromY(1.0);
            pulse.setToX(1.15);
            pulse.setToY(1.15);
            pulse.setCycleCount(2);
            pulse.setAutoReverse(true);
            pulse.play();
        });
    }

    private void addTileAt(int index, double x, double y, double w, double h) {
        if (index >= board.size()) return;
        Tile t = board.getTile(index);
        StackPane tile = makeTileNode(t, w, h);
        tile.setOnMouseClicked(ev -> { if (onTileClick != null) onTileClick.accept(index); });

        Group g = new Group(tile);
        tile.setLayoutX(-w / 2.0);
        tile.setLayoutY(-h / 2.0);
        g.setLayoutX(x + w / 2.0);
        g.setLayoutY(y + h / 2.0);
        g.setUserData(index);
        tilesGroup.getChildren().add(g);
    }

    private StackPane makeTileNode(Tile tile, double w, double h) {
        StackPane tilePane = new StackPane();
        tilePane.setPrefSize(w, h);
        tilePane.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        tilePane.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

        Pane contentPane = new Pane();
        contentPane.setPrefSize(w, h);
        contentPane.setStyle(STYLE_TILE);

        Rectangle clip = new Rectangle(w, h);
        clip.setArcWidth(16);
        clip.setArcHeight(16);
        contentPane.setClip(clip);

        buildTileBackground(tile, w, h).ifPresent(bg -> contentPane.getChildren().add(bg));

        if (tile instanceof CompanyTile companyTile) {
            String color = groupColors.getOrDefault(companyTile.getGroup(), "#666666");
            double barH = Math.max(10, Math.min(16, h * 0.18));
            Pane bar = new Pane();
            bar.setPrefSize(w, barH);
            bar.setStyle("-fx-background-color: " + color + ";" +
                    "-fx-background-radius: 6 6 0 0;" +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.30), 2, 0, 0, 1);");

            if (companyTile.getPrice() > 0) {
                Label priceLabel = new Label("$" + companyTile.getPrice());
                double fontSize = Math.max(9, Math.min(12, h * 0.14));
                priceLabel.setStyle("-fx-font-size: " + fontSize + "px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: white;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.45), 2, 0, 0, 1);");
                priceLabel.layoutBoundsProperty().addListener((obs, oldB, newB) -> {
                    priceLabel.setLayoutX((w - newB.getWidth()) / 2);
                    priceLabel.setLayoutY((barH - newB.getHeight()) / 2);
                });
                bar.getChildren().add(priceLabel);
            }
            bar.setMouseTransparent(true);
            contentPane.getChildren().add(bar);
        }

        tilePane.getChildren().add(contentPane);
        return tilePane;
    }

    private Optional<ImageView> buildTileBackground(Tile tile, double w, double h) {
        String src = ImageUtils.getImgSrc(tile);
        if (src == null || src.isBlank()) return Optional.empty();

        Image img = imageCache.get(src);
        if (img == null || img.isError()) return Optional.empty();

        ImageView iv = new ImageView(img);
        iv.setSmooth(false);
        iv.setCache(true);

        double iw = img.getWidth();
        double ih = img.getHeight();
        if (iw > 0 && ih > 0 && w > 0 && h > 0) {
            double targetRatio = w / h;
            double imgRatio = iw / ih;
            if (imgRatio > targetRatio) {
                double vw = ih * targetRatio;
                iv.setViewport(new Rectangle2D((iw - vw) / 2.0, 0, vw, ih));
            } else {
                double vh = iw / targetRatio;
                iv.setViewport(new Rectangle2D(0, (ih - vh) / 2.0, iw, vh));
            }
        }
        iv.setFitWidth(w);
        iv.setFitHeight(h);
        return Optional.of(iv);
    }

    private Optional<Pane> findOwnerLayer(StackPane tilePane) {
        return tilePane.getChildren().stream()
                .filter(ch -> "owner-layer".equals(ch.getUserData()) && ch instanceof Pane)
                .map(Pane.class::cast)
                .findFirst();
    }

    private Pane getOrCreateOwnerLayer(StackPane tilePane) {
        return findOwnerLayer(tilePane).orElseGet(() -> {
            Pane layer = new Pane();
            layer.setMouseTransparent(true);
            layer.setPickOnBounds(false);
            layer.setUserData("owner-layer");
            tilePane.getChildren().add(layer);
            tilePane.widthProperty().addListener((o, ov, nv) -> refreshOwnerLayer(tilePane, layer));
            tilePane.heightProperty().addListener((o, ov, nv) -> refreshOwnerLayer(tilePane, layer));
            return layer;
        });
    }

    private void refreshOwnerLayer(StackPane tilePane, Pane layer) {
        PlayerGroup g = (PlayerGroup) layer.getProperties().get("group");
        if (g == null) return;
        findTileIndexByPane(tilePane).ifPresent(index -> refreshOwnerLayer(tilePane, layer, index, g));
    }

    private void refreshOwnerLayer(StackPane tilePane, Pane layer, int index, PlayerGroup group) {
        double w = tilePane.getWidth() > 0 ? tilePane.getWidth() : tilePane.getPrefWidth();
        double h = tilePane.getHeight() > 0 ? tilePane.getHeight() : tilePane.getPrefHeight();
        if (w <= 0 || h <= 0) return;

        boolean hasBar = board.getTile(index) instanceof CompanyTile;
        double barH = hasBar ? Math.max(10, Math.min(16, h * 0.18)) : 0.0;

        layer.setLayoutX(0);
        layer.setLayoutY(barH);
        layer.setPrefSize(w, Math.max(0, h - barH));

        String hex = playerColors.getOrDefault(group, "#888888");
        String fill = toRgba(hex, 0.25);
        layer.setStyle(
                "-fx-background-color: " + fill + ";" +
                        "-fx-border-color: " + hex + ";" +
                        "-fx-border-width: 2;" +
                        "-fx-background-radius: 0 0 8 8;" +
                        "-fx-border-radius: 0 0 8 8;");
    }

    private static String toRgba(String hex, double alpha) {
        Color color = Color.web(hex, alpha);
        return String.format(Locale.US, "rgba(%d,%d,%d,%.3f)",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255),
                color.getOpacity());
    }

    private Optional<Label> findStarsBadge(StackPane tilePane) {
        return tilePane.getChildren().stream()
                .filter(ch -> "stars-badge".equals(ch.getUserData()) && ch instanceof Label)
                .map(Label.class::cast)
                .findFirst();
    }

    private Optional<Pane> findTokensLayer(StackPane tilePane) {
        return tilePane.getChildren().stream()
                .filter(ch -> "tokens-layer".equals(ch.getUserData()) && ch instanceof Pane)
                .map(Pane.class::cast)
                .findFirst();
    }

    private Pane getOrCreateTokensLayer(StackPane tilePane) {
        return findTokensLayer(tilePane).orElseGet(() -> {
            Pane layer = new Pane();
            layer.setPickOnBounds(false);
            layer.setMouseTransparent(true);
            layer.setUserData("tokens-layer");
            tilePane.getChildren().add(layer);
            layer.prefWidthProperty().bind(tilePane.widthProperty());
            layer.prefHeightProperty().bind(tilePane.heightProperty());
            return layer;
        });
    }

    private Circle makeTokenNode(Player p, double diameter) {
        Circle c = new Circle(diameter / 2.0);
        c.setFill(Color.web(playerColors.getOrDefault(p.getColor(), "#888888")));
        c.setStroke(Color.BLACK);
        c.setStrokeWidth(Math.max(1, diameter * 0.08));
        c.setEffect(new DropShadow(6, Color.rgb(0, 0, 0, 0.35)));
        c.setMouseTransparent(true);
        return c;
    }

    private Optional<Group> findTileGroup(int index) {
        if (tilesGroup == null) return Optional.empty();
        return tilesGroup.getChildren().stream()
                .filter(n -> Integer.valueOf(index).equals(n.getUserData()) && n instanceof Group)
                .map(Group.class::cast)
                .findFirst();
    }

    public StackPane tilePaneAt(int index) {
        return findTileGroup(index)
                .map(g -> g.getChildren().isEmpty() ? null : g.getChildren().get(0))
                .filter(StackPane.class::isInstance)
                .map(StackPane.class::cast)
                .orElse(null);
    }

    private Optional<Integer> findTileIndexByPane(StackPane pane) {
        if (tilesGroup == null) return Optional.empty();
        return tilesGroup.getChildren().stream()
                .filter(n -> n instanceof Group)
                .map(Group.class::cast)
                .filter(g -> !g.getChildren().isEmpty() && g.getChildren().get(0) == pane)
                .map(g -> (Integer) g.getUserData())
                .findFirst();
    }
}