package com.krchvl.MonopolyGame.core.tiles;

import com.krchvl.MonopolyGame.core.DiceRoll;
import com.krchvl.MonopolyGame.core.Player;
import com.krchvl.MonopolyGame.core.engine.GameContext;

public class UtilityTile extends CompanyTile {
    public UtilityTile(String name, String imgSrc, int price) {
        super(name, imgSrc, price, 0, CompanyTileGroup.BROWN);
    }

    @Override
    protected int calculateRent(GameContext game, DiceRoll lastRoll) {
        Player owner = getOwner();
        if (owner == null || lastRoll == null) return 0;
        long owned = game.getBoard().countOwnedInGroup(owner, CompanyTileGroup.BROWN);
        int multiplier = (owned >= 2) ? 10 : 5;
        return multiplier * lastRoll.sum();
    }
}