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
        font  = new BitmapFont();
        font.setColor(Color.WHITE);
        font.getData().setScale(1.8f);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.05f, 0.05f, 0.1f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // TODO M4: replace with proper character card UI
        batch.begin();
        font.draw(batch, "SELECT LOCATION",      Gdx.graphics.getWidth() / 2f - 160, Gdx.graphics.getHeight() / 2f + 80);
        font.draw(batch, "1 - Forest",           Gdx.graphics.getWidth() / 2f - 80,  Gdx.graphics.getHeight() / 2f + 20);
        font.draw(batch, "2 - City",             Gdx.graphics.getWidth() / 2f - 80,  Gdx.graphics.getHeight() / 2f - 30);
        font.draw(batch, "3 - Ocean",            Gdx.graphics.getWidth() / 2f - 80,  Gdx.graphics.getHeight() / 2f - 80);
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
