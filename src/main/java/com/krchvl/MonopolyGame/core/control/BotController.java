package com.krchvl.MonopolyGame.core.control;

import com.krchvl.MonopolyGame.core.Board;
import com.krchvl.MonopolyGame.core.Player;
import com.krchvl.MonopolyGame.core.engine.GameEngine;
import com.krchvl.MonopolyGame.core.tiles.CompanyTile;
import com.krchvl.MonopolyGame.core.tiles.Tile;

public class BotController implements PlayerController {
    private final int reserve;

    private static final int BASE_STEP_DURATION = 200;
    private static final int MAX_DICE_ROLL = 12;
    private static final int THINKING_TIME = 600;

    private volatile boolean boughtThisTurn = false;

    public BotController(int reserve) { this.reserve = reserve; }

    @Override public boolean isHuman() { return false; }

    @Override
    public void onPhase(GameEngine engine) {
        GameEngine.Phase ph = engine.getPhase();

        switch (ph) {
            case AWAITING_ROLL:
                boughtThisTurn = false;
                delayThen(THINKING_TIME, engine::roll);
                break;

            case AWAITING_ROLL_AGAIN:
                delayThen(THINKING_TIME, engine::roll);
                break;

            case AWAITING_BUY_DECISION:
                int maxAnimationTime = BASE_STEP_DURATION * MAX_DICE_ROLL;
                delayThen(maxAnimationTime + THINKING_TIME, () -> handleBuyDecision(engine));
                break;

            case AWAITING_END_TURN:
                tryUpgradeOrEndTurn(engine);
                break;

            default:
                break;
        }
    }

    private void tryUpgradeOrEndTurn(GameEngine engine) {
        delayThen(600, () -> {
            if (boughtThisTurn) {
                boughtThisTurn = false;
                engine.endTurn();
                return;
            }

            CompanyTile tileToUpgrade = findTileToUpgrade(engine);

            if (tileToUpgrade != null) {
                engine.upgradeTile(tileToUpgrade);
                tryUpgradeOrEndTurn(engine);
            } else {
                engine.endTurn();
            }
        });
    }

    private CompanyTile findTileToUpgrade(GameEngine engine) {
        Player p = engine.current();
        Board board = engine.getBoard();

        for (int i = 0; i < board.size(); i++) {
            Tile t = board.getTile(i);
            if (t instanceof CompanyTile ct) {
                if (engine.canUpgradeTile(p, ct)) {
                    int cost = engine.calcTileUpgradeCost(ct);
                    if (p.getBalance() - cost >= reserve) {
                        return ct;
                    }
                }
            }
        }
        return null;
    }

    private void handleBuyDecision(GameEngine engine) {
        CompanyTile prop = engine.getPendingPurchase();
        Player p = engine.current();
        if (prop != null && p.getBalance() - prop.getPrice() >= reserve) {
            boughtThisTurn = true;
            engine.buy();
        } else {
            engine.pass();
        }
    }

    private void delayThen(int delayMs, Runnable action) {
        new Thread(() -> {
            try {
                Thread.sleep(delayMs);
                action.run();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }
}