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
import com.softchaos.entities.Chest;
import com.softchaos.entities.Player;
import com.softchaos.systems.ChestSystem;
import com.softchaos.utils.WeaponRarity;
import com.softchaos.weapons.Bow;
import com.softchaos.weapons.Diary;
import com.softchaos.weapons.DesertEagles;
import com.softchaos.weapons.Minigun;
import com.softchaos.weapons.NuclearBazooka;
import com.softchaos.weapons.PotatoThrower;
import com.softchaos.weapons.Shurikens;
import com.softchaos.weapons.Sword;
import com.softchaos.weapons.Weapon;

/**
 * Mid-run weapon selection screen shown when a chest is opened.
 * Shows up to 3 weapons the player doesn't already own.
 * ESC skips without taking anything.
 */
public class ChestScreen implements Screen {

    private static final int   CHOICES  = 3;
    private static final float CARD_W   = 280f;
    private static final float CARD_H   = 340f;
    private static final float CARD_GAP = 40f;

    private final SoftChaosGame  game;
    private final Screen         returnScreen;
    private final Player         player;
    private final ChestSystem    chestSystem;
    private final Chest          chest;
    private final Array<Weapon>  choices;

    private SpriteBatch        batch;
    private ShapeRenderer      shape;
    private BitmapFont         titleFont;
    private BitmapFont         bodyFont;
    private OrthographicCamera camera;
    private Texture[]          choiceIcons;

    public ChestScreen(SoftChaosGame game, Chest chest, Player player,
                       ChestSystem chestSystem, Screen returnScreen) {
        this.game         = game;
        this.chest        = chest;
        this.player       = player;
        this.chestSystem  = chestSystem;
        this.returnScreen = returnScreen;
        this.choices      = buildChoices();
    }

    /** Build offer: all weapons the player doesn't own yet, shuffled and capped at CHOICES. */
    private Array<Weapon> buildChoices() {
        Array<Weapon> pool = new Array<>();
        pool.add(new Sword());
        pool.add(new Bow());
        pool.add(new Diary());
        pool.add(new DesertEagles());
        pool.add(new Shurikens());
        pool.add(new Minigun());
        pool.add(new PotatoThrower());
        pool.add(new NuclearBazooka());

        // Remove weapons the player already owns
        for (int i = pool.size - 1; i >= 0; i--) {
            String poolId = pool.get(i).id;
            for (Weapon owned : player.weapons) {
                if (owned.id.equals(poolId)) {
                    pool.removeIndex(i);
                    break;
                }
            }
        }

        pool.shuffle();
        if (pool.size > CHOICES) pool.removeRange(CHOICES, pool.size - 1);
        return pool;
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

        // Load icon for each weapon choice
        choiceIcons = new Texture[choices.size];
        for (int i = 0; i < choices.size; i++) {
            choiceIcons[i] = new Texture(Gdx.files.internal(choices.get(i).iconPath));
            choiceIcons[i].setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        }
    }

