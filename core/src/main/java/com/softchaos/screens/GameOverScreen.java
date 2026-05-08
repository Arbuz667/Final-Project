package com.softchaos.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.softchaos.SoftChaosGame;
import com.softchaos.managers.AudioManager;
import com.softchaos.managers.GameStateManager;
import com.softchaos.utils.MusicType;

/** Shows run statistics after the player dies. ENTER → retry, M → main menu. */
public class GameOverScreen implements Screen {

    private final SoftChaosGame game;
    private SpriteBatch   batch;
    private ShapeRenderer shape;
    private BitmapFont    titleFont;
    private BitmapFont    bodyFont;
    private OrthographicCamera camera;

    public GameOverScreen(SoftChaosGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        int w = Gdx.graphics.getWidth();
        int h = Gdx.graphics.getHeight();
        camera = new OrthographicCamera();
        camera.setToOrtho(false, w, h);
        batch     = new SpriteBatch();
        shape     = new ShapeRenderer();
        titleFont = new BitmapFont();
        titleFont.getData().setScale(3.5f);
        bodyFont  = new BitmapFont();
        bodyFont.getData().setScale(1.8f);
        AudioManager.getInstance().playMusic(MusicType.GAME_OVER);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.04f, 0.02f, 0.02f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        GameStateManager gsm = GameStateManager.getInstance();
        int   sw  = Gdx.graphics.getWidth();
        int   sh  = Gdx.graphics.getHeight();
        float cx  = sw / 2f;
        float cy  = sh / 2f;

        int totalSecs = (int) gsm.sessionTime;
        String time   = String.format("%02d:%02d", totalSecs / 60, totalSecs % 60);

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shape.setProjectionMatrix(camera.combined);
        shape.begin(ShapeRenderer.ShapeType.Filled);
        // Centre panel
        shape.setColor(0.08f, 0.04f, 0.04f, 0.95f);
        shape.rect(cx - 280f, cy - 160f, 560f, 360f);
        // Red top accent
        shape.setColor(0.75f, 0.1f, 0.1f, 1f);
        shape.rect(cx - 280f, cy + 200f, 560f, 8f);
        shape.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        // Title
        titleFont.setColor(new Color(0.9f, 0.15f, 0.15f, 1f));
        GlyphLayout gl = new GlyphLayout(titleFont, "YOU DIED");
        titleFont.draw(batch, "YOU DIED", cx - gl.width / 2f, cy + 190f);

        // Stats
        GlyphLayout gl2 = new GlyphLayout();
        bodyFont.setColor(new Color(0.75f, 0.75f, 0.75f, 1f));

        String[] lines = {
            "Time survived:  " + time,
            "Enemies killed: " + gsm.kills,
            "Level reached:  " + gsm.playerLevel
        };
        float lineH = 44f;
        float startY = cy + 110f;
        for (int i = 0; i < lines.length; i++) {
            gl2.setText(bodyFont, lines[i]);
            bodyFont.draw(batch, lines[i], cx - gl2.width / 2f, startY - i * lineH);
        }

        // Buttons
        bodyFont.setColor(Color.WHITE);
        gl2.setText(bodyFont, "[ ENTER ]  Play Again");
        bodyFont.draw(batch, "[ ENTER ]  Play Again", cx - gl2.width / 2f, cy - 80f);

        bodyFont.setColor(new Color(0.6f, 0.6f, 0.6f, 1f));
        gl2.setText(bodyFont, "[ M ]  Main Menu");
        bodyFont.draw(batch, "[ M ]  Main Menu", cx - gl2.width / 2f, cy - 128f);

        batch.end();

        AudioManager.getInstance().update(delta);

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            GameStateManager.getInstance().reset();
            game.setScreen(new CharacterSelectScreen(game));
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.M)) {
            GameStateManager.getInstance().reset();
            game.setScreen(new MainMenuScreen(game));
        }
    }

    @Override public void resize(int width, int height) {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() { dispose(); }

    @Override
    public void dispose() {
        if (batch     != null) { batch.dispose();     batch     = null; }
        if (shape     != null) { shape.dispose();     shape     = null; }
        if (titleFont != null) { titleFont.dispose(); titleFont = null; }
        if (bodyFont  != null) { bodyFont.dispose();  bodyFont  = null; }
    }
}

