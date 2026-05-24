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
import com.badlogic.gdx.utils.Array;
import com.softchaos.SoftChaosGame;
import com.softchaos.managers.GameStateManager;
import com.softchaos.utils.WeaponRarity;
import com.softchaos.weapons.Bow;
import com.softchaos.weapons.Diary;
import com.softchaos.weapons.DesertEagles;
import com.softchaos.weapons.Minigun;
import com.softchaos.weapons.Shurikens;
import com.softchaos.weapons.Sword;
import com.softchaos.weapons.Weapon;

/** VS-style weapon selection screen shown before the run starts. Displays 3 random weapon cards. */
public class WeaponSelectScreen implements Screen {

    private static final int   CHOICES  = 3;
    private static final float CARD_W   = 280f;
    private static final float CARD_H   = 360f;
    private static final float CARD_GAP = 30f;

    private final SoftChaosGame game;
    private OrthographicCamera  camera;
    private SpriteBatch         batch;
    private ShapeRenderer       shape;
    private BitmapFont          font;
    private BitmapFont          fontBig;
    private GlyphLayout         layout;
    private Texture             background;

    private final Array<Weapon> choices = new Array<>(CHOICES);

    public WeaponSelectScreen(SoftChaosGame game) {
        this.game = game;
        buildChoices();
    }

    private void buildChoices() {
        Array<Weapon> pool = new Array<>();
        pool.add(new Sword());
        pool.add(new Bow());
        pool.add(new Diary());
        pool.add(new DesertEagles());
        pool.add(new Shurikens());
        pool.add(new Minigun());
        pool.shuffle();
        for (int i = 0; i < CHOICES && i < pool.size; i++) {
            choices.add(pool.get(i));
        }
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
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        camera.update();

        // Draw menu background
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.draw(background, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.end();
        batch.setProjectionMatrix(camera.combined);
        int sw = Gdx.graphics.getWidth();
        int sh = Gdx.graphics.getHeight();

        float totalW = CHOICES * CARD_W + (CHOICES - 1) * CARD_GAP;
        float startX = (sw - totalW) / 2f;
        float cardY  = (sh - CARD_H) / 2f - 30f;

        // ── Shape pass: card backgrounds ──────────────────────────────
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shape.setProjectionMatrix(camera.combined);
        shape.begin(ShapeRenderer.ShapeType.Filled);
        for (int i = 0; i < choices.size; i++) {
            Weapon  w  = choices.get(i);
            float   cx = startX + i * (CARD_W + CARD_GAP);
            float[] rc = rarityRGB(w.rarity);
            // Card body
            shape.setColor(0.06f, 0.06f, 0.14f, 0.96f);
            shape.rect(cx, cardY, CARD_W, CARD_H);
            // Rarity top stripe
            shape.setColor(rc[0], rc[1], rc[2], 1f);
            shape.rect(cx, cardY + CARD_H - 6f, CARD_W, 6f);
            // Rarity left glow
            shape.setColor(rc[0], rc[1], rc[2], 0.22f);
            shape.rect(cx, cardY, 4f, CARD_H);
        }
        shape.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        // ── Text pass ─────────────────────────────────────────────────
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        // Title
        fontBig.getData().setScale(2.2f);
        fontBig.setColor(Color.WHITE);
        layout.setText(fontBig, "CHOOSE YOUR WEAPON");
        fontBig.draw(batch, "CHOOSE YOUR WEAPON", (sw - layout.width) / 2f, sh - 55f);

        for (int i = 0; i < choices.size; i++) {
            Weapon  w  = choices.get(i);
            float   cx = startX + i * (CARD_W + CARD_GAP);
            float[] rc = rarityRGB(w.rarity);

            // Key hint [1] / [2] / [3]
            font.getData().setScale(1.2f);
            font.setColor(0.5f, 0.5f, 0.55f, 1f);
            font.draw(batch, "[" + (i + 1) + "]", cx + 8f, cardY + CARD_H - 12f);

            // Rarity label (top-right)
            font.getData().setScale(1.15f);
            font.setColor(rc[0], rc[1], rc[2], 1f);
            String rName = w.rarity != null ? w.rarity.name() : "COMMON";
            layout.setText(font, rName);
            font.draw(batch, rName, cx + CARD_W - layout.width - 8f, cardY + CARD_H - 12f);

            // Weapon name (centered)
            fontBig.getData().setScale(1.85f);
            fontBig.setColor(Color.WHITE);
            layout.setText(fontBig, w.name);
            fontBig.draw(batch, w.name, cx + (CARD_W - layout.width) / 2f, cardY + CARD_H - 48f);

            // Divider
            font.getData().setScale(1.0f);
            font.setColor(0.3f, 0.3f, 0.42f, 1f);
            font.draw(batch, "- - - - - - - - - - -", cx + 10f, cardY + CARD_H - 104f);

            // Stats
            font.getData().setScale(1.3f);
            font.setColor(Color.YELLOW);
            font.draw(batch, String.format("DMG    %.0f",  w.damage),   cx + 16f, cardY + CARD_H - 132f);
            font.setColor(new Color(0.35f, 0.9f, 0.35f, 1f));
            font.draw(batch, String.format("CD     %.2fs", w.cooldown), cx + 16f, cardY + CARD_H - 165f);
            if (w.projectileCount > 1) {
                font.setColor(Color.CYAN);
                font.draw(batch, "PROJ   " + w.projectileCount, cx + 16f, cardY + CARD_H - 198f);
            }

            // Short description (bottom of card)
            font.getData().setScale(1.1f);
            font.setColor(0.7f, 0.7f, 0.75f, 1f);
            font.draw(batch, getDescription(w), cx + 12f, cardY + 68f);
        }

        // Footer hint
        font.getData().setScale(1.25f);
        font.setColor(0.5f, 0.5f, 0.55f, 1f);
        layout.setText(font, "Press  1 / 2 / 3  to pick");
        font.draw(batch, "Press  1 / 2 / 3  to pick", (sw - layout.width) / 2f, cardY - 24f);

        batch.end();

        // ── Input ─────────────────────────────────────────────────────
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1) && choices.size > 0) pick(0);
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2) && choices.size > 1) pick(1);
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3) && choices.size > 2) pick(2);
    }

    private void pick(int index) {
        GameStateManager.getInstance().startingWeapon = choices.get(index);
        game.setScreen(new GameScreen(game));
    }

    private String getDescription(Weapon w) {
        switch (w.id) {
            case "sword":         return "Melee. Hits nearby enemies.";
            case "iron_bow":      return "Ranged. 1 fast arrow.";
            case "desert_eagles": return "Ranged. 2 bullets, spread.";
            case "shurikens":     return "Ranged. 3 projectiles/shot.";
            case "diary":         return "Magic. Slow fireball with AOE.";
            case "minigun":       return "Rapid fire. Low damage.";
            default:              return "";
        }
    }

    private float[] rarityRGB(WeaponRarity r) {
        if (r == null) return new float[]{0.6f, 0.6f, 0.6f};
        switch (r) {
            case RARE:      return new float[]{0.3f, 0.6f, 1.0f};
            case EPIC:      return new float[]{0.7f, 0.3f, 1.0f};
            case LEGENDARY: return new float[]{1.0f, 0.8f, 0.1f};
            default:        return new float[]{0.65f, 0.65f, 0.65f}; // COMMON
        }
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
    }
}
