package com.krchvl.MonopolyGame.core.engine.events;

import com.krchvl.MonopolyGame.core.Player;
import com.krchvl.MonopolyGame.core.tiles.Tile;

public final class LandedOnTile {
    public final Player player;
    public final int index;
    public final Tile tile;

    public LandedOnTile(Player player, int index, Tile tile) {
        this.player = player;
        this.index = index;
        this.tile = tile;
    }
}