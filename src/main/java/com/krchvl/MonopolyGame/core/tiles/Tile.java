package com.krchvl.MonopolyGame.core.tiles;

import com.krchvl.MonopolyGame.core.DiceRoll;
import com.krchvl.MonopolyGame.core.Player;
import com.krchvl.MonopolyGame.core.engine.GameContext;

public abstract class Tile {
    private final String name;
    private final String imgSrc;

    protected Tile(String name, String imgSrc) {
        this.name = name;
        this.imgSrc = imgSrc;
    }

    public String getName() { return name; }
    public String getImgSrc() { return imgSrc; }

    public abstract void onLand(GameContext game, Player player, DiceRoll lastRoll);
}