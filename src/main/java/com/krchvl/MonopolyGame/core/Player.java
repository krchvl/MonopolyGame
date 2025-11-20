package com.krchvl.MonopolyGame.core;

public class Player {
    private final String name;
    public final PlayerGroup color;
    private int balance = 1500;
    private int position = 0;
    private boolean inJail = false;
    private int jailTurns = 0;
    private boolean bankrupt = false;

    public Player(String name, PlayerGroup color) {
        this.name = name;
        this.color = color;
    }

    public String getName() { return name; }
    public PlayerGroup getColor() { return color; }
    public int getBalance() { return balance; }
    public void receive(int amount) { balance += amount; }
    public void pay(int amount) { balance -= amount; }
    public int getPosition() { return position; }
    public void setPosition(int position) { this.position = position; }
    public boolean isInJail() { return inJail; }
    public void sendToJail() { inJail = true; jailTurns = 0; }
    public void releaseFromJail() { inJail = false; jailTurns = 0; }
    public int getJailTurns() { return jailTurns; }
    public void incrementJailTurns() { jailTurns++; }
    public boolean isBankrupt() { return bankrupt; }
    public void setBankrupt(boolean bankrupt) { this.bankrupt = bankrupt; }
}