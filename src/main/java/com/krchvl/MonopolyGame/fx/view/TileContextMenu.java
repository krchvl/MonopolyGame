package com.krchvl.MonopolyGame.fx.view;

import com.krchvl.MonopolyGame.core.engine.GameEngine;
import com.krchvl.MonopolyGame.core.tiles.CompanyTile;
import com.krchvl.MonopolyGame.fx.util.MoneyUtils;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

public final class TileContextMenu {
    private final GameEngine engine;

    public TileContextMenu(GameEngine engine) {
        this.engine = engine;
    }

    public ContextMenu build(CompanyTile tile) {
        ContextMenu menu = new ContextMenu();
        VBox box = new VBox(8);
        box.setStyle("-fx-padding: 10; -fx-background-color: transparent;");

        Label title = new Label(tile.getName());
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        Label groupLbl = new Label("Группа: " + tile.getGroup().getGroupName());
        Label starsLbl = new Label("Уровень: " + tile.getStars());
        Label costLbl = new Label("Базовая цена: " + MoneyUtils.formatMoney(tile.getPrice()));
        Label rentLbl = new Label("Базовая рента: " + MoneyUtils.formatMoney(tile.getBaseRent()));

        box.getChildren().addAll(title, groupLbl, starsLbl, costLbl, rentLbl);

        boolean isCurrentHuman = engine.isCurrentHuman();
        boolean showUpgrade   = isCurrentHuman && engine.canUpgradeTile(engine.current(), tile);
        boolean showDowngrade = isCurrentHuman && engine.canDowngradeTile(engine.current(), tile);

        if (showUpgrade) {
            int upgradeCost = Math.max(0, engine.calcTileUpgradeCost(tile));
            if (upgradeCost > 0) {
                Button btnUpgrade = new Button("Улучшить за " + MoneyUtils.formatMoney(upgradeCost));
                btnUpgrade.setDefaultButton(true);
                btnUpgrade.getStyleClass().add("monopoly-menu__button--accept");
                btnUpgrade.setOnAction(ev -> {
                    engine.upgradeTile(tile);
                    menu.hide();
                });
                box.getChildren().add(btnUpgrade);
            }
        }

        if (showDowngrade) {
            int refund = Math.max(0, engine.calcTileDowngradeRefund(tile));
            if (refund > 0) {
                Button btnDowngrade = new Button("Продать 1⭐ за " + MoneyUtils.formatMoney(refund));
                btnDowngrade.getStyleClass().add("monopoly-menu__button--decline");
                btnDowngrade.setOnAction(ev -> {
                    engine.downgradeTile(tile);
                    menu.hide();
                });
                box.getChildren().add(btnDowngrade);
            }
        }

        CustomMenuItem cmi = new CustomMenuItem(box, false);
        menu.getItems().add(cmi);
        menu.getStyleClass().add("monopoly-menu");
        return menu;
    }
}