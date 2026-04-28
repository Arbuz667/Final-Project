package com.softchaos.screens;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.softchaos.SoftChaosGame;
import com.softchaos.managers.AudioManager;
import com.softchaos.utils.MusicType;

/** Play / Quit buttons. Background music: "All I Need". → CharacterSelectScreen */
public class MainMenuScreen implements Screen {

    private final SoftChaosGame game;
    private SpriteBatch batch;

    public MainMenuScreen(SoftChaosGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        AudioManager.getInstance().playMusic(MusicType.MENU);
    }

    @Override
    public void render(float delta) {
        // TODO M1: render background, title, Play/Quit buttons (libGDX Scene2D or manual)
        // On Play click: game.setScreen(new CharacterSelectScreen(game));
        // On Quit click: Gdx.app.exit();
        AudioManager.getInstance().update(delta);
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
