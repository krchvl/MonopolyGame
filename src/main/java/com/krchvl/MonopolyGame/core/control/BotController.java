package com.krchvl.MonopolyGame.core.control;

import com.krchvl.MonopolyGame.core.Player;
import com.krchvl.MonopolyGame.core.engine.GameEngine;
import com.krchvl.MonopolyGame.core.tiles.CompanyTile;

import static com.krchvl.MonopolyGame.core.engine.GameEngine.Phase;

public class BotController implements PlayerController {
    private final int reserve;

    private static final int BASE_STEP_DURATION = 200; // из GameController
    private static final int MAX_DICE_ROLL = 12;

    public BotController(int reserve) { this.reserve = reserve; }

    @Override public boolean isHuman() { return false; }

    @Override
    public void onPhase(GameEngine engine) {
        Phase ph = engine.getPhase();

        switch (ph) {
            case AWAITING_ROLL:
            case AWAITING_ROLL_AGAIN:
                delayThen(800, engine::roll);
                break;

            case AWAITING_BUY_DECISION:
                int maxAnimationTime = BASE_STEP_DURATION * MAX_DICE_ROLL;
                int thinkingTime = 800;
                delayThen(maxAnimationTime + thinkingTime, () -> handleBuyDecision(engine));
                break;

            case AWAITING_END_TURN:
                delayThen(600, engine::endTurn);
                break;

            default:
                break;
        }
    }

    private void handleBuyDecision(GameEngine engine) {
        CompanyTile prop = engine.getPendingPurchase();
        Player p = engine.current();
        if (prop != null && p.getBalance() - prop.getPrice() >= reserve) {
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