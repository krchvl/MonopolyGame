package com.krchvl.MonopolyGame.core.engine.events;

import com.krchvl.MonopolyGame.core.Player;
import com.krchvl.MonopolyGame.core.tiles.CompanyTile;

public class CompanyDowngraded {
    public final Player player;
    public final CompanyTile tile;
    public final int newLevel;
    public final int refund;

    public CompanyDowngraded(Player player, CompanyTile tile, int newLevel, int refund) {
        this.player = player;
        this.tile = tile;
        this.newLevel = newLevel;
        this.refund = refund;
    }
}
