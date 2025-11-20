package com.krchvl.MonopolyGame.core;

public class DiceRoll {
    private final int d1;
    private final int d2;

    public DiceRoll(int d1, int d2) {
        this.d1 = d1;
        this.d2 = d2;
    }

    public int sum() {
        return d1 + d2;
    }

    public boolean isDouble() {
        return d1 == d2;
    }

    public int d1() {
        return d1;
    }

    public int d2() {
        return d2;
    }

    @Override
    public String toString() {
        return sum() + " (" + d1 + ":" + d2 + ")" + (isDouble() ? " (дубль)" : "");
    }
}