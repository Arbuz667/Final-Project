package com.softchaos.screens;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.softchaos.SoftChaosGame;
import com.softchaos.managers.AudioManager;
import com.softchaos.utils.MusicType;

/** Boss defeated — victory screen. Buttons: Next Location / Main Menu. */
public class WinScreen implements Screen {

    private final SoftChaosGame game;
    private SpriteBatch batch;

    public WinScreen(SoftChaosGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        AudioManager.getInstance().playMusic(MusicType.WIN);
    }

    @Override
    public void render(float delta) {
        // TODO M4: render win message + stats
        // Next Location button: advance location in GameStateManager, game.setScreen(new GameScreen(game));
        // Main Menu button:     game.setScreen(new MainMenuScreen(game));
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
