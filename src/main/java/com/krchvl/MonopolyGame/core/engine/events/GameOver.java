package com.krchvl.MonopolyGame.core.engine.events;

import com.krchvl.MonopolyGame.core.Player;

public final class GameOver {
    public final Player winner;

    public GameOver(Player winner) {
        this.winner = winner;
    }
}