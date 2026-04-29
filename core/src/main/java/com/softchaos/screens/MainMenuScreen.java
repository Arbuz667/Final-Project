package com.softchaos.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.softchaos.SoftChaosGame;
import com.softchaos.managers.AudioManager;
import com.softchaos.utils.MusicType;

/** Play / Quit buttons. Background music: "All I Need". → CharacterSelectScreen */
public class MainMenuScreen implements Screen {

    private final SoftChaosGame game;
    private SpriteBatch batch;
    private BitmapFont font;

    public MainMenuScreen(SoftChaosGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        font = new BitmapFont();
        font.getData().setScale(2.5f);
        AudioManager.getInstance().playMusic(MusicType.MENU);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.05f, 0.05f, 0.15f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        int cx = Gdx.graphics.getWidth()  / 2;
        int cy = Gdx.graphics.getHeight() / 2;

        batch.begin();
        font.setColor(Color.WHITE);
        font.draw(batch, "SOFT CHAOS",   cx - 150f, cy + 100f);
        font.setColor(Color.LIGHT_GRAY);
        font.draw(batch, "ENTER - Play", cx - 120f, cy + 20f);
        font.draw(batch, "ESC   - Quit", cx - 120f, cy - 50f);
        batch.end();

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            game.setScreen(new CharacterSelectScreen(game));
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            Gdx.app.exit();
        }

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
        if (font  != null) { font.dispose();  font  = null; }
    }
}
