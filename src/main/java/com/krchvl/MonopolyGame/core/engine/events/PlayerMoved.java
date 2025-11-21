package com.krchvl.MonopolyGame.core.engine.events;

import com.krchvl.MonopolyGame.core.Player;

public final class PlayerMoved {
    public final Player player;
    public final int from;
    public final int to;

    public PlayerMoved(Player player, int from, int to) {
        this.player = player;
        this.from = from;
        this.to = to;
    }
}