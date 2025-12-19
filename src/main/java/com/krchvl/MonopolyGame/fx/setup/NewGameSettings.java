package com.krchvl.MonopolyGame.fx.setup;

import com.krchvl.MonopolyGame.core.PlayerGroup;

import java.util.*;

public class NewGameSettings {

    public static class PlayerCfg {
        public String name;
        public PlayerGroup color;
        public boolean bot;
        public int botReserve = 200;

        public PlayerCfg() {}
        public PlayerCfg(String name, PlayerGroup color, boolean bot, int botReserve) {
            this.name = name;
            this.color = color;
            this.bot = bot;
            this.botReserve = botReserve;
        }
    }

    private final List<PlayerCfg> players = new ArrayList<>();

    private final Map<Integer, Integer> priceOverrides = new HashMap<>();

    private final Map<Integer, Integer> initialStars = new HashMap<>();

    public List<PlayerCfg> getPlayers() { return players; }
    public Map<Integer, Integer> getPriceOverrides() { return priceOverrides; }
    public Map<Integer, Integer> getInitialStars() { return initialStars; }
}