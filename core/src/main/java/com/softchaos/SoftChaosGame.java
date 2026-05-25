package com.softchaos;

import com.badlogic.gdx.Game;
import com.softchaos.managers.AssetLoader;
import com.softchaos.managers.AudioManager;
import com.softchaos.managers.GameStateManager;
import com.softchaos.screens.MainMenuScreen;

/** Application entry point (shared by all platforms). Extends libGDX Game for screen management. */
public class SoftChaosGame extends Game {

    @Override
    public void create() {
        AssetLoader.getInstance().load();
        AudioManager.getInstance();
        GameStateManager.getInstance();
        setScreen(new MainMenuScreen(this));
    }

    @Override
    public void dispose() {
        super.dispose();
        AssetLoader.getInstance().dispose();
        AudioManager.getInstance().dispose();
    }
}
