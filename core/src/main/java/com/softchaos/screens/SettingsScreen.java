package com.softchaos.screens;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.softchaos.SoftChaosGame;
import com.softchaos.managers.AudioManager;

/** Music and SFX volume sliders. Navigates back to Main Menu on close. */
public class SettingsScreen implements Screen {

    private final SoftChaosGame game;
    private SpriteBatch batch;

    public SettingsScreen(SoftChaosGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
    }

    @Override
    public void render(float delta) {
        // TODO M4: render two sliders (Scene2D Slider widgets)
        // Music slider:  AudioManager.getInstance().setMusicVolume(value);
        // SFX slider:    AudioManager.getInstance().setSFXVolume(value);
        // Back button:   game.setScreen(new MainMenuScreen(game));
    }

    @Override public void resize(int width, int height) {}
    @Override public void pause() {}
    @Override public void resume() {}

    @Override
    public void hide() {
        dispose();
    }

    @Override
    public void dispose() {
        if (batch != null) { batch.dispose(); batch = null; }
    }
}
