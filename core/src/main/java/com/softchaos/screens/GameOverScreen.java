package com.softchaos.screens;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.softchaos.SoftChaosGame;
import com.softchaos.managers.AudioManager;
import com.softchaos.managers.GameStateManager;
import com.softchaos.utils.MusicType;

/** Shows run statistics. Music: "Where Is My Mind". Buttons: Retry / Main Menu. */
public class GameOverScreen implements Screen {

    private final SoftChaosGame game;
    private SpriteBatch batch;

    public GameOverScreen(SoftChaosGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        AudioManager.getInstance().playMusic(MusicType.GAME_OVER);
    }

    @Override
    public void render(float delta) {
        GameStateManager gsm = GameStateManager.getInstance();
        // TODO M4: render stats: gsm.sessionTime, gsm.kills, gsm.playerLevel
        // Retry button:    GameStateManager.getInstance().reset(); game.setScreen(new CharacterSelectScreen(game));
        // MainMenu button: game.setScreen(new MainMenuScreen(game));
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
