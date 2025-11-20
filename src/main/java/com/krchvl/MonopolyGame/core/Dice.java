package com.krchvl.MonopolyGame.core;

import java.util.Random;

public class Dice {
    private final Random rnd = new Random();

    public DiceRoll roll() {
        int d1 = 1 + rnd.nextInt(6);
        int d2 = 1 + rnd.nextInt(6);
        return new DiceRoll(d1, d2);
    }
}