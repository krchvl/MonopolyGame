package com.krchvl.MonopolyGame.core.tiles;

import com.krchvl.MonopolyGame.core.Board;

public final class DefaultBoards {
    private DefaultBoards() {}

    public static Board sampleClassicLikeBoard() {
        Board b = new Board();
        b.setGoIndex(0);
        b.setJailIndex(10);

        b.addTile(new GoTile("Старт"));
        b.addTile(new CompanyTile("Альфа Банк", "/images/companies/alfabank.png", 60, 2, CompanyTileGroup.GREEN));
        b.addTile(new CompanyTile("Сбербанк", "/images/companies/sberbank.png", 60, 4, CompanyTileGroup.GREEN));
        b.addTile(new UtilityTile("Газпром", "/images/companies/gasprom.png", 150));
        b.addTile(new CompanyTile("Т-Банк", "/images/companies/tbank.png", 60, 4, CompanyTileGroup.GREEN));
        b.addTile(new CompanyTile("МТС", "/images/companies/mts.png", 100, 6, CompanyTileGroup.LIGHT_BLUE));
        b.addTile(new ChanceTile("Шанс"));
        b.addTile(new CompanyTile("Теле2", "/images/companies/tele2.png", 100, 6, CompanyTileGroup.LIGHT_BLUE));
        b.addTile(new CompanyTile("Мегафон", "/images/companies/megafon.png", 120, 8, CompanyTileGroup.LIGHT_BLUE));
        b.addTile(new CompanyTile("Пятёрочка", "/images/companies/pyaterochka.png", 140, 10, CompanyTileGroup.PINK));
        b.addTile(new UtilityTile("Яндекс", "/images/companies/yandex.png", 150));
        b.addTile(new CompanyTile("Билайн", "/images/companies/beeline.png", 140, 10, CompanyTileGroup.LIGHT_BLUE));
        b.addTile(new JailTile("Тюрьма"));
        b.addTile(new CompanyTile("Дикси", "/images/companies/diksi.png", 140, 12, CompanyTileGroup.PINK));
        b.addTile(new CompanyTile("Перекрёсток", "/images/companies/perekrestok.png", 160, 12, CompanyTileGroup.PINK));
        b.addTile(new CompanyTile("Магнит", "/images/companies/magnit.png", 180, 12, CompanyTileGroup.PINK));
        b.addTile(new CompanyTile("Вкусно - и точка", "/images/companies/mc.png", 160, 14, CompanyTileGroup.ORANGE));
        b.addTile(new CompanyTile("Бургер кинг", "/images/companies/burger king.png", 180, 14, CompanyTileGroup.ORANGE));
        b.addTile(new TaxTile("Подоходный налог", 150));
        b.addTile(new CompanyTile("Лукойл", "/images/companies/lukoil.png", 200, 16, CompanyTileGroup.YELLOW));
        b.addTile(new CompanyTile("Роснефть", "/images/companies/rosneft.png", 200, 16, CompanyTileGroup.YELLOW));
        b.addTile(new CompanyTile("Rostic's", "/images/companies/rostics.png", 180, 14, CompanyTileGroup.ORANGE));
        b.addTile(new JailTile("Тюрьма"));
        b.addTile(new CompanyTile("Перекрёсток", "/images/companies/perekrestok.png", 180, 14, CompanyTileGroup.PINK));
        return b;
    }

    public static Board testStarsBoard() {
        Board b = new Board();
        b.setGoIndex(0);

        b.addTile(new CompanyTile("Сбербанк", "/images/companies/sberbank.png", 60, 4, CompanyTileGroup.GREEN));
        b.addTile(new CompanyTile("Магнит", "/images/companies/magnit.png", 180, 12, CompanyTileGroup.PINK));
        b.addTile(new CompanyTile("Лукойл", "/images/companies/lukoil.png", 200, 16, CompanyTileGroup.YELLOW));
        b.addTile(new CompanyTile("Rostic's", "/images/companies/rostics.png", 180, 14, CompanyTileGroup.ORANGE));

        return b;
    }
}