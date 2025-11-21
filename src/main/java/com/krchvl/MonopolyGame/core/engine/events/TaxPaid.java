package com.krchvl.MonopolyGame.core.engine.events;

import com.krchvl.MonopolyGame.core.Player;

public final class TaxPaid {
    public final Player player;
    public final int amount;

    public TaxPaid(Player player, int amount) {
        this.player = player;
        this.amount = amount;
    }
}