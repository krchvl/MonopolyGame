package com.krchvl.MonopolyGame.core.tiles;

import com.krchvl.MonopolyGame.core.DiceRoll;
import com.krchvl.MonopolyGame.core.Player;
import com.krchvl.MonopolyGame.core.engine.GameContext;

public class TaxTile extends Tile {
    private final int amount;

    public TaxTile(String name, int amount) {
        super(name, "/images/tax.png");
        this.amount = amount;
    }

    @Override
    public void onLand(GameContext game, Player player, DiceRoll lastRoll) {
        game.safePay(player, amount, null);
    }
}