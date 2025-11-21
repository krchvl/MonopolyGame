package com.krchvl.MonopolyGame.fx.anim;

import com.krchvl.MonopolyGame.fx.theme.Anim;
import javafx.animation.*;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;

import java.util.List;
import java.util.function.Function;
import java.util.function.IntFunction;

public final class DiceOverlay {

    private static final double DICE_SPACING = 12.0;

    private final StackPane parent;
    private final Function<Object, List<Integer>> parseDiceValues;
    private final IntFunction<String> getDieCharacter;

    private Node overlayNode;
    private Animation currentAnimation;

    public DiceOverlay(StackPane parent,
                       Function<Object, List<Integer>> parseDiceValues,
                       IntFunction<String> getDieCharacter) {
        this.parent = parent;
        this.parseDiceValues = parseDiceValues;
        this.getDieCharacter = getDieCharacter;
    }

    public Animation show(Object rollData) {
        stop();

        List<Integer> dice = parseDiceValues.apply(rollData);
        Node content;

        if (dice.size() == 2) {
            HBox diceContainer = new HBox(DICE_SPACING);
            diceContainer.setAlignment(Pos.CENTER);

            for (int i = 0; i < dice.size(); i++) {
                int dieValue = dice.get(i);
                Label dieLabel = buildDieLabel(getDieCharacter.apply(dieValue));
                dieLabel.setOpacity(0);

                FadeTransition appear = new FadeTransition(Duration.millis(Anim.DICE_FADE_IN_MS), dieLabel);
                appear.setToValue(1);
                appear.setDelay(Duration.millis((long) i * Anim.DICE_STAGGER_MS));

                RotateTransition rotate = new RotateTransition(Duration.millis(Anim.DICE_ROTATE_MS), dieLabel);
                rotate.setByAngle(360);
                rotate.setDelay(Duration.millis((long) i * Anim.DICE_STAGGER_MS));

                new ParallelTransition(appear, rotate).play();
                diceContainer.getChildren().add(dieLabel);
            }
            content = diceContainer;
        } else {
            Label fallbackBadge = new Label(String.valueOf(rollData));
            fallbackBadge.setAlignment(Pos.CENTER);
            fallbackBadge.getStyleClass().add("badge");
            content = fallbackBadge;
        }

        overlayNode = content;
        overlayNode.setOpacity(0);
        parent.getChildren().add(overlayNode);
        StackPane.setAlignment(overlayNode, Pos.CENTER);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(Anim.DICE_FADE_IN_MS), overlayNode);
        fadeIn.setToValue(1);

        PauseTransition pause = new PauseTransition(Duration.millis(Anim.DICE_PAUSE_MS));

        FadeTransition fadeOut = new FadeTransition(Duration.millis(Anim.DICE_FADE_OUT_MS), overlayNode);
        fadeOut.setToValue(0);

        currentAnimation = new SequentialTransition(fadeIn, pause, fadeOut);
        currentAnimation.setOnFinished(e -> cleanup());
        currentAnimation.play();
        return currentAnimation;
    }

    public boolean isRunning() {
        return currentAnimation != null && currentAnimation.getStatus() == Animation.Status.RUNNING;
    }

    public void stop() {
        if (isRunning()) {
            currentAnimation.stop();
        }
        cleanup();
    }

    private void cleanup() {
        if (overlayNode != null) {
            parent.getChildren().remove(overlayNode);
            overlayNode = null;
        }
        currentAnimation = null;
    }

    private static Label buildDieLabel(String face) {
        Label die = new Label(face);
        die.setAlignment(Pos.CENTER);
        die.setMinSize(64, 64);
        die.setPrefSize(64, 64);
        die.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        die.getStyleClass().add("die");
        die.setTextAlignment(TextAlignment.CENTER);
        return die;
    }
}