    @Override
    public void render(float delta) {
        int w = Gdx.graphics.getWidth();
        int h = Gdx.graphics.getHeight();

        // Mouse state
        float mx      = Gdx.input.getX();
        float my      = h - Gdx.input.getY();
        boolean clicked = Gdx.input.justTouched();

        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (choices.isEmpty()) {
            returnToGame();
            return;
        }

        int count = choices.size;
        float totalW = count * CARD_W + (count - 1) * CARD_GAP;
        float startX = (w - totalW) / 2f;
        float cardY  = (h - CARD_H) / 2f;

        // Detect hovered card
        int hoveredCard = -1;
        for (int i = 0; i < count; i++) {
            float cx = startX + i * (CARD_W + CARD_GAP);
            if (mx >= cx && mx <= cx + CARD_W && my >= cardY && my <= cardY + CARD_H)
                hoveredCard = i;
        }

        // Skip button geometry
        float skipW = 200f, skipH = 46f;
        float skipX = (w - skipW) / 2f;
        float skipY = cardY - 78f;
        boolean overSkip = mx >= skipX && mx <= skipX + skipW && my >= skipY && my <= skipY + skipH;

        shape.setProjectionMatrix(camera.combined);

        // --- Card fill ---
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shape.begin(ShapeRenderer.ShapeType.Filled);
        for (int i = 0; i < count; i++) {
            Weapon c  = choices.get(i);
            float  cx = startX + i * (CARD_W + CARD_GAP);

            shape.setColor(0f, 0f, 0f, 0.6f);
            shape.rect(cx + 6, cardY - 6, CARD_W, CARD_H);

            shape.setColor(0.12f, 0.12f, 0.18f, 1f);
            shape.rect(cx, cardY, CARD_W, CARD_H);

            // Hover overlay
            if (i == hoveredCard) {
                shape.setColor(1f, 1f, 1f, 0.07f);
                shape.rect(cx, cardY, CARD_W, CARD_H);
            }

            Color rc = rarityColor(c.rarity);
            shape.setColor(rc);
            shape.rect(cx, cardY + CARD_H - 12f, CARD_W, 12f);

            shape.setColor(rc);
            shape.circle(cx + CARD_W / 2f, cardY + CARD_H + 30f, 22f, 32);
        }

        // Skip button
        shape.setColor(overSkip ? new Color(0.75f, 0.25f, 0.25f, 0.9f)
                                : new Color(0.2f,  0.2f,  0.25f, 0.8f));
        shape.rect(skipX, skipY, skipW, skipH);
        shape.end();

        // --- Card borders ---
        shape.begin(ShapeRenderer.ShapeType.Line);
        for (int i = 0; i < count; i++) {
            Weapon c  = choices.get(i);
            float  cx = startX + i * (CARD_W + CARD_GAP);
            shape.setColor(i == hoveredCard ? Color.WHITE : rarityColor(c.rarity));
            shape.rect(cx, cardY, CARD_W, CARD_H);
        }
        shape.setColor(overSkip ? Color.WHITE : new Color(0.5f, 0.5f, 0.5f, 1f));
        shape.rect(skipX, skipY, skipW, skipH);
        shape.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        // --- Text ---
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        GlyphLayout layout = new GlyphLayout();

        titleFont.setColor(Color.GOLD);
        titleFont.draw(batch, "CHEST OPENED!", w / 2f - 110f, cardY + CARD_H + 90f);
        bodyFont.setColor(Color.LIGHT_GRAY);
        bodyFont.draw(batch, "Choose a new weapon", w / 2f - 120f, cardY + CARD_H + 58f);

        for (int i = 0; i < count; i++) {
            Weapon c  = choices.get(i);
            float  cx = startX + i * (CARD_W + CARD_GAP);

            titleFont.setColor(Color.WHITE);
            titleFont.draw(batch, String.valueOf(i + 1), cx + CARD_W / 2f - 8f, cardY + CARD_H + 44f);

            String rarityName = c.rarity != null ? c.rarity.name() : "COMMON";
            bodyFont.setColor(rarityColor(c.rarity));
            layout.setText(bodyFont, rarityName);
            bodyFont.draw(batch, rarityName, cx + (CARD_W - layout.width) / 2f, cardY + CARD_H - 20f);

            titleFont.setColor(Color.WHITE);
            layout.setText(titleFont, c.name);
            titleFont.draw(batch, c.name, cx + (CARD_W - layout.width) / 2f, cardY + CARD_H - 55f);

            // Weapon icon in lower half of card
            if (i < choiceIcons.length && choiceIcons[i] != null) {
                float iconSz = 140f;
                batch.draw(choiceIcons[i],
                    cx + (CARD_W - iconSz) / 2f, cardY + 40f, iconSz, iconSz);
            }

            bodyFont.setColor(new Color(0.4f, 0.4f, 0.4f, 1f));
            bodyFont.draw(batch, "- - - - - - - -", cx + 10f, cardY + CARD_H - 98f);

            String stats = String.format("DMG %.0f   CD %.1fs   x%d proj",
                c.damage, c.cooldown, c.projectileCount);
            bodyFont.setColor(Color.YELLOW);
            layout.setText(bodyFont, stats);
            bodyFont.draw(batch, stats, cx + (CARD_W - layout.width) / 2f, cardY + CARD_H - 122f);
        }

        // Skip button text
        bodyFont.setColor(Color.WHITE);
        layout.setText(bodyFont, "SKIP");
        bodyFont.draw(batch, "SKIP", skipX + (skipW - layout.width) / 2f, skipY + skipH - 10f);

        bodyFont.setColor(new Color(0.5f, 0.5f, 0.5f, 1f));
        bodyFont.draw(batch, "1 / 2 / 3  or click  |  ESC or Skip to cancel", w / 2f - 185f, cardY - 30f);

        batch.end();

        // Input — keyboard
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1) && count > 0) pick(0);
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2) && count > 1) pick(1);
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3) && count > 2) pick(2);
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE))             returnToGame();

        // Input — mouse
        if (clicked) {
            if (hoveredCard >= 0 && hoveredCard < count) pick(hoveredCard);
            else if (overSkip)                           returnToGame();
        }
    }

    private void pick(int i) {
        player.weapons.add(choices.get(i));
        returnToGame();
    }

    private void returnToGame() {
        chestSystem.freeChest(chest);
        game.setScreen(returnScreen);
    }

    private Color rarityColor(WeaponRarity rarity) {
        if (rarity == null) return Color.WHITE;
        switch (rarity) {
            case RARE:      return new Color(0.2f, 0.6f, 1f, 1f);
            case EPIC:      return new Color(0.7f, 0.2f, 1f, 1f);
            case LEGENDARY: return new Color(1f, 0.75f, 0f, 1f);
            default:        return new Color(0.7f, 0.7f, 0.7f, 1f);
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
        if (choiceIcons != null) {
            for (Texture t : choiceIcons) if (t != null) t.dispose();
            choiceIcons = null;
        }
    }
}
