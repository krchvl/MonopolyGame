package com.krchvl.MonopolyGame.core.tiles;

import com.krchvl.MonopolyGame.core.Player;

public interface CompanyLike {
    String getName();
    CompanyTileGroup getGroup();
    Player getOwner();
    void setOwner(Player owner);
}