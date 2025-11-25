package com.krchvl.MonopolyGame.core.tiles;

import com.krchvl.MonopolyGame.core.Board;

public final class DefaultBoards {
    private DefaultBoards() {}

    public static Board sampleClassicLikeBoard() {
        Board b = new Board();
        b.setGoIndex(0);
        b.setJailIndex(12);

        b.addTile(new GoTile("Старт")); // 0
        b.addTile(new CompanyTile("Альфа Банк", "/images/companies/alfabank.png", 60, 6, CompanyTileGroup.GREEN));
        b.addTile(new CompanyTile("Сбербанк", "/images/companies/sberbank.png", 60, 6, CompanyTileGroup.GREEN));
        b.addTile(new UtilityTile("Газпром", "/images/companies/gasprom.png", 150));
        b.addTile(new CompanyTile("Т-Банк", "/images/companies/tbank.png", 80, 8, CompanyTileGroup.GREEN));

        b.addTile(new CompanyTile("МТС", "/images/companies/mts.png", 100, 10, CompanyTileGroup.LIGHT_BLUE)); // 5
        b.addTile(new ChanceTile("Шанс"));
        b.addTile(new CompanyTile("Теле2", "/images/companies/tele2.png", 100, 10, CompanyTileGroup.LIGHT_BLUE));
        b.addTile(new CompanyTile("Мегафон", "/images/companies/megafon.png", 120, 14, CompanyTileGroup.LIGHT_BLUE));

        b.addTile(new CompanyTile("Пятёрочка", "/images/companies/pyaterochka.png", 140, 16, CompanyTileGroup.PINK)); // 9
        b.addTile(new UtilityTile("Яндекс", "/images/companies/yandex.png", 150));
        b.addTile(new CompanyTile("Билайн", "/images/companies/beeline.png", 120, 14, CompanyTileGroup.LIGHT_BLUE));

        // Тюрьма (Индекс 12)
        b.addTile(new JailTile("Тюрьма"));

        b.addTile(new CompanyTile("Дикси", "/images/companies/diksi.png", 140, 16, CompanyTileGroup.PINK));
        b.addTile(new CompanyTile("Перекрёсток", "/images/companies/perekrestok.png", 160, 18, CompanyTileGroup.PINK));
        b.addTile(new CompanyTile("Магнит", "/images/companies/magnit.png", 160, 18, CompanyTileGroup.PINK));

        b.addTile(new CompanyTile("Вкусно - и точка", "/images/companies/mc.png", 200, 24, CompanyTileGroup.ORANGE));
        b.addTile(new CompanyTile("Бургер кинг", "/images/companies/burger king.png", 200, 24, CompanyTileGroup.ORANGE));

        b.addTile(new TaxTile("Подоходный налог", 150)); // 19

        b.addTile(new CompanyTile("Лукойл", "/images/companies/lukoil.png", 260, 35, CompanyTileGroup.YELLOW));
        b.addTile(new CompanyTile("Роснефть", "/images/companies/rosneft.png", 280, 40, CompanyTileGroup.YELLOW));

        b.addTile(new CompanyTile("Rostic's", "/images/companies/rostics.png", 220, 26, CompanyTileGroup.ORANGE));

        b.addTile(new JailTile("Тюрьма"));

        // Дубликат Перекрестка в конце? Сделаем его дорогим "Розовым" активом
        b.addTile(new CompanyTile("Перекрёсток (VIP)", "/images/companies/perekrestok.png", 240, 30, CompanyTileGroup.PINK));

        return b;
    }

    public static Board testStarsBoard() {
        Board b = new Board();
        b.setGoIndex(0);

        b.addTile(new CompanyTile("Сбербанк", "/images/companies/sberbank.png", 60, 6, CompanyTileGroup.GREEN));
        b.addTile(new CompanyTile("Магнит", "/images/companies/magnit.png", 160, 18, CompanyTileGroup.PINK));
        b.addTile(new CompanyTile("Лукойл", "/images/companies/lukoil.png", 300, 50, CompanyTileGroup.YELLOW));
        b.addTile(new CompanyTile("Rostic's", "/images/companies/rostics.png", 220, 26, CompanyTileGroup.ORANGE));

        return b;
    }
}