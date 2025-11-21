package com.krchvl.MonopolyGame.core.control;

import com.krchvl.MonopolyGame.core.engine.GameEngine;

public interface PlayerController {
    boolean isHuman();
    void onPhase(GameEngine engine);
}