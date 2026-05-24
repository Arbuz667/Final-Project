package com.softchaos.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.softchaos.SoftChaosGame;
import com.softchaos.managers.AudioManager;
import com.softchaos.managers.GameStateManager;
import com.softchaos.utils.MusicType;

/** Victory screen shown after completing all waves. Click / keyboard to navigate. */
public class WinScreen implements Screen {

    private static final float BTN_W = 300f, BTN_H = 84f;

    private final SoftChaosGame game;
    private SpriteBatch        batch;
    private OrthographicCamera camera;

    private Texture background;
    private Texture retryNormal, retryHover;
    private Texture menuNormal,  menuHover;

    private Rectangle retryHit, menuHit;

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

        boolean overRetry = retryHit.contains(mx, my);
        boolean overMenu  = menuHit.contains(mx, my);
        boolean clicked   = Gdx.input.justTouched();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.draw(background, 0, 0, sw, sh);
        drawBtn(retryNormal, retryHover, overRetry, retryHit);
        drawBtn(menuNormal,  menuHover,  overMenu,  menuHit);
        batch.end();

        AudioManager.getInstance().update(delta);

        if ((clicked && overRetry) || Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            GameStateManager.getInstance().reset();
            game.setScreen(new CharacterSelectScreen(game));
        }
        if ((clicked && overMenu) || Gdx.input.isKeyJustPressed(Input.Keys.M)) {
            GameStateManager.getInstance().reset();
            game.setScreen(new MainMenuScreen(game));
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
        if (background  != null) { background.dispose();  background  = null; }
        if (retryNormal != null) { retryNormal.dispose(); retryNormal = null; }
        if (retryHover  != null) { retryHover.dispose();  retryHover  = null; }
        if (menuNormal  != null) { menuNormal.dispose();  menuNormal  = null; }
        if (menuHover   != null) { menuHover.dispose();   menuHover   = null; }
    }
}


