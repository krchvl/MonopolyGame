package com.krchvl.MonopolyGame.core.engine;

import com.krchvl.MonopolyGame.core.Board;
import com.krchvl.MonopolyGame.core.Player;
import com.krchvl.MonopolyGame.core.tiles.CompanyTile;

public interface GameContext {
    Board getBoard();
    boolean safePay(Player from, int amount, Player to);
    void sendToJail(Player p);

    void offerPurchase(Player p, CompanyTile property);
    void publish(Object event);
}