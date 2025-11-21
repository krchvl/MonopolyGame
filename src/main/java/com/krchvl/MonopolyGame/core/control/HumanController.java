package com.krchvl.MonopolyGame.core.control;

import com.krchvl.MonopolyGame.core.engine.GameEngine;

public class HumanController implements PlayerController {
    @Override public boolean isHuman() { return true; }
    @Override public void onPhase(GameEngine engine) {}
}