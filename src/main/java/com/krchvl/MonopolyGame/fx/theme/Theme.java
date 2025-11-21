package com.krchvl.MonopolyGame.fx.theme;

import com.krchvl.MonopolyGame.core.PlayerGroup;
import com.krchvl.MonopolyGame.core.tiles.CompanyTileGroup;

import java.util.Map;

public class Theme {
    public Theme() {}

    public static final Map<PlayerGroup, String> PLAYER_COLORS = Map.of(
            PlayerGroup.RED,        "#e74c3c",
            PlayerGroup.YELLOW,     "#f1c40f",
            PlayerGroup.LIGHT_BLUE, "#5dade2",
            PlayerGroup.GREEN,      "#27ae60",
            PlayerGroup.ORANGE,     "#e67e22"
    );

    public static final Map<CompanyTileGroup, String> GROUP_COLORS = Map.of(
            CompanyTileGroup.BROWN,      "#8B4513",
            CompanyTileGroup.LIGHT_BLUE, "#87CEEB",
            CompanyTileGroup.PINK,       "#FFC0CB",
            CompanyTileGroup.ORANGE,     "#FFA500",
            CompanyTileGroup.RED,        "#DC143C",
            CompanyTileGroup.YELLOW,     "#FFD700",
            CompanyTileGroup.GREEN,      "#228B22",
            CompanyTileGroup.DARK_BLUE,  "#00008B"
    );
}
