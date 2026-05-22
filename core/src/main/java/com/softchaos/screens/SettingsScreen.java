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

/** Music and SFX volume sliders. ESC → back to previous screen. */
public class SettingsScreen implements Screen {

    private static final float SLIDER_W    = 500f;
    private static final float SLIDER_H    = 22f;
    private static final float STEP        = 0.05f;
    private static final float HOLD_DELAY  = 0.4f;
    private static final float HOLD_REPEAT = 0.08f;

    private final SoftChaosGame game;
    private final Screen        returnScreen;

    private SpriteBatch        batch;
    private ShapeRenderer      shape;
    private BitmapFont         titleFont;
    private BitmapFont         bodyFont;
    private OrthographicCamera camera;
    private Texture            background;
    private GlyphLayout        layout;

    /** 0 = Music, 1 = SFX */
    private int     selected    = 0;
    private float   holdTimer   = 0f;
    private float   repeatTimer = 0f;
    private boolean holding     = false;
    private int     dragging    = -1; // which slider is being dragged (-1 = none)

    private float sliderX, musicY, sfxY;

    public SettingsScreen(SoftChaosGame game, Screen returnScreen) {
        this.game         = game;
        this.returnScreen = returnScreen;
    }

    @Override
    public void show() {
        int w = Gdx.graphics.getWidth();
        int h = Gdx.graphics.getHeight();
        camera = new OrthographicCamera();
        camera.setToOrtho(false, w, h);

        batch      = new SpriteBatch();
        shape      = new ShapeRenderer();
        layout     = new GlyphLayout();
        titleFont  = new BitmapFont();
        titleFont.getData().setScale(3.5f);
        bodyFont   = new BitmapFont();
        bodyFont.getData().setScale(2f);
        background = new Texture(Gdx.files.internal("main_menu.png"));
        background.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        float cx = w / 2f;
        float cy = h / 2f;
        sliderX = cx - SLIDER_W / 2f;
        musicY  = cy + 40f;
        sfxY    = cy - 90f;
    }

    @Override
    public void render(float delta) {
        int   sw = Gdx.graphics.getWidth();
        int   sh = Gdx.graphics.getHeight();
        float mx = Gdx.input.getX();
        float my = sh - Gdx.input.getY();

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        AudioManager am    = AudioManager.getInstance();
        float        music = am.getMusicVolume();
        float        sfx   = am.getSFXVolume();

        // Background
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.draw(background, 0, 0, sw, sh);
        batch.end();

        // Dark overlay panel
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shape.setProjectionMatrix(camera.combined);
        shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.setColor(0f, 0f, 0f, 0.65f);
        shape.rect(sliderX - 60f, sfxY - 70f, SLIDER_W + 120f, musicY - sfxY + SLIDER_H + 160f);
        shape.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        // Sliders
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shape.begin(ShapeRenderer.ShapeType.Filled);
        drawSlider(music, sliderX, musicY, selected == 0);
        drawSlider(sfx,   sliderX, sfxY,   selected == 1);
        shape.end();
        shape.begin(ShapeRenderer.ShapeType.Line);
        shape.setColor(selected == 0 ? Color.WHITE : new Color(0.5f, 0.5f, 0.5f, 1f));
        shape.rect(sliderX, musicY, SLIDER_W, SLIDER_H);
        shape.setColor(selected == 1 ? Color.WHITE : new Color(0.5f, 0.5f, 0.5f, 1f));
        shape.rect(sliderX, sfxY, SLIDER_W, SLIDER_H);
        shape.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        // Text
        batch.begin();
        float cx = sw / 2f;

        titleFont.setColor(Color.WHITE);
        layout.setText(titleFont, "SETTINGS");
        titleFont.draw(batch, "SETTINGS", cx - layout.width / 2f, musicY + SLIDER_H + 140f);

        bodyFont.setColor(selected == 0 ? Color.YELLOW : Color.LIGHT_GRAY);
        bodyFont.draw(batch, String.format("Music Volume:  %d%%", Math.round(music * 100)),
            sliderX, musicY + SLIDER_H + 36f);

        bodyFont.setColor(selected == 1 ? Color.YELLOW : Color.LIGHT_GRAY);
        bodyFont.draw(batch, String.format("SFX Volume:    %d%%", Math.round(sfx * 100)),
            sliderX, sfxY + SLIDER_H + 36f);

        bodyFont.setColor(new Color(0.6f, 0.6f, 0.6f, 1f));
        bodyFont.getData().setScale(1.4f);
        layout.setText(bodyFont, "Click slider · Arrow keys · ESC to close");
        bodyFont.draw(batch, "Click slider \u00b7 Arrow keys \u00b7 ESC to close",
            cx - layout.width / 2f, sfxY - 28f);
        bodyFont.getData().setScale(2f);

        batch.end();

        // Input handled AFTER drawing — prevents NPE if setScreen() is called
        // (which would invoke hide() → dispose() → batch=null mid-frame)
        handleMouse(mx, my);
        handleKeyboard(delta);
    }

