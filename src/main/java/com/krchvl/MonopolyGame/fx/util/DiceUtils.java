package com.krchvl.MonopolyGame.fx.util;

import com.krchvl.MonopolyGame.core.DiceRoll;

import java.util.*;

public class DiceUtils {
    public DiceUtils() {}

    private static final Map<Integer, String> UNICODE_DICE_VALUES = Map.of(
            1, "⚀",
            2, "⚁",
            3, "⚂",
            4, "⚃",
            5, "⚄",
            6, "⚅"
    );

    public static String dieChar(int value) {
        return UNICODE_DICE_VALUES.get((Math.max(1, Math.min(6, value))));
    }

    public static List<Integer> parseDiceValues(Object rollObj) {
        List<Integer> dice = new ArrayList<>(2);
        if (rollObj == null) return dice;

        DiceRoll dr = (DiceRoll) rollObj;
        int d1 = dr.d1();
        int d2 = dr.d2();
        if (isDie(d1) && isDie(d2)) {
            dice.add(d1);
            dice.add(d2);
        }
        return dice;
    }

    private static boolean isDie(int v) {
        return v >= 1 && v <= 6;
    }
}
