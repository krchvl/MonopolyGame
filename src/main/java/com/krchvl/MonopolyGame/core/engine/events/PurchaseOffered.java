package com.krchvl.MonopolyGame.core.engine.events;

import com.krchvl.MonopolyGame.core.Player;
import com.krchvl.MonopolyGame.core.tiles.CompanyTile;

public final class PurchaseOffered {
    public final Player player;
    public final CompanyTile company;
    public final int price;

    public PurchaseOffered(Player player, CompanyTile company, int price) {
        this.player = player;
        this.company = company;
        this.price = price;
    }
}