package com.krchvl.MonopolyGame.core.engine;

import com.krchvl.MonopolyGame.core.*;
import com.krchvl.MonopolyGame.core.control.PlayerController;
import com.krchvl.MonopolyGame.core.engine.events.*;
import com.krchvl.MonopolyGame.core.tiles.CompanyLike;
import com.krchvl.MonopolyGame.core.tiles.CompanyTile;
import com.krchvl.MonopolyGame.core.tiles.CompanyTileGroup;
import com.krchvl.MonopolyGame.core.tiles.Tile;

import java.util.*;
import java.util.stream.Collectors;

public class GameEngine implements GameContext {

    public enum Phase {
        AWAITING_ROLL,
        AWAITING_BUY_DECISION,
        AWAITING_ROLL_AGAIN,
        AWAITING_END_TURN,
        GAME_OVER
    }

    public static final int MAX_STARS = 5;
    private static final int GO_REWARD = 200;

    private final Board board;
    private final List<Player> players;
    private final Map<Player, PlayerController> controllers;
    private final EventBus bus;
    private final Dice dice = new Dice();

    private int currentIndex = 0;
    private int round = 1;
    private Phase phase = Phase.AWAITING_ROLL;
    private int doublesInRow = 0;
    private DiceRoll lastRoll;
    private CompanyTile pendingPurchase;

    public GameEngine(Board board, List<Player> players,
                      Map<Player, PlayerController> controllers, EventBus bus) {
        this.board = Objects.requireNonNull(board);
        this.players = new ArrayList<>(players);
        this.controllers = new HashMap<>(controllers);
        this.bus = Objects.requireNonNull(bus);
    }

    public void start() {
        phase = Phase.AWAITING_ROLL;
        publish(new TurnStarted(current(), round));
        triggerControllerIfBot();
    }

    public void roll() {
        if (phase != Phase.AWAITING_ROLL && phase != Phase.AWAITING_ROLL_AGAIN) return;

        Player p = current();
        if (p.isInJail()) {
            jailRoll(p);
            triggerControllerIfBot();
        } else {
            normalRoll(p);
            triggerControllerIfBot();
        }
    }

    public void buy() {
        if (phase != Phase.AWAITING_BUY_DECISION || pendingPurchase == null) return;

        Player p = current();
        int price = pendingPurchase.getPrice();

        if (p.getBalance() >= price) {
            p.pay(price);
            pendingPurchase.setOwner(p);
            publish(new CompanyBought(p, pendingPurchase, price));
        } else {
            publish(new LogEvent(p.getName() + " не хватает денег на покупку " + pendingPurchase.getName()));
        }

        pendingPurchase = null;
        proceedAfterLanding();
        triggerControllerIfBot();
    }

    public void pass() {
        if (phase != Phase.AWAITING_BUY_DECISION || pendingPurchase == null) return;
        publish(new LogEvent(current().getName() + " пасует покупку " + pendingPurchase.getName()));
        pendingPurchase = null;
        proceedAfterLanding();
        triggerControllerIfBot();
    }

    public void endTurn() {
        if (phase != Phase.AWAITING_END_TURN) return;
        nextPlayer();
        triggerControllerIfBot();
    }

    private void normalRoll(Player p) {
        DiceRoll roll = dice.roll();
        lastRoll = roll;
        publish(new DiceRolled(p, roll));

        boolean isDouble = roll.isDouble();
        if (isDouble) {
            doublesInRow++;
            if (doublesInRow == 3) {
                sendToJail(p);
                phase = Phase.AWAITING_END_TURN;
                return;
            }
        } else {
            doublesInRow = 0;
        }

        moveAndLand(p, roll.sum(), roll);
        if (pendingPurchase != null) {
            phase = Phase.AWAITING_BUY_DECISION;
            return;
        }

        phase = (isDouble && !p.isInJail()) ? Phase.AWAITING_ROLL_AGAIN : Phase.AWAITING_END_TURN;
    }

