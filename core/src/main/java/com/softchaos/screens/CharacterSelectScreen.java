package com.softchaos.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.softchaos.SoftChaosGame;
import com.softchaos.managers.GameStateManager;

/** Start screen. Press ENTER to begin the run from Forest. */
public class CharacterSelectScreen implements Screen {

    private final SoftChaosGame game;
    private SpriteBatch batch;
    private BitmapFont font;

    public CharacterSelectScreen(SoftChaosGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        font = new BitmapFont();
        font.getData().setScale(2.5f);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.05f, 0.05f, 0.15f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        int cx = Gdx.graphics.getWidth()  / 2;
        int cy = Gdx.graphics.getHeight() / 2;

        batch.begin();
        font.setColor(Color.WHITE);
        font.draw(batch, "SOFT CHAOS",         cx - 130f, cy + 80f);
        font.setColor(new Color(0.6f, 1f, 0.6f, 1f));
        font.draw(batch, "Forest  ->  Ocean  ->  Space", cx - 310f, cy);
        font.setColor(Color.YELLOW);
        font.draw(batch, "[ENTER] to start",   cx - 170f, cy - 80f);
        batch.end();

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            GameStateManager.getInstance().reset();
            game.setScreen(new WeaponSelectScreen(game));
        }
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
        if (font  != null) { font.dispose();  font  = null; }
    }
}
