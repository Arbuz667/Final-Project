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
import com.softchaos.utils.LocationType;

/** 3 character cards. On click: save choice to GameStateManager → GameScreen. */
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
        font.draw(batch, "SELECT LOCATION", cx - 180f, cy + 120f);
        font.setColor(Color.GREEN);
        font.draw(batch, "1 - Forest",      cx - 100f, cy + 30f);
        font.setColor(Color.YELLOW);
        font.draw(batch, "2 - City",        cx - 100f, cy - 40f);
        font.setColor(Color.CYAN);
        font.draw(batch, "3 - Ocean",       cx - 100f, cy - 110f);
        batch.end();

        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) startGame(LocationType.FOREST);
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) startGame(LocationType.CITY);
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)) startGame(LocationType.OCEAN);
    }

    private void startGame(LocationType location) {
        GameStateManager.getInstance().currentLocation = location;
        game.setScreen(new GameScreen(game));
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
