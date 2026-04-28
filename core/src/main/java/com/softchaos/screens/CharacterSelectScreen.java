package com.softchaos.screens;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.softchaos.SoftChaosGame;
import com.softchaos.managers.GameStateManager;

/** 3 character cards. On click: save choice to GameStateManager → GameScreen. */
public class CharacterSelectScreen implements Screen {

    private final SoftChaosGame game;
    private SpriteBatch batch;

    public CharacterSelectScreen(SoftChaosGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
    }

    @Override
    public void render(float delta) {
        // TODO M1: render 3 character cards
        // On card click:
        //   GameStateManager.getInstance().selectedCharacter = index;
        //   game.setScreen(new GameScreen(game));
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
