package com.krchvl.MonopolyGame.core.engine.events;

import com.krchvl.MonopolyGame.core.Player;
import com.krchvl.MonopolyGame.core.tiles.CompanyTile;

public final class CompanyBought {
    public final Player buyer;
    public final CompanyTile company;
    public final int price;

    public CompanyBought(Player buyer, CompanyTile company, int price) {
        this.buyer = buyer;
        this.company = company;
        this.price = price;
    }
}