package com.krchvl.MonopolyGame.core.tiles;

public enum CompanyTileGroup {
    RED,
    YELLOW,
    DARK_BLUE,
    LIGHT_BLUE,
    PINK,
    BROWN,
    GREEN,
    ORANGE,
    ;

    public String getGroupName() {
        switch (this) {
            case RED:
                return "";
            case YELLOW:
                return "Нефть";
            case DARK_BLUE:
                return "";
            case LIGHT_BLUE:
                return "Телекоммуникации";
            case PINK:
                return "Магазины";
            case BROWN:
                return "Крупнейшие компании";
            case GREEN:
                return "Банки";
            case ORANGE:
                return "Фастфуд";
            default:
                return "";
        }
    }
}