    private void jailRoll(Player p) {
        DiceRoll roll = dice.roll();
        lastRoll = roll;
        publish(new DiceRolled(p, roll));

        if (roll.isDouble()) {
            p.releaseFromJail();
            publish(new LogEvent("Дубль! " + p.getName() + " выходит из тюрьмы."));
            moveAndLand(p, roll.sum(), roll);
            phase = (pendingPurchase != null) ? Phase.AWAITING_BUY_DECISION : Phase.AWAITING_END_TURN;
        } else {
            p.incrementJailTurns();
            if (p.getJailTurns() >= 3) {
                if (safePay(p, 50, null)) {
                    p.releaseFromJail();
                    publish(new LogEvent(p.getName() + " платит $50 и выходит из тюрьмы."));
                    moveAndLand(p, roll.sum(), roll);
                    phase = (pendingPurchase != null) ? Phase.AWAITING_BUY_DECISION : Phase.AWAITING_END_TURN;
                } else {
                    declareBankruptcy(p, null);
                }
            } else {
                publish(new LogEvent(p.getName() + " остаётся в тюрьме (" + p.getJailTurns() + "/3)."));
                phase = Phase.AWAITING_END_TURN;
            }
        }
    }

    private void proceedAfterLanding() {
        if (lastRoll != null && lastRoll.isDouble() && !current().isInJail()) {
            phase = Phase.AWAITING_ROLL_AGAIN;
        } else {
            phase = Phase.AWAITING_END_TURN;
        }
    }

    private void moveAndLand(Player p, int steps, DiceRoll roll) {
        int oldPos = p.getPosition();
        int newPos = (oldPos + steps) % board.size();

        if (newPos < oldPos) {
            p.receive(GO_REWARD);
            publish(new LogEvent(p.getName() + " проходит 'Старт' +$" + GO_REWARD));
        }

        p.setPosition(newPos);
        publish(new PlayerMoved(p, oldPos, newPos));

        Tile tile = board.getTile(newPos);
        publish(new LandedOnTile(p, newPos, tile));
        tile.onLand(this, p, roll);
        checkGameOver();
    }

    private void nextPlayer() {
        List<Player> alive = alivePlayers();
        if (alive.size() <= 1) {
            if (alive.size() == 1) {
                publish(new GameOver(alive.get(0)));
            }
            phase = Phase.GAME_OVER;
            return;
        }

        do {
            currentIndex = (currentIndex + 1) % players.size();
        } while (current().isBankrupt());

        doublesInRow = 0;
        lastRoll = null;
        phase = Phase.AWAITING_ROLL;
        round++;
        publish(new TurnStarted(current(), round));
    }

    private void declareBankruptcy(Player p, Player creditor) {
        p.setBankrupt(true);
        if (creditor != null) {
            transferAllAssets(p, creditor);
        } else {
            releaseAllAssetsToBank(p);
        }
        publish(new LogEvent(p.getName() + " банкрот."));
        checkGameOver();
    }

    private void transferAllAssets(Player from, Player to) {
        for (CompanyLike prop : board.allProperties()) {
            if (prop.getOwner() == from) {
                prop.setOwner(to);
            }
        }
    }

    private void releaseAllAssetsToBank(Player p) {
        for (CompanyLike prop : board.allProperties()) {
            if (prop.getOwner() == p) {
                prop.setOwner(null);
                if (prop instanceof CompanyTile ct) {
                    ct.setStars(0);
                }
            }
        }
    }

    private void checkGameOver() {
        List<Player> alive = alivePlayers();
        if (alive.size() == 1) {
            publish(new GameOver(alive.get(0)));
            phase = Phase.GAME_OVER;
        }
    }

    private List<Player> alivePlayers() {
        return players.stream().filter(pl -> !pl.isBankrupt()).collect(Collectors.toList());
    }

    private void triggerControllerIfBot() {
        if (phase == Phase.GAME_OVER) return;
        PlayerController ctrl = controllers.get(current());
        if (ctrl != null && !ctrl.isHuman()) {
            ctrl.onPhase(this);
        }
    }

    public boolean hasMonopoly(Player p, CompanyTileGroup group) {
        if (p == null || group == null) return false;
        boolean hasAny = false;
        for (int i = 0; i < board.size(); i++) {
            Tile t = board.getTile(i);
            if (t instanceof CompanyTile ct && ct.getGroup() == group) {
                hasAny = true;
                if (ct.getOwner() != p) return false;
            }
        }
        return hasAny;
    }

