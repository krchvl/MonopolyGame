package com.krchvl.MonopolyGame.core;

import com.krchvl.MonopolyGame.core.tiles.CompanyLike;
import com.krchvl.MonopolyGame.core.tiles.CompanyTileGroup;
import com.krchvl.MonopolyGame.core.tiles.Tile;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Board {
    private final List<Tile> tiles = new ArrayList<>();
    private int goIndex;
    private int jailIndex = 10;

    public void addTile(Tile tile) {
        tiles.add(Objects.requireNonNull(tile));
    }

    public Tile getTile(int index) {
        return tiles.get(index);
    }

    public int size() {
        return tiles.size();
    }

    public void setGoIndex(int goIndex) {
        this.goIndex = goIndex;
    }

    public void setJailIndex(int jailIndex) {
        this.jailIndex = jailIndex;
    }

    public int getJailIndex() { return jailIndex; }

    public List<CompanyLike> allProperties() {
        return tiles.stream()
                .filter(t -> t instanceof CompanyLike)
                .map(t -> (CompanyLike) t)
                .collect(Collectors.toList());
    }

    public long countOwnedInGroup(Player owner, CompanyTileGroup group) {
        return allProperties().stream()
                .filter(p -> p.getGroup() == group && p.getOwner() == owner)
                .count();
    }

    public long totalInGroup(CompanyTileGroup group) {
        return allProperties().stream()
                .filter(p -> p.getGroup() == group)
                .count();
    }
}