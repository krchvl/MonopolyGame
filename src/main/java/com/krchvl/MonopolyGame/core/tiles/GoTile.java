package com.krchvl.MonopolyGame.core.tiles;

import com.krchvl.MonopolyGame.core.DiceRoll;
import com.krchvl.MonopolyGame.core.Player;
import com.krchvl.MonopolyGame.core.engine.GameContext;

public class GoTile extends Tile {
    public GoTile(String name) { super(name, "/images/start.png"); }

    @Override
    public void onLand(GameContext game, Player player, DiceRoll lastRoll) {
        player.receive(200);
    }
}