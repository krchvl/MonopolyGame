package com.krchvl.MonopolyGame.fx.util;

public class MoneyUtils {
    public MoneyUtils() {}

    public static String formatMoney(int money) {
        return String.format("$%dK", money);
    }
}
