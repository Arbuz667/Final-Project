package com.softchaos.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.softchaos.SoftChaosGame;
import com.softchaos.managers.AudioManager;
import com.softchaos.managers.GameStateManager;
import com.softchaos.utils.MusicType;

/** Victory screen shown after completing all waves. Click / keyboard to navigate. */
public class WinScreen implements Screen {

    private static final float BTN_W = 300f, BTN_H = 84f;
    private static final float FADE_SPEED = 1.8f;

    private final SoftChaosGame game;
    private SpriteBatch        batch;
    private ShapeRenderer      shape;
    private OrthographicCamera camera;

    private Texture background;
    private Texture retryNormal, retryHover;
    private Texture menuNormal,  menuHover;

    private Rectangle retryHit, menuHit;

    private float    fadeAlpha = 1f;   // 1 = black; fades to 0 on show
    private boolean  fadingOut = false;
    private Runnable pendingTransition;

    public WinScreen(SoftChaosGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        int w = Gdx.graphics.getWidth();
        int h = Gdx.graphics.getHeight();
        camera = new OrthographicCamera();
        camera.setToOrtho(false, w, h);

        batch = new SpriteBatch();
        shape = new ShapeRenderer();

        background  = load("screens/victory.png");
        retryNormal = load("buttons/winner button retry.png");
        retryHover  = load("buttons/winner button light retry.png");
        menuNormal  = load("buttons/winner buttons menu.png");
        menuHover   = load("buttons/winner button light menu.png");

        float cx = w / 2f, cy = h / 2f;
        retryHit = new Rectangle(cx - BTN_W / 2f, cy - 140f, BTN_W, BTN_H);
        menuHit  = new Rectangle(cx - BTN_W / 2f, cy - 240f, BTN_W, BTN_H);

        AudioManager.getInstance().playMusic(MusicType.WIN);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        int   sw = Gdx.graphics.getWidth();
        int   sh = Gdx.graphics.getHeight();
        float mx = Gdx.input.getX();
        float my = sh - Gdx.input.getY();

        // Update fade
        if (fadingOut) {
            fadeAlpha = Math.min(1f, fadeAlpha + FADE_SPEED * delta);
            if (fadeAlpha >= 1f && pendingTransition != null) {
                Runnable r = pendingTransition;
                pendingTransition = null;
                r.run();
                return;
            }
        } else {
            fadeAlpha = Math.max(0f, fadeAlpha - FADE_SPEED * delta);
        }

        boolean overRetry = !fadingOut && retryHit.contains(mx, my);
        boolean overMenu  = !fadingOut && menuHit.contains(mx, my);
        boolean clicked   = !fadingOut && Gdx.input.justTouched();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.draw(background, 0, 0, sw, sh);
        drawBtn(retryNormal, retryHover, overRetry, retryHit);
        drawBtn(menuNormal,  menuHover,  overMenu,  menuHit);
        batch.end();

        // Black fade overlay
        if (fadeAlpha > 0f) {
            Gdx.gl.glEnable(GL20.GL_BLEND);
            shape.setProjectionMatrix(camera.combined);
            shape.begin(ShapeRenderer.ShapeType.Filled);
            shape.setColor(0f, 0f, 0f, fadeAlpha);
            shape.rect(0, 0, sw, sh);
            shape.end();
            Gdx.gl.glDisable(GL20.GL_BLEND);
        }

        AudioManager.getInstance().update(delta);

        if (!fadingOut) {
            if ((clicked && overRetry) || Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                fadingOut = true;
                pendingTransition = () -> {
                    GameStateManager.getInstance().reset();
                    game.setScreen(new CharacterSelectScreen(game));
                };
            }
            if ((clicked && overMenu) || Gdx.input.isKeyJustPressed(Input.Keys.M)) {
                fadingOut = true;
                pendingTransition = () -> {
                    GameStateManager.getInstance().reset();
                    game.setScreen(new MainMenuScreen(game));
                };
            }
        }
    }

    private void drawBtn(Texture normal, Texture hover, boolean isHover, Rectangle hit) {
        if (!isHover) {
            batch.draw(normal, hit.x, hit.y, hit.width, hit.height);
        } else {
            float drawH = hit.height * ((float) hover.getHeight() / normal.getHeight());
            batch.draw(hover,
                hit.x,
                hit.y + hit.height / 2f - drawH / 2f,
                hit.width, drawH);
        }
    }

    private Texture load(String path) {
        Texture t = new Texture(Gdx.files.internal(path));
        t.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        return t;
    }

    @Override public void resize(int width, int height) {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() { dispose(); }

    @Override
    public void dispose() {
        if (batch       != null) { batch.dispose();       batch       = null; }
        if (shape       != null) { shape.dispose();       shape       = null; }
        if (background  != null) { background.dispose();  background  = null; }
        if (retryNormal != null) { retryNormal.dispose(); retryNormal = null; }
        if (retryHover  != null) { retryHover.dispose();  retryHover  = null; }
        if (menuNormal  != null) { menuNormal.dispose();  menuNormal  = null; }
        if (menuHover   != null) { menuHover.dispose();   menuHover   = null; }
    }
}


