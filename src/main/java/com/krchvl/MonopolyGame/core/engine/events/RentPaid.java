package com.krchvl.MonopolyGame.core.engine.events;

import com.krchvl.MonopolyGame.core.Player;

public final class RentPaid {
    public final Player from;
    public final Player to;
    public final int amount;
    public final String subject;

    public RentPaid(Player from, Player to, int amount, String subject) {
        this.from = from;
        this.to = to;
        this.amount = amount;
        this.subject = subject;
    }
}