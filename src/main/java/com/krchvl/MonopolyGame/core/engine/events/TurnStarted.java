package com.krchvl.MonopolyGame.core.engine.events;

import com.krchvl.MonopolyGame.core.Player;

public final class TurnStarted {
    public final Player player;
    public final int round;

    public TurnStarted(Player player, int round) {
        this.player = player;
        this.round = round;
    }
}