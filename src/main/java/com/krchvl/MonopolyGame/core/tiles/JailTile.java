package com.krchvl.MonopolyGame.core.tiles;

import com.krchvl.MonopolyGame.core.DiceRoll;
import com.krchvl.MonopolyGame.core.Player;
import com.krchvl.MonopolyGame.core.engine.GameContext;

public class JailTile extends Tile {
    public JailTile(String name) { super(name, "/images/prison.png"); }

    @Override
    public void onLand(GameContext game, Player player, DiceRoll lastRoll) {
        player.sendToJail();
    }
}