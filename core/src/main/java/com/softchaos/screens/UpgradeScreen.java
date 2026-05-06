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
import com.badlogic.gdx.utils.Array;
import com.softchaos.SoftChaosGame;
import com.softchaos.config.UpgradeConfig;
import com.softchaos.systems.UpgradeSystem;
import com.softchaos.utils.WeaponRarity;
import com.softchaos.weapons.Weapon;

/** VS-style upgrade card selection screen. */
public class UpgradeScreen implements Screen {

    private static final float CARD_W = 280f;
    private static final float CARD_H = 340f;
    private static final float CARD_GAP = 40f;

    private final SoftChaosGame        game;
    private final Screen               returnScreen;
    private final Weapon               weapon;
    private final UpgradeSystem        upgradeSystem;
    private final Array<UpgradeConfig> choices;

    private SpriteBatch   batch;
    private ShapeRenderer shape;
    private BitmapFont    titleFont;
    private BitmapFont    bodyFont;
    private OrthographicCamera camera;

    public UpgradeScreen(SoftChaosGame game, Weapon weapon,
                         UpgradeSystem upgradeSystem, Screen returnScreen) {
        this.game          = game;
        this.weapon        = weapon;
        this.upgradeSystem = upgradeSystem;
        this.returnScreen  = returnScreen;
        this.choices       = upgradeSystem.rollChoices(weapon);
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
        titleFont.getData().setScale(2.2f);
        bodyFont  = new BitmapFont();
        bodyFont.getData().setScale(1.5f);
    }

    @Override
    public void render(float delta) {
        int w = Gdx.graphics.getWidth();
        int h = Gdx.graphics.getHeight();

        // Dark overlay
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (choices.isEmpty()) { returnToGame(); return; }

        int count = choices.size;
        float totalW = count * CARD_W + (count - 1) * CARD_GAP;
        float startX = (w - totalW) / 2f;
        float cardY  = (h - CARD_H) / 2f;

        shape.setProjectionMatrix(camera.combined);

        // Draw card backgrounds
        shape.begin(ShapeRenderer.ShapeType.Filled);
        for (int i = 0; i < count; i++) {
            UpgradeConfig c = choices.get(i);
            float cx = startX + i * (CARD_W + CARD_GAP);

            // Card shadow
            shape.setColor(0f, 0f, 0f, 0.6f);
            shape.rect(cx + 6, cardY - 6, CARD_W, CARD_H);

            // Card body — dark background
            shape.setColor(0.12f, 0.12f, 0.18f, 1f);
            shape.rect(cx, cardY, CARD_W, CARD_H);

            // Rarity colour bar at top of card
            Color rc = rarityColor(c.rarity);
            shape.setColor(rc);
            shape.rect(cx, cardY + CARD_H - 12f, CARD_W, 12f);

            // Key indicator circle
            shape.setColor(rc);
            shape.circle(cx + CARD_W / 2f, cardY + CARD_H + 30f, 22f, 32);
        }
        shape.end();

        // Card borders
        shape.begin(ShapeRenderer.ShapeType.Line);
        for (int i = 0; i < count; i++) {
            UpgradeConfig c = choices.get(i);
            float cx = startX + i * (CARD_W + CARD_GAP);
            shape.setColor(rarityColor(c.rarity));
            shape.rect(cx, cardY, CARD_W, CARD_H);
        }
        shape.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        // Draw text
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        GlyphLayout layout = new GlyphLayout();

        // Header
        titleFont.setColor(Color.WHITE);
        titleFont.draw(batch, "LEVEL UP!", w / 2f - 80f, cardY + CARD_H + 90f);
        bodyFont.setColor(Color.LIGHT_GRAY);
        bodyFont.draw(batch, "Choose an upgrade for: " + weapon.name, w / 2f - 160f, cardY + CARD_H + 58f);

        for (int i = 0; i < count; i++) {
            UpgradeConfig c = choices.get(i);
            float cx = startX + i * (CARD_W + CARD_GAP);
            String[] parts = c.description.split("\\|");
            String upgradeName = parts.length > 0 ? parts[0] : c.description;
            String statLine    = parts.length > 1 ? parts[1] : "";

            // Key number
            titleFont.setColor(Color.WHITE);
            titleFont.draw(batch, String.valueOf(i + 1), cx + CARD_W / 2f - 8f, cardY + CARD_H + 44f);

            // Rarity label
            bodyFont.setColor(rarityColor(c.rarity));
            layout.setText(bodyFont, c.rarity.name());
            bodyFont.draw(batch, c.rarity.name(), cx + (CARD_W - layout.width) / 2f, cardY + CARD_H - 20f);

            // Upgrade name
            titleFont.setColor(Color.WHITE);
            layout.setText(titleFont, upgradeName);
            float nameX = cx + (CARD_W - layout.width) / 2f;
            titleFont.draw(batch, upgradeName, nameX, cardY + CARD_H - 55f);

            // LVL indicator  e.g.  "LVL 2 / 3"
            int picked = upgradeSystem.getPickedCount(c);
            int max    = upgradeSystem.getMaxLevel(c);
            String lvlStr = "LVL " + picked + " / " + max;
            Color lvlColor = picked == 0
                ? new Color(0.5f, 0.5f, 0.5f, 1f)
                : new Color(0.4f, 1f, 0.4f, 1f);
            bodyFont.setColor(lvlColor);
            layout.setText(bodyFont, lvlStr);
            bodyFont.draw(batch, lvlStr, cx + (CARD_W - layout.width) / 2f, cardY + CARD_H - 78f);

            // Divider hint
            bodyFont.setColor(new Color(0.4f, 0.4f, 0.4f, 1f));
            bodyFont.draw(batch, "- - - - - - - -", cx + 10f, cardY + CARD_H - 98f);

            // Stat line
            bodyFont.setColor(Color.YELLOW);
            layout.setText(bodyFont, statLine);
            bodyFont.draw(batch, statLine, cx + (CARD_W - layout.width) / 2f, cardY + CARD_H - 122f);
        }

        // Footer hint
        bodyFont.setColor(new Color(0.5f, 0.5f, 0.5f, 1f));
        bodyFont.draw(batch, "Press 1 / 2 / 3 to select", w / 2f - 130f, cardY - 30f);

        batch.end();

        // Input
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1) && count > 0) pick(0);
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2) && count > 1) pick(1);
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3) && count > 2) pick(2);
    }

    private void pick(int i) {
        upgradeSystem.applyUpgrade(weapon, choices.get(i));
        returnToGame();
    }

    private void returnToGame() {
        game.setScreen(returnScreen);
    }

    private Color rarityColor(WeaponRarity rarity) {
        if (rarity == null) return Color.WHITE;
        switch (rarity) {
            case RARE:      return new Color(0.2f, 0.6f, 1f, 1f);    // blue
            case EPIC:      return new Color(0.7f, 0.2f, 1f, 1f);    // purple
            case LEGENDARY: return new Color(1f,   0.75f, 0f, 1f);   // gold
            default:        return new Color(0.7f, 0.7f, 0.7f, 1f);  // COMMON grey
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

