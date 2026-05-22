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
import com.softchaos.utils.MusicType;

/** Play / Quit texture buttons over background image. → CharacterSelectScreen */
public class MainMenuScreen implements Screen {

    private final SoftChaosGame game;
    private SpriteBatch        batch;
    private OrthographicCamera camera;
    private Texture            background;
    private Texture            playNormal, playHover;
    private Texture            quitNormal, quitHover;
    private Rectangle          playHit, quitHit;

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
        background = load("main_menu.png");
        playNormal = load("play_button.png");
        playHover  = load("active_play_button.png");
        quitNormal = load("quit_button.png");
        quitHover  = load("active_quit_button.png");

        // Hit areas — sized to the normal button display dimensions
        float bw = 320f, bh = 88f;
        float cx = w / 2f, cy = h / 2f;
        playHit = new Rectangle(cx - bw / 2f, cy - 5f,  bw, bh);
        quitHit = new Rectangle(cx - bw / 2f, cy - 115f, bw, bh);

        AudioManager.getInstance().playMusic(MusicType.MENU);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        int   sw = Gdx.graphics.getWidth();
        int   sh = Gdx.graphics.getHeight();
        float mx = Gdx.input.getX();
        float my = sh - Gdx.input.getY();   // flip Y

        boolean overPlay = playHit.contains(mx, my);
        boolean overQuit = quitHit.contains(mx, my);
        boolean clicked  = Gdx.input.justTouched();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        batch.draw(background, 0, 0, sw, sh);
        drawBtn(playNormal, playHover, overPlay, playHit);
        drawBtn(quitNormal, quitHover, overQuit, quitHit);

        batch.end();

        if ((clicked && overPlay) || Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            game.setScreen(new CharacterSelectScreen(game));
        }
        if ((clicked && overQuit) || Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            Gdx.app.exit();
        }

        AudioManager.getInstance().update(delta);
    }

    /**
     * Normal texture is drawn exactly at hit.width × hit.height.
     * Hover texture (which has sparks around the button core) is scaled up so that
     * its core matches the same screen size — computed from the ratio of texture dimensions.
     */
    private void drawBtn(Texture normal, Texture hover, boolean isHover, Rectangle hit) {
        if (!isHover) {
            batch.draw(normal, hit.x, hit.y, hit.width, hit.height);
        } else {
            float scaleW = (float) hover.getWidth()  / normal.getWidth();
            float scaleH = (float) hover.getHeight() / normal.getHeight();
            float drawW  = hit.width  * scaleW;
            float drawH  = hit.height * scaleH;
            batch.draw(hover,
                hit.x + hit.width  / 2f - drawW / 2f,
                hit.y + hit.height / 2f - drawH / 2f,
                drawW, drawH);
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
        if (batch      != null) { batch.dispose();      batch      = null; }
        if (background != null) { background.dispose(); background = null; }
        if (playNormal != null) { playNormal.dispose(); playNormal = null; }
        if (playHover  != null) { playHover.dispose();  playHover  = null; }
        if (quitNormal != null) { quitNormal.dispose(); quitNormal = null; }
        if (quitHover  != null) { quitHover.dispose();  quitHover  = null; }
    }
}