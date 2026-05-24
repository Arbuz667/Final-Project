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
import com.softchaos.SoftChaosGame;
import com.softchaos.managers.GameStateManager;
import com.softchaos.weapons.Bow;
import com.softchaos.weapons.Diary;
import com.softchaos.weapons.Sword;
import com.softchaos.weapons.Weapon;

/** Pre-run character selection. Player chooses one of 3 classes, each with a fixed starting weapon. */
public class CharacterSelectScreen implements Screen {

    private static final float CARD_W   = 280f;
    private static final float CARD_H   = 380f;
    private static final float CARD_GAP = 30f;

    // Character definitions
    private static final String[] NAMES  = { "Warrior",    "Archer",    "Mage"       };
    private static final String[] FLAVOR = {
        "Melee fighter.\nSlashes nearby\nenemies.",
        "Agile marksman.\nFast arrows from\na distance.",
        "Spellcaster.\nFireballs with\nAOE explosion."
    };
    // Class accent colors: [R, G, B]
    private static final float[][] COLORS = {
        { 0.95f, 0.25f, 0.25f },  // red   — Swordsman
        { 0.25f, 0.85f, 0.35f },  // green — Archer
        { 0.55f, 0.25f, 0.95f },  // purple— Mage
    };

    private final SoftChaosGame game;
    private final Weapon[]      weapons;

    private OrthographicCamera camera;
    private SpriteBatch        batch;
    private ShapeRenderer      shape;
    private BitmapFont         font;
    private BitmapFont         fontBig;
    private GlyphLayout        layout;
    private Texture            background;

    private int hovered = -1;

    // Character sprite animation
    private Texture[][] charFrames;
    private float animTimer = 0f;
    private int   animFrame = 0;
    private static final float CHAR_FRAME_DUR = 0.25f;

    public CharacterSelectScreen(SoftChaosGame game) {
        this.game    = game;
        this.weapons = new Weapon[]{ new Sword(), new Bow(), new Diary() };
    }

    @Override
    public void show() {
        camera = new OrthographicCamera();
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch      = new SpriteBatch();
        shape      = new ShapeRenderer();
        font       = new BitmapFont();
        fontBig    = new BitmapFont();
        layout     = new GlyphLayout();
        font.getData().setScale(1.5f);
        fontBig.getData().setScale(2.4f);
        background = new Texture(Gdx.files.internal("screens/main_menu.png"));
        background.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        charFrames = new Texture[3][];
        charFrames[0] = new Texture[]{
            new Texture(Gdx.files.internal("characters/Warrior.png")),
            new Texture(Gdx.files.internal("characters/Warrior left leg.png")),
            new Texture(Gdx.files.internal("characters/Warrior\u00a0right leg.png"))
        };
        charFrames[1] = new Texture[]{
            new Texture(Gdx.files.internal("characters/elf straight.png")),
            new Texture(Gdx.files.internal("characters/elf left .png")),
            new Texture(Gdx.files.internal("characters/elf right.png"))
        };
        charFrames[2] = new Texture[]{
            new Texture(Gdx.files.internal("characters/witch.png")),
            new Texture(Gdx.files.internal("characters/withc left.png"))
        };
        for (Texture[] frames : charFrames)
            for (Texture t : frames)
                t.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
    }

