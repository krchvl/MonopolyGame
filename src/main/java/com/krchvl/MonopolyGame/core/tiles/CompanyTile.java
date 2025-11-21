package com.krchvl.MonopolyGame.core.tiles;

import com.krchvl.MonopolyGame.core.DiceRoll;
import com.krchvl.MonopolyGame.core.Player;
import com.krchvl.MonopolyGame.core.engine.GameContext;
import com.krchvl.MonopolyGame.core.engine.events.RentPaid;

public class CompanyTile extends Tile implements CompanyLike {
    private final int price;
    private final int baseRent;
    private final CompanyTileGroup group;
    private Player owner;
    private boolean mortgaged = false;

    private int stars = 0;

    private static final double STAR_RENT_BONUS = 0.50;

    public CompanyTile(String name, String imgSrc, int price, int baseRent, CompanyTileGroup group) {
        super(name, imgSrc);
        this.price = price;
        this.baseRent = baseRent;
        this.group = group;
    }

    @Override
    public void onLand(GameContext game, Player player, DiceRoll lastRoll) {
        if (owner == null) {
            game.offerPurchase(player, this);
            return;
        }
        if (owner == player || mortgaged) {
            return;
        }
        int rent = calculateRent(game, lastRoll);
        game.publish(new RentPaid(player, owner, rent, getName()));
        game.safePay(player, rent, owner);
    }

    protected int calculateRent(GameContext game, DiceRoll lastRoll) {
        long ownedInGroup = game.getBoard().countOwnedInGroup(owner, group);
        long totalInGroup = game.getBoard().totalInGroup(group);

        boolean hasMonopoly = (ownedInGroup == totalInGroup) && totalInGroup > 1
                && group != CompanyTileGroup.RED && group != CompanyTileGroup.BROWN;

        int rent = hasMonopoly ? baseRent * 2 : baseRent;

        if (stars > 0) {
            double multiplier = 1.0 + STAR_RENT_BONUS * stars;
            rent = (int) Math.round(rent * multiplier);
        }
        return Math.max(0, rent);
    }

    public int getPrice() { return price; }
    public int getBaseRent() { return baseRent; }
    public CompanyTileGroup getGroup() { return group; }

    @Override public Player getOwner() { return owner; }
    @Override public void setOwner(Player owner) { this.owner = owner; }
    public boolean isMortgaged() { return mortgaged; }
    public void setMortgaged(boolean mortgaged) { this.mortgaged = mortgaged; }

    public int getStars() { return stars; }
    public void setStars(int stars) {
        this.stars = Math.max(0, stars);
    }
}