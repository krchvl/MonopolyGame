package com.krchvl.MonopolyGame.core.engine.events;

import com.krchvl.MonopolyGame.core.Player;
import com.krchvl.MonopolyGame.core.tiles.CompanyTile;

public class CompanyUpgraded {
    public final Player player;
    public final CompanyTile tile;
    public final int newLevel;
    public final int cost;

    public CompanyUpgraded(Player player, CompanyTile tile, int newLevel, int cost) {
        this.player = player;
        this.tile = tile;
        this.newLevel = newLevel;
        this.cost = cost;
    }
}
