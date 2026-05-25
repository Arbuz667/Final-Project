package com.softchaos.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.softchaos.SoftChaosGame;
import com.softchaos.managers.AudioManager;

/** Music volume slider. ESC → back to previous screen. */
public class SettingsScreen implements Screen {

    private static final float SLIDER_W    = 500f;
    private static final float SLIDER_H    = 22f;
    private static final float STEP        = 0.05f;
    private static final float HOLD_DELAY  = 0.4f;
    private static final float HOLD_REPEAT = 0.08f;
    private static final float PANEL_W     = 620f;
    private static final float PANEL_H     = 280f;

    private final SoftChaosGame game;
    private final Screen        returnScreen;

    private SpriteBatch        batch;
    private ShapeRenderer      shape;
    private BitmapFont         titleFont;
    private BitmapFont         bodyFont;
    private OrthographicCamera camera;
    private Texture            bgSnapshot;
    private GlyphLayout        layout;

    private int     dragging    = -1;
    private boolean holding     = false;
    private float   holdTimer   = 0f;
    private float   repeatTimer = 0f;

    private float sliderX, musicY;

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
        titleFont.getData().setScale(3.0f);
        titleFont.setUseIntegerPositions(false);
        titleFont.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        bodyFont   = new BitmapFont();
        bodyFont.getData().setScale(1.9f);
        bodyFont.setUseIntegerPositions(false);
        bodyFont.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        // Capture game frame as background
        Pixmap pm = Pixmap.createFromFrameBuffer(0, 0, w, h);
        bgSnapshot = new Texture(pm);
        pm.dispose();

        float cx = w / 2f;
        float cy = h / 2f;
        sliderX = cx - SLIDER_W / 2f;
        musicY  = cy;
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

        // Background snapshot
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        if (bgSnapshot != null)
            batch.draw(bgSnapshot, 0, 0, sw, sh, 0, 0, sw, sh, false, true);
        batch.end();

        float cx = sw / 2f;
        float cy = sh / 2f;
        float px = cx - PANEL_W / 2f;
        float py = cy - PANEL_H / 2f;

        // Dark overlay + panel
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shape.setProjectionMatrix(camera.combined);
        shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.setColor(0f, 0f, 0f, 0.75f);
        shape.rect(0, 0, sw, sh);
        shape.setColor(0.07f, 0.07f, 0.15f, 0.97f);
        shape.rect(px, py, PANEL_W, PANEL_H);
        shape.setColor(0.35f, 0.55f, 1f, 1f);
        shape.rect(px, py + PANEL_H - 5f, PANEL_W, 5f);
        // Sliders
        drawSlider(music, sliderX, musicY, true);
        shape.end();
        // Panel border
        shape.begin(ShapeRenderer.ShapeType.Line);
        shape.setColor(0.35f, 0.55f, 1f, 0.45f);
        shape.rect(px, py, PANEL_W, PANEL_H);
        shape.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        // Text
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        titleFont.setColor(Color.WHITE);
        layout.setText(titleFont, "SETTINGS");
        titleFont.draw(batch, "SETTINGS", cx - layout.width / 2f, py + PANEL_H - 22f);

        bodyFont.setColor(Color.YELLOW);
        bodyFont.draw(batch, String.format("Music Volume:  %d%%", Math.round(music * 100)),
            sliderX, musicY + SLIDER_H + 36f);

        batch.end();

        // Input handled after drawing
        handleMouse(mx, my);
        handleKeyboard(delta);
    }

    private void handleMouse(float mx, float my) {
        boolean pressed      = Gdx.input.isTouched();
        boolean justReleased = !pressed && dragging != -1;

        if (Gdx.input.justTouched()) {
            if (new Rectangle(sliderX, musicY - 10f, SLIDER_W, SLIDER_H + 20f).contains(mx, my))
                dragging = 0;
        }

        if (dragging == 0 && pressed)
            AudioManager.getInstance().setMusicVolume(clamp((mx - sliderX) / SLIDER_W));

        if (justReleased) dragging = -1;
    }

    private void handleKeyboard(float delta) {
        AudioManager am = AudioManager.getInstance();

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
                am.setMusicVolume(clamp(am.getMusicVolume() + dir));
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

    @Override public void resize(int width, int height) {
        if (camera != null) camera.setToOrtho(false, width, height);
    }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() { dispose(); }

    @Override
    public void dispose() {
        if (batch      != null) { batch.dispose();      batch      = null; }
        if (shape      != null) { shape.dispose();      shape      = null; }
        if (titleFont  != null) { titleFont.dispose();  titleFont  = null; }
        if (bodyFont   != null) { bodyFont.dispose();   bodyFont   = null; }
        if (bgSnapshot != null) { bgSnapshot.dispose(); bgSnapshot = null; }
    }
}