    public int calcTileUpgradeCost(CompanyTile tile) {
        int next = tile.getStars() + 1;
        if (next > MAX_STARS) return Integer.MAX_VALUE;
        return Math.max(50, (int) Math.round(tile.getPrice() * 0.25 * next));
    }

    public boolean canUpgradeTile(Player p, CompanyTile tile) {
        if (tile == null || p == null) return false;
        if (p != current()) return false;

        boolean canManage = phase == Phase.AWAITING_ROLL || phase == Phase.AWAITING_ROLL_AGAIN || phase == Phase.AWAITING_END_TURN;
        if (!canManage) return false;

        if (tile.getOwner() != p) return false;
        if (!hasMonopoly(p, tile.getGroup())) return false;
        if (tile.getStars() >= MAX_STARS) return false;

        int cost = calcTileUpgradeCost(tile);
        return p.getBalance() >= cost;
    }

    public void upgradeTile(CompanyTile tile) {
        Player p = current();
        if (!canUpgradeTile(p, tile)) {
            publish(new LogEvent("Нельзя улучшить " + (tile != null ? tile.getName() : "плитку") + " сейчас."));
            return;
        }
        int cost = calcTileUpgradeCost(tile);
        if (!safePay(p, cost, null)) return;
        tile.setStars(tile.getStars() + 1);
        publish(new CompanyUpgraded(p, tile, tile.getStars(), cost));
    }



    private int calcStarPrice(CompanyTile tile, int starIndex) {
        int base = Math.max(1, tile.getPrice() / 2);
        return Math.max(1, base * Math.max(1, starIndex));
    }

    public boolean canDowngradeTile(Player p, CompanyTile tile) {
        if (p == null || tile == null) return false;
        if (tile.getOwner() != p) return false;
        if (tile.isMortgaged()) return false;
        if (tile.getStars() <= 0) return false;

        int maxStars = 0;
        for (int i = 0; i < board.size(); i++) {
            Tile t = board.getTile(i);
            if (t instanceof CompanyTile ct
                    && ct.getOwner() == p
                    && ct.getGroup() == tile.getGroup()) {
                maxStars = Math.max(maxStars, ct.getStars());
            }
        }
        return tile.getStars() == maxStars;
    }

    public int calcTileDowngradeRefund(CompanyTile tile) {
        if (tile == null || tile.getStars() <= 0) return 0;
        int k = tile.getStars();
        int paidForKth = calcStarPrice(tile, k);
        return Math.max(1, Math.round(paidForKth / 2f));
    }

    public boolean downgradeTile(CompanyTile tile) {
        Player p = current();
        if (!canDowngradeTile(p, tile)) return false;

        int refund = calcTileDowngradeRefund(tile);
        tile.setStars(tile.getStars() - 1);
        p.receive(refund);
        bus.publish(new CompanyDowngraded(p, tile, tile.getStars(), refund));
        return true;
    }

    @Override
    public Board getBoard() {
        return board;
    }

    @Override
    public boolean safePay(Player from, int amount, Player to) {
        if (from.isBankrupt()) return false;
        if (from.getBalance() >= amount) {
            from.pay(amount);
            if (to != null) to.receive(amount);
            return true;
        }
        declareBankruptcy(from, to);
        return false;
    }

    @Override
    public void sendToJail(Player p) {
        p.setPosition(board.getJailIndex());
        p.sendToJail();
        publish(new SentToJail(p));
    }

    @Override
    public void offerPurchase(Player p, CompanyTile companyTile) {
        pendingPurchase = companyTile;
        publish(new PurchaseOffered(p, companyTile, companyTile.getPrice()));
        phase = Phase.AWAITING_BUY_DECISION;
    }

    @Override
    public void publish(Object event) {
        bus.publish(event);
    }

    public Player current() {
        return players.get(currentIndex);
    }

    public Phase getPhase() {
        return phase;
    }

    public List<Player> getPlayers() {
        return Collections.unmodifiableList(players);
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    public boolean isCurrentHuman() {
        PlayerController ctrl = controllers.get(current());
        return ctrl == null || ctrl.isHuman();
    }

    public CompanyTile getPendingPurchase() {
        return pendingPurchase;
    }
}