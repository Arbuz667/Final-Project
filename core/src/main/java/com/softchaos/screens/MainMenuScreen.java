package com.softchaos.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.softchaos.SoftChaosGame;
import com.softchaos.managers.AudioManager;
import com.softchaos.utils.MusicType;

/** Play / Quit buttons over background image. → CharacterSelectScreen */
public class MainMenuScreen implements Screen {

    // Button colours matching the screenshot
    private static final Color BTN_FILL        = new Color(0.24f, 0.18f, 0.56f, 0.93f);
    private static final Color BTN_FILL_HOVER  = new Color(0.34f, 0.27f, 0.70f, 0.96f);
    private static final Color GOLD_OUTER      = new Color(0.95f, 0.76f, 0.10f, 1f);
    private static final Color GOLD_INNER      = new Color(1.00f, 0.92f, 0.55f, 1f);

    private final SoftChaosGame game;
    private SpriteBatch        batch;
    private ShapeRenderer      shape;
    private BitmapFont         fontPlay;
    private BitmapFont         fontQuit;
    private OrthographicCamera camera;
    private Texture            background;
    private Rectangle          playBtn;
    private Rectangle          quitBtn;
    private final GlyphLayout  layout = new GlyphLayout();

    public MainMenuScreen(SoftChaosGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        int w = Gdx.graphics.getWidth();
        int h = Gdx.graphics.getHeight();
        camera = new OrthographicCamera();
        camera.setToOrtho(false, w, h);

        batch      = new SpriteBatch();
        shape      = new ShapeRenderer();
        background = new Texture(Gdx.files.internal("main_menu.png"));
        background.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        fontPlay = new BitmapFont();
        fontPlay.getData().setScale(3.8f);
        fontQuit = new BitmapFont();
        fontQuit.getData().setScale(2.2f);

        float playW = 300f, playH = 78f;
        float quitW = 190f, quitH = 54f;
        float cx = w / 2f;
        float cy = h / 2f;

        playBtn = new Rectangle(cx - playW / 2f, cy - 10f,  playW, playH);
        quitBtn = new Rectangle(cx - quitW / 2f, cy - 105f, quitW, quitH);

        AudioManager.getInstance().playMusic(MusicType.MENU);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        int   sw = Gdx.graphics.getWidth();
        int   sh = Gdx.graphics.getHeight();
        float mx = Gdx.input.getX();
        float my = sh - Gdx.input.getY();     // libGDX Y is bottom-up

        boolean playHover = playBtn.contains(mx, my);
        boolean quitHover = quitBtn.contains(mx, my);
        boolean clicked   = Gdx.input.justTouched();

        // --- Background ---
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.draw(background, 0, 0, sw, sh);
        batch.end();

        // --- Button fills ---
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shape.setProjectionMatrix(camera.combined);
        shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.setColor(playHover ? BTN_FILL_HOVER : BTN_FILL);
        shape.rect(playBtn.x, playBtn.y, playBtn.width, playBtn.height);
        shape.setColor(quitHover ? BTN_FILL_HOVER : BTN_FILL);
        shape.rect(quitBtn.x, quitBtn.y, quitBtn.width, quitBtn.height);
        shape.end();

        // --- Gold double border ---
        shape.begin(ShapeRenderer.ShapeType.Line);
        Gdx.gl20.glLineWidth(3f);
        for (Rectangle btn : new Rectangle[]{playBtn, quitBtn}) {
            shape.setColor(GOLD_OUTER);
            shape.rect(btn.x, btn.y, btn.width, btn.height);
            shape.setColor(GOLD_INNER);
            shape.rect(btn.x + 3, btn.y + 3, btn.width - 6, btn.height - 6);
        }
        Gdx.gl20.glLineWidth(1f);
        shape.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        // --- Labels (centred in buttons) ---
        batch.begin();
        fontPlay.setColor(Color.WHITE);
        layout.setText(fontPlay, "PLAY");
        fontPlay.draw(batch, "PLAY",
            playBtn.x + (playBtn.width  - layout.width)  / 2f,
            playBtn.y + (playBtn.height + layout.height) / 2f);

        fontQuit.setColor(new Color(0.92f, 0.92f, 0.92f, 1f));
        layout.setText(fontQuit, "Quit");
        fontQuit.draw(batch, "Quit",
            quitBtn.x + (quitBtn.width  - layout.width)  / 2f,
            quitBtn.y + (quitBtn.height + layout.height) / 2f);
        batch.end();

        // --- Input ---
        if ((clicked && playHover) || Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            game.setScreen(new CharacterSelectScreen(game));
        }
        if ((clicked && quitHover) || Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            Gdx.app.exit();
        }

        AudioManager.getInstance().update(delta);
    }

    @Override public void resize(int width, int height) {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() { dispose(); }

    @Override
    public void dispose() {
        if (batch      != null) { batch.dispose();      batch      = null; }
        if (shape      != null) { shape.dispose();      shape      = null; }
        if (fontPlay   != null) { fontPlay.dispose();   fontPlay   = null; }
        if (fontQuit   != null) { fontQuit.dispose();   fontQuit   = null; }
        if (background != null) { background.dispose(); background = null; }
    }
}