    private void handleMouse(float mx, float my) {
        boolean pressed = Gdx.input.isTouched();
        boolean justReleased = !pressed && dragging != -1;

        // Detect drag start
        if (Gdx.input.justTouched()) {
            if (new Rectangle(sliderX, musicY - 10f, SLIDER_W, SLIDER_H + 20f).contains(mx, my)) {
                dragging = 0; selected = 0;
            } else if (new Rectangle(sliderX, sfxY - 10f, SLIDER_W, SLIDER_H + 20f).contains(mx, my)) {
                dragging = 1; selected = 1;
            }
        }

        if (dragging != -1 && pressed) {
            float val = clamp((mx - sliderX) / SLIDER_W);
            AudioManager am = AudioManager.getInstance();
            if (dragging == 0) am.setMusicVolume(val);
            else               am.setSFXVolume(val);
        }

        if (justReleased) dragging = -1;
    }

    private void handleKeyboard(float delta) {
        AudioManager am = AudioManager.getInstance();

        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)   || Gdx.input.isKeyJustPressed(Input.Keys.W))
            selected = 0;
        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN) || Gdx.input.isKeyJustPressed(Input.Keys.S))
            selected = 1;

        boolean leftDown  = Gdx.input.isKeyPressed(Input.Keys.LEFT)  || Gdx.input.isKeyPressed(Input.Keys.A);
        boolean rightDown = Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D);

        if (leftDown || rightDown) {
            boolean fire = false;
            if (!holding) {
                fire = true; holding = true; holdTimer = 0f; repeatTimer = 0f;
            } else {
                holdTimer += delta;
                if (holdTimer >= HOLD_DELAY) {
                    repeatTimer += delta;
                    if (repeatTimer >= HOLD_REPEAT) { fire = true; repeatTimer = 0f; }
                }
            }
            if (fire) {
                float dir = leftDown ? -STEP : STEP;
                if (selected == 0) am.setMusicVolume(clamp(am.getMusicVolume() + dir));
                else               am.setSFXVolume  (clamp(am.getSFXVolume()   + dir));
            }
        } else {
            holding = false; holdTimer = 0f; repeatTimer = 0f;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.setScreen(returnScreen);
        }
    }

    private void drawSlider(float value, float x, float y, boolean active) {
        shape.setColor(0.1f, 0.1f, 0.2f, 1f);
        shape.rect(x, y, SLIDER_W, SLIDER_H);
        shape.setColor(active ? new Color(0.3f, 0.7f, 1f, 1f) : new Color(0.3f, 0.3f, 0.55f, 1f));
        shape.rect(x, y, SLIDER_W * value, SLIDER_H);
        float thumbX = x + SLIDER_W * value - 7f;
        shape.setColor(Color.WHITE);
        shape.rect(thumbX, y - 6f, 14f, SLIDER_H + 12f);
    }

    private float clamp(float v) { return Math.max(0f, Math.min(1f, v)); }

    @Override public void resize(int width, int height) {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() { dispose(); }

    @Override
    public void dispose() {
        if (batch      != null) { batch.dispose();      batch      = null; }
        if (shape      != null) { shape.dispose();      shape      = null; }
        if (titleFont  != null) { titleFont.dispose();  titleFont  = null; }
        if (bodyFont   != null) { bodyFont.dispose();   bodyFont   = null; }
        if (background != null) { background.dispose(); background = null; }
    }
}