    @Override
    public void render(float delta) {
        // Advance animation timer
        animTimer += delta;
        if (animTimer >= CHAR_FRAME_DUR) {
            animTimer -= CHAR_FRAME_DUR;
            animFrame++;
        }

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        camera.update();

        int sw = Gdx.graphics.getWidth();
        int sh = Gdx.graphics.getHeight();

        float totalW = 3 * CARD_W + 2 * CARD_GAP;
        float startX = (sw - totalW) / 2f;
        float cardY  = (sh - CARD_H) / 2f - 30f;

        // Mouse hover detection
        float mx = Gdx.input.getX();
        float my = sh - Gdx.input.getY();
        hovered = -1;
        for (int i = 0; i < 3; i++) {
            float cx = startX + i * (CARD_W + CARD_GAP);
            if (mx >= cx && mx <= cx + CARD_W && my >= cardY && my <= cardY + CARD_H) {
                hovered = i;
                break;
            }
        }

        // Background
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.draw(background, 0, 0, sw, sh);
        batch.end();

        // ── Shape pass ────────────────────────────────────────────────
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shape.setProjectionMatrix(camera.combined);
        shape.begin(ShapeRenderer.ShapeType.Filled);
        for (int i = 0; i < 3; i++) {
            float   cx = startX + i * (CARD_W + CARD_GAP);
            float[] c  = COLORS[i];
            boolean hot = (i == hovered);
            // Card body
            shape.setColor(hot ? 0.10f : 0.06f, hot ? 0.10f : 0.06f, hot ? 0.20f : 0.14f, 0.97f);
            shape.rect(cx, cardY, CARD_W, CARD_H);
            // Class color top stripe
            shape.setColor(c[0], c[1], c[2], 1f);
            shape.rect(cx, cardY + CARD_H - 6f, CARD_W, 6f);
            // Class color left glow
            shape.setColor(c[0], c[1], c[2], hot ? 0.45f : 0.22f);
            shape.rect(cx, cardY, 4f, CARD_H);
        }
        shape.end();

        // Hover outline
        if (hovered >= 0) {
            float   hx = startX + hovered * (CARD_W + CARD_GAP);
            float[] hc = COLORS[hovered];
            shape.begin(ShapeRenderer.ShapeType.Line);
            shape.setColor(hc[0], hc[1], hc[2], 0.9f);
            shape.rect(hx, cardY, CARD_W, CARD_H);
            shape.end();
        }
        Gdx.gl.glDisable(GL20.GL_BLEND);

        // ── Text pass ─────────────────────────────────────────────────
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        // Title
        fontBig.getData().setScale(2.2f);
        fontBig.setColor(Color.WHITE);
        layout.setText(fontBig, "CHOOSE YOUR CHARACTER");
        fontBig.draw(batch, "CHOOSE YOUR CHARACTER", (sw - layout.width) / 2f, sh - 55f);

        for (int i = 0; i < 3; i++) {
            Weapon  w  = weapons[i];
            float   cx = startX + i * (CARD_W + CARD_GAP);
            float[] c  = COLORS[i];

            // Key hint [1] / [2] / [3]
            font.getData().setScale(1.2f);
            font.setColor(0.5f, 0.5f, 0.55f, 1f);
            font.draw(batch, "[" + (i + 1) + "]", cx + 8f, cardY + CARD_H - 12f);

            // Class name (large, centered, class color)
            fontBig.getData().setScale(1.9f);
            fontBig.setColor(c[0], c[1], c[2], 1f);
            layout.setText(fontBig, NAMES[i]);
            fontBig.draw(batch, NAMES[i], cx + (CARD_W - layout.width) / 2f, cardY + CARD_H - 48f);

            // Character sprite (animated)
            Texture[] frames = charFrames[i];
            Texture   frame  = frames[animFrame % frames.length];
            float spriteSize = 110f;
            float spriteX    = cx + (CARD_W - spriteSize) / 2f;
            float spriteY    = cardY + CARD_H - 166f;
            batch.draw(frame, spriteX, spriteY, spriteSize, spriteSize);

            // Divider
            font.getData().setScale(1.0f);
            font.setColor(0.3f, 0.3f, 0.42f, 1f);
            font.draw(batch, "- - - - - - - - - - -", cx + 10f, cardY + CARD_H - 170f);

            // Flavor description (multi-line manual)
            font.getData().setScale(1.15f);
            font.setColor(0.75f, 0.75f, 0.80f, 1f);
            String[] lines = FLAVOR[i].split("\n");
            float lineH = 24f;
            float descY = cardY + CARD_H - 185f;
            for (String line : lines) {
                font.draw(batch, line, cx + 14f, descY);
                descY -= lineH;
            }

            // Starting weapon section
            float weapY = cardY + 128f;
            font.getData().setScale(1.1f);
            font.setColor(0.45f, 0.45f, 0.52f, 1f);
            font.draw(batch, "--- Starting Weapon ---", cx + 6f, weapY);

            fontBig.getData().setScale(1.45f);
            fontBig.setColor(Color.WHITE);
            layout.setText(fontBig, w.name);
            fontBig.draw(batch, w.name, cx + (CARD_W - layout.width) / 2f, weapY - 28f);

            font.getData().setScale(1.3f);
            font.setColor(Color.YELLOW);
            font.draw(batch, String.format("DMG    %.0f",  w.damage),   cx + 16f, weapY - 64f);
            font.setColor(new Color(0.35f, 0.9f, 0.35f, 1f));
            font.draw(batch, String.format("CD     %.2fs", w.cooldown), cx + 16f, weapY - 94f);
        }

        // Footer
        font.getData().setScale(1.25f);
        font.setColor(0.5f, 0.5f, 0.55f, 1f);
        layout.setText(font, "Click or press  1 / 2 / 3  to pick");
        font.draw(batch, "Click or press  1 / 2 / 3  to pick", (sw - layout.width) / 2f, cardY - 24f);

        batch.end();

        // ── Input ─────────────────────────────────────────────────────
        if (Gdx.input.justTouched() && hovered >= 0) pick(hovered);
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) pick(0);
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) pick(1);
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)) pick(2);
    }

    private void pick(int index) {
        GameStateManager gsm = GameStateManager.getInstance();
        gsm.reset();
        gsm.selectedCharacter = index;
        gsm.startingWeapon    = weapons[index];
        game.setScreen(new GameScreen(game));
    }

    @Override public void resize(int width, int height) {
        if (camera != null) camera.setToOrtho(false, width, height);
    }
    @Override public void pause() {}
    @Override public void resume() {}

    @Override
    public void hide() {
        dispose();
    }

    @Override
    public void dispose() {
        if (batch      != null) { batch.dispose();      batch      = null; }
        if (shape      != null) { shape.dispose();      shape      = null; }
        if (font       != null) { font.dispose();       font       = null; }
        if (fontBig    != null) { fontBig.dispose();    fontBig    = null; }
        if (background != null) { background.dispose(); background = null; }
        if (charFrames != null) {
            for (Texture[] frames : charFrames)
                for (Texture t : frames) if (t != null) t.dispose();
            charFrames = null;
        }
    }
}
