package com.krchvl.MonopolyGame.fx.anim;

import com.krchvl.MonopolyGame.core.Player;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class MovementAnimator {
    private static final int STEP_DURATION_MS = 200;

    private final int boardSize;
    private final BiConsumer<Player, Integer> setVisualPosition;
    private final Runnable renderTokens;
    private final Consumer<Integer> onLandingEffect;

    private Timeline timeline;

    public MovementAnimator(int boardSize,
                            BiConsumer<Player, Integer> setVisualPosition,
                            Runnable renderTokens,
                            Consumer<Integer> onLandingEffect) {
        this.boardSize = boardSize;
        this.setVisualPosition = setVisualPosition;
        this.renderTokens = renderTokens;
        this.onLandingEffect = onLandingEffect;
    }

    public void animate(Player player, int from, int to) {
        stop();

        int distance = Math.floorMod(to - from, boardSize);
        if (distance == 0) {
            setVisualPosition.accept(player, to);
            renderTokens.run();
            if (onLandingEffect != null) {
                onLandingEffect.accept(to);
            }
            return;
        }

        timeline = new Timeline();

        for (int step = 0; step <= distance; step++) {
            final int currentPos = (from + step) % boardSize;
            final boolean isFinalStep = (step == distance);

            KeyFrame keyFrame = new KeyFrame(
                    Duration.millis((long) step * STEP_DURATION_MS),
                    event -> {
                        setVisualPosition.accept(player, currentPos);
                        renderTokens.run();
                        if (isFinalStep && onLandingEffect != null) {
                            onLandingEffect.accept(currentPos);
                        }
                    }
            );
            timeline.getKeyFrames().add(keyFrame);
        }

        timeline.setOnFinished(event -> {
            setVisualPosition.accept(player, to);
            renderTokens.run();
            timeline = null;
        });

        timeline.playFromStart();
    }

    public boolean isRunning() {
        return timeline != null && timeline.getStatus() == Animation.Status.RUNNING;
    }

    public void stop() {
        if (isRunning()) {
            timeline.stop();
        }
        timeline = null;
    }
}