package com.krchvl.MonopolyGame.core.tiles;

import com.krchvl.MonopolyGame.core.DiceRoll;
import com.krchvl.MonopolyGame.core.Player;
import com.krchvl.MonopolyGame.core.engine.GameContext;

public class ChanceTile extends Tile {
    public ChanceTile(String name) { super(name, "/images/chance.png"); }

    @Override
    public void onLand(GameContext game, Player player, DiceRoll lastRoll) {
    }
}