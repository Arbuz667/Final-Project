package com.softchaos.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.softchaos.SoftChaosGame;
import com.softchaos.managers.GameStateManager;

/** Start screen. Press ENTER to begin the run from Forest. */
public class CharacterSelectScreen implements Screen {

    private final SoftChaosGame game;
    private SpriteBatch        batch;
    private BitmapFont         font;
    private Texture            background;
    private OrthographicCamera camera;

    public CharacterSelectScreen(SoftChaosGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        int w = Gdx.graphics.getWidth();
        int h = Gdx.graphics.getHeight();
        camera = new OrthographicCamera();
        camera.setToOrtho(false, w, h);
        batch      = new SpriteBatch();
        font       = new BitmapFont();
        font.getData().setScale(2.5f);
        background = new Texture(Gdx.files.internal("main_menu.png"));
        background.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        int sw = Gdx.graphics.getWidth();
        int sh = Gdx.graphics.getHeight();
        int cx = sw / 2;
        int cy = sh / 2;

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.draw(background, 0, 0, sw, sh);
        font.setColor(new Color(0.6f, 1f, 0.6f, 1f));
        font.draw(batch, "Forest  ->  Ocean  ->  Space", cx - 310f, cy + 60f);
        font.setColor(Color.YELLOW);
        font.draw(batch, "[ENTER] to start",   cx - 170f, cy - 20f);
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
        if (batch      != null) { batch.dispose();      batch      = null; }
        if (font       != null) { font.dispose();       font       = null; }
        if (background != null) { background.dispose(); background = null; }
    }
}
