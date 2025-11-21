package com.krchvl.MonopolyGame.core.engine.events;

import com.krchvl.MonopolyGame.core.DiceRoll;
import com.krchvl.MonopolyGame.core.Player;

public final class DiceRolled {
    public final Player player;
    public final DiceRoll roll;

    public DiceRolled(Player player, DiceRoll roll) {
        this.player = player;
        this.roll = roll;
    }
}