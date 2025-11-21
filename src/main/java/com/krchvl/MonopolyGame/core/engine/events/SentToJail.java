package com.krchvl.MonopolyGame.core.engine.events;


import com.krchvl.MonopolyGame.core.Player;

public final class SentToJail {
    public final Player player;

    public SentToJail(Player player) {
        this.player = player;
    }
}