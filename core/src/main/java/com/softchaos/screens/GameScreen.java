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
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;
import com.softchaos.SoftChaosGame;
import com.softchaos.entities.Chest;
import com.softchaos.entities.Enemy;
import com.softchaos.entities.Player;
import com.softchaos.entities.Projectile;
import com.softchaos.managers.AudioManager;
import com.softchaos.managers.GameStateManager;
import com.softchaos.managers.UIManager;
import com.softchaos.systems.ChestSystem;
import com.softchaos.systems.EnemySpawner;
import com.softchaos.systems.UpgradeSystem;
import com.softchaos.systems.WaveManager;
import com.softchaos.systems.XPSystem;
import com.softchaos.utils.Constants;
import com.softchaos.utils.LocationType;
import com.softchaos.utils.MusicType;
import com.softchaos.utils.WeaponRarity;
import com.softchaos.weapons.Sword;
import com.softchaos.weapons.Weapon;
import com.softchaos.weapons.WeaponSystem;

/** Main gameplay screen. update() + draw() loop for all systems. */
public class GameScreen implements Screen,
    WaveManager.WaveListener,
    XPSystem.XPListener,
    ChestSystem.ChestListener {

    private final SoftChaosGame game;
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private OrthographicCamera camera;

    private Player player;
    private Array<Enemy> enemies;
    private Array<Projectile> projectiles;

    private Texture playerTexture;
    private Texture backgroundTexture;

    private WeaponSystem   weaponSystem;
    private WaveManager    waveManager;
    private EnemySpawner   enemySpawner;
    private XPSystem       xpSystem;
    private UpgradeSystem  upgradeSystem;
    private ChestSystem    chestSystem;
    private UIManager      uiManager;

    // HUD
    private OrthographicCamera hudCamera;
    private BitmapFont         hudFont;
    private BitmapFont         hudFontBig;
    private GlyphLayout        glyphLayout;

    // Floating damage numbers
    private Array<DamageNumber> damageNumbers;

    // Location-transition fade
    private float    fadeAlpha        = 0f;
    private boolean  fadingOut        = false;
    private Runnable pendingTransition;

    private static class DamageNumber {
        float x, y, alpha;
        String text;
        DamageNumber(float worldX, float worldY, float dmg) {
            // Convert world units → screen pixels so we draw with hudCamera
            this.x     = worldX * Constants.PPM;
            this.y     = worldY * Constants.PPM + 24f;
            this.alpha = 1f;
            this.text  = String.valueOf((int) dmg);
        }
    }

    /**
     * Start a fresh run from the current location.
     */
    public GameScreen(SoftChaosGame game) {
        this(game, null, null, null);
    }

    /**
     * Advance to the next location, carrying player + progression systems over.
     * Pass {@code null} for fresh-start defaults.
     */
    public GameScreen(SoftChaosGame game, Player existingPlayer,
                      XPSystem existingXpSystem, UpgradeSystem existingUpgradeSystem) {
        this.game = game;

        float worldW = Constants.SCREEN_WIDTH  / Constants.PPM;
        float worldH = Constants.SCREEN_HEIGHT / Constants.PPM;

        camera = new OrthographicCamera();
        camera.setToOrtho(false, worldW, worldH);

        batch         = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        playerTexture = new Texture(Gdx.files.internal("player.png"));

        // Load full-screen background for current location
        LocationType loc = GameStateManager.getInstance().currentLocation;
        if (loc == null) {
            loc = LocationType.FOREST;
            GameStateManager.getInstance().currentLocation = loc;
        }
        backgroundTexture = new Texture(Gdx.files.internal(loc.backgroundFile()));

        hudCamera = new OrthographicCamera();
        hudCamera.setToOrtho(false, Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);
        hudFont   = new BitmapFont();
        hudFont.getData().setScale(1.4f);
        hudFontBig = new BitmapFont();
        hudFontBig.getData().setScale(2.8f);
        glyphLayout   = new GlyphLayout();
        damageNumbers = new Array<>();

        enemies       = new Array<>();
        projectiles   = new Array<>();

        if (existingPlayer != null) {
            player   = existingPlayer;
            // Reset position to world centre for the new location
            player.x = worldW / 2f;
            player.y = worldH / 2f;
        } else {
            player = new Player();
            Weapon start = GameStateManager.getInstance().startingWeapon;
            player.weapons.add(start != null ? start : new Sword());
        }

        weaponSystem  = new WeaponSystem();
        enemySpawner  = new EnemySpawner(enemies);
        waveManager   = new WaveManager(enemySpawner);

        // Carry over level + upgrade progress across locations, or start fresh
        if (existingXpSystem != null) {
            xpSystem = existingXpSystem;
        } else {
            xpSystem = new XPSystem();
        }
        xpSystem.setListener(this);

        if (existingUpgradeSystem != null) {
            upgradeSystem = existingUpgradeSystem;
        } else {
            upgradeSystem = new UpgradeSystem(weaponSystem);
        }

        chestSystem   = new ChestSystem();
        uiManager     = new UIManager(player, waveManager, xpSystem);

        waveManager.setListener(this);
        chestSystem.setListener(this);

        waveManager.startWave(loc, 120f);

        AudioManager.getInstance().playMusic(MusicType.valueOf(loc.name()));
    }

    /** Called by libGDX each time this screen becomes active. Does NOT reinitialize. */
    @Override
    public void show() {
        // Intentionally empty — all initialization is done in the constructor.
        // This prevents state reset when returning from UpgradeScreen/ChestScreen.
    }

    @Override
    public void render(float delta) {
        update(delta);
        draw();
    }

    private void update(float delta) {
        // TAB — instant return to main menu
        if (Gdx.input.isKeyJustPressed(Input.Keys.TAB)) {
            GameStateManager.getInstance().reset();
            game.setScreen(new MainMenuScreen(game));
            return;
        }

        // Freeze gameplay during fade-out; only advance the fade timer
        if (fadingOut) {
            fadeAlpha = Math.min(1f, fadeAlpha + delta * 1.8f);
            if (fadeAlpha >= 1f && pendingTransition != null) {
                Runnable r = pendingTransition;
                pendingTransition = null;
                r.run();
            }
            return;
        }

        player.update(delta);
        waveManager.update(delta);
        weaponSystem.update(delta, player, enemies, projectiles);

        // Track session time
        GameStateManager.getInstance().sessionTime += delta;

        // Update enemies
        for (int i = enemies.size - 1; i >= 0; i--) {
            Enemy e = enemies.get(i);
            e.update(delta, player);
            if (e.isDead()) {
                xpSystem.addXP(e.xpDrop);
                GameStateManager.getInstance().kills++;
                // TODO M3: roll chest drop
                enemySpawner.freeEnemy(e);
                i--;
                continue;
            }
            // Enemy touches player → deal damage
            if (e.hitbox.overlaps(player.hitbox)) {
                player.takeDamage(e.damage * delta);
            }
        }

        // Update projectiles + collision
        for (int i = projectiles.size - 1; i >= 0; i--) {
            Projectile p = projectiles.get(i);
            p.update(delta);
            if (!p.active) { projectiles.removeIndex(i); continue; }

            // Collision with enemies
            for (int ei = 0; ei < enemies.size; ei++) {
                Enemy e = enemies.get(ei);
                // Skip already-visited enemies for bouncing projectiles
                if (p.bouncesLeft > 0 && p.visitedEnemies.contains(e, true)) continue;
                if (!p.hitbox.overlaps(e.hitbox)) continue;

                float hpBefore = e.hp;
                p.onHit(e);
                float dealt = hpBefore - e.hp;
                if (dealt > 0) damageNumbers.add(new DamageNumber(e.x, e.y, dealt));

                // AOE explosion: damage all enemies within explosionRadius
                if (p.explosionRadius > 0) {
                    float r2 = p.explosionRadius * p.explosionRadius;
                    boolean isKnockback = p.source == com.softchaos.utils.WeaponType.POTATO_THROWER;
                    // Also apply knockback to the directly-hit enemy
                    if (isKnockback) {
                        float edx = e.x - p.x, edy = e.y - p.y;
                        float elen = (float) Math.sqrt(edx * edx + edy * edy);
                        if (elen > 0) e.applyKnockback(
                            (edx / elen) * com.softchaos.weapons.PotatoThrower.KNOCKBACK_FORCE,
                            (edy / elen) * com.softchaos.weapons.PotatoThrower.KNOCKBACK_FORCE,
                            com.softchaos.weapons.PotatoThrower.KNOCKBACK_DURATION);
                    }
                    for (int ai = 0; ai < enemies.size; ai++) {
                        Enemy ae = enemies.get(ai);
                        if (ae == e) continue;
                        float ddx = ae.x - p.x;
                        float ddy = ae.y - p.y;
                        float dist2 = ddx * ddx + ddy * ddy;
                        if (dist2 <= r2) {
                            float hpA = ae.hp;
                            ae.takeDamage(p.damage);
                            float dealtA = hpA - ae.hp;
                            if (dealtA > 0) damageNumbers.add(new DamageNumber(ae.x, ae.y, dealtA));
                            if (isKnockback) {
                                float dist = (float) Math.sqrt(dist2);
                                ae.applyKnockback(
                                    (ddx / dist) * com.softchaos.weapons.PotatoThrower.KNOCKBACK_FORCE,
                                    (ddy / dist) * com.softchaos.weapons.PotatoThrower.KNOCKBACK_FORCE,
                                    com.softchaos.weapons.PotatoThrower.KNOCKBACK_DURATION);
                            }
                        }
                    }
                    break;
                }

                // Bounce logic: redirect to nearest unvisited enemy
                if (p.bouncesLeft > 0) {
                    p.visitedEnemies.add(e);
                    Enemy next = findNearestBounceTarget(p, enemies);
                    if (next != null) {
                        float speed = (float) Math.sqrt(p.velX * p.velX + p.velY * p.velY);
                        p.setVelocityToward(next.x, next.y, speed);
                        p.bouncesLeft--;
                        p.active = true; // keep alive
                    } else {
                        p.active = false;
                    }
                }

                if (!p.active) break;
            }
        }

        // Update floating damage numbers
        for (int i = damageNumbers.size - 1; i >= 0; i--) {
            DamageNumber n = damageNumbers.get(i);
            n.y     += 55f * delta;
            n.alpha -= 1.8f * delta;
            if (n.alpha <= 0) damageNumbers.removeIndex(i);
        }

        // Check player death
        if (player.isDead()) {
            game.setScreen(new GameOverScreen(game));
        }

        AudioManager.getInstance().update(delta);
        uiManager.update();
    }

    private void draw() {
        // Clear screen
        Gdx.gl.glClearColor(0.08f, 0.08f, 0.08f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();

        // 1. Background — single full-screen PNG stretched to world size
        float worldW = Constants.SCREEN_WIDTH  / Constants.PPM;
        float worldH = Constants.SCREEN_HEIGHT / Constants.PPM;
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.draw(backgroundTexture, 0, 0, worldW, worldH);
        batch.end();

        // 2. Enemies + HP bars + projectiles
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Enemy bodies
        shapeRenderer.setColor(0.85f, 0.15f, 0.15f, 1f);
        for (Enemy e : enemies) {
            shapeRenderer.rect(e.x - 0.4f, e.y - 0.4f, 0.8f, 0.8f);
        }

        // Enemy HP bars (world coords, small bar above each enemy)
        float hpBarW = 0.8f, hpBarHt = 0.07f;
        for (Enemy e : enemies) {
            float frac = Math.max(0, e.hp / e.maxHp);
            float bx   = e.x - hpBarW / 2f;
            float by   = e.y + 0.52f;
            shapeRenderer.setColor(0.25f, 0f, 0f, 0.9f);
            shapeRenderer.rect(bx, by, hpBarW, hpBarHt);
            shapeRenderer.setColor(0.9f, 0.12f, 0.12f, 1f);
            shapeRenderer.rect(bx, by, hpBarW * frac, hpBarHt);
        }

        // Projectiles
        shapeRenderer.setColor(Color.YELLOW);
        for (Projectile p : projectiles) {
            float h = p.size / 2f;
            shapeRenderer.rect(p.x - h, p.y - h, p.size, p.size);
        }
        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        // 3. Player sprite + UI
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.draw(playerTexture, player.x - 1.046875f, player.y - 1.046875f, 2.09375f, 2.09375f);
        uiManager.render(batch);
        batch.end();

        // 4. HUD (XP bar) in screen pixels
        drawHud();

        // 5. Fade-to-black overlay (location transition)
        if (fadeAlpha > 0f) {
            Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            shapeRenderer.setProjectionMatrix(hudCamera.combined);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(0f, 0f, 0f, fadeAlpha);
            shapeRenderer.rect(0, 0, Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);
            shapeRenderer.end();
            Gdx.gl.glDisable(GL20.GL_BLEND);
        }
    }

    public void showUpgradeScreen() {
        if (player.weapons.size == 0) return;
        Weapon w = player.weapons.first();
        game.setScreen(new UpgradeScreen(game, w, upgradeSystem, this));
    }

    /** Draw full VS-style HUD in screen pixel coordinates. */
    private void drawHud() {
        int sw = Gdx.graphics.getWidth();
        int sh = Gdx.graphics.getHeight();

        float xpBarH   = 20f;
        float xpBarY   = sh - xpBarH;
        float progress = xpSystem.getProgress();
        int   lvl      = xpSystem.getLevel();
        float hpFrac   = player.maxHp > 0 ? Math.max(0, player.hp / player.maxHp) : 0;
        int   kills    = GameStateManager.getInstance().kills;

        // Timer — use countdown if wave active, else total elapsed
        float timerSecs = Math.max(0, waveManager.getWaveTimer());
        int   mins      = (int) timerSecs / 60;
        int   secs      = (int) timerSecs % 60;
        String timerStr = String.format("%02d:%02d", mins, secs);

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.setProjectionMatrix(hudCamera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // ── XP bar (very top, full width) ──────────────────────────────
        shapeRenderer.setColor(0.08f, 0.08f, 0.18f, 0.92f);
        shapeRenderer.rect(0, xpBarY, sw, xpBarH);
        shapeRenderer.setColor(0.2f, 0.55f, 1f, 1f);
        shapeRenderer.rect(0, xpBarY, sw * progress, xpBarH);
        shapeRenderer.setColor(0.55f, 0.82f, 1f, 0.45f);
        shapeRenderer.rect(0, xpBarY + xpBarH - 5f, sw * progress, 5f);

        // ── Top bar background strip (below XP bar) ────────────────────
        float topStripH = 40f;
        float topStripY = sh - xpBarH - topStripH;
        shapeRenderer.setColor(0f, 0f, 0f, 0.55f);
        shapeRenderer.rect(0, topStripY, sw, topStripH);

        // ── HP bar (top-left inside strip) ────────────────────────────
        float hpW = 210f, hpH = 16f;
        float hpX = 48f,  hpY = topStripY + (topStripH - hpH) / 2f;
        shapeRenderer.setColor(0.18f, 0f, 0f, 1f);
        shapeRenderer.rect(hpX, hpY, hpW, hpH);
        // Color: green→yellow→red depending on HP
        float r = 1f - hpFrac * 0.5f;
        float g = hpFrac;
        shapeRenderer.setColor(r, g, 0.05f, 1f);
        shapeRenderer.rect(hpX, hpY, hpW * hpFrac, hpH);
        // HP bar highlight
        shapeRenderer.setColor(1f, 1f, 1f, 0.15f);
        shapeRenderer.rect(hpX, hpY + hpH - 4f, hpW * hpFrac, 4f);

        // ── Timer box (top-center) ────────────────────────────────────
        float tboxW = 110f, tboxH = 36f;
        float tboxX = (sw - tboxW) / 2f;
        float tboxY = topStripY + (topStripH - tboxH) / 2f;
        shapeRenderer.setColor(0.15f, 0.12f, 0.05f, 0.9f);
        shapeRenderer.rect(tboxX, tboxY, tboxW, tboxH);
        shapeRenderer.setColor(0.6f, 0.45f, 0.1f, 0.6f);
        shapeRenderer.rect(tboxX, tboxY + tboxH - 3f, tboxW, 3f);

        // ── Kill counter (top-right) ──────────────────────────────────
        shapeRenderer.setColor(0.1f, 0.05f, 0.05f, 0.85f);
        shapeRenderer.rect(sw - 140f, topStripY + 5f, 132f, 28f);

        // ── Weapon inventory slots (bottom-center) ───────────────────
        float slotW = 130f, slotH = 66f, slotGap = 8f;
        float slotsTotal = Constants.PLAYER_MAX_WEAPONS * slotW + (Constants.PLAYER_MAX_WEAPONS - 1) * slotGap;
        float slotStartX = (sw - slotsTotal) / 2f;
        float slotY = 4f;
        for (int i = 0; i < Constants.PLAYER_MAX_WEAPONS; i++) {
            float sx = slotStartX + i * (slotW + slotGap);
            boolean filled = i < player.weapons.size;
            shapeRenderer.setColor(0f, 0f, 0f, filled ? 0.72f : 0.35f);
            shapeRenderer.rect(sx, slotY, slotW, slotH);
            if (filled) {
                float[] rc = rarityRGB(player.weapons.get(i).rarity);
                shapeRenderer.setColor(rc[0], rc[1], rc[2], 0.9f);
            } else {
                shapeRenderer.setColor(0.18f, 0.18f, 0.22f, 0.5f);
            }
            shapeRenderer.rect(sx, slotY + slotH - 4f, slotW, 4f);
        }

        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        // ── Text pass ────────────────────────────────────────────────
        batch.setProjectionMatrix(hudCamera.combined);
        batch.begin();

        // LVL inside XP bar (right)
        hudFont.getData().setScale(1.3f);
        hudFont.setColor(Color.WHITE);
        String lvlStr = "LVL " + lvl;
        glyphLayout.setText(hudFont, lvlStr);
        hudFont.draw(batch, lvlStr, sw - glyphLayout.width - 10f, xpBarY + xpBarH - 3f);

        // Heart icon + HP text
        hudFont.getData().setScale(1.3f);
        hudFont.setColor(new Color(1f, 0.25f, 0.25f, 1f));
        hudFont.draw(batch, "HP", 10f, topStripY + topStripH - 12f);
        hudFont.setColor(Color.WHITE);
        hudFont.draw(batch, String.format("%.0f / %.0f", player.hp, player.maxHp),
            50f, topStripY + topStripH - 12f);

        // Timer
        hudFontBig.getData().setScale(2.0f);
        hudFontBig.setColor(new Color(1f, 0.88f, 0.4f, 1f));
        glyphLayout.setText(hudFontBig, timerStr);
        hudFontBig.draw(batch, timerStr,
            (sw - glyphLayout.width) / 2f,
            topStripY + topStripH - 6f);

        // Kill counter
        hudFont.getData().setScale(1.3f);
        hudFont.setColor(new Color(1f, 0.55f, 0.2f, 1f));
        hudFont.draw(batch, "K: " + kills, sw - 134f, topStripY + topStripH - 10f);

        // ── Weapon inventory slots text ──────────────────────
        slotW = 130f; slotH = 66f; slotGap = 8f;
        slotsTotal = Constants.PLAYER_MAX_WEAPONS * slotW + (Constants.PLAYER_MAX_WEAPONS - 1) * slotGap;
        slotStartX = (sw - slotsTotal) / 2f;
        slotY = 4f;
        for (int i = 0; i < Constants.PLAYER_MAX_WEAPONS; i++) {
            float sx = slotStartX + i * (slotW + slotGap);
            boolean filled = i < player.weapons.size;
            // Slot index number
            hudFont.getData().setScale(0.95f);
            hudFont.setColor(0.45f, 0.45f, 0.5f, 0.8f);
            hudFont.draw(batch, String.valueOf(i + 1), sx + 5f, slotY + slotH - 4f);
            if (filled) {
                Weapon w = player.weapons.get(i);
                hudFont.getData().setScale(1.25f);
                hudFont.setColor(Color.WHITE);
                glyphLayout.setText(hudFont, w.name);
                hudFont.draw(batch, w.name, sx + (slotW - glyphLayout.width) / 2f, slotY + slotH - 18f);
                hudFont.getData().setScale(0.95f);
                hudFont.setColor(Color.YELLOW);
                String stats = String.format("%.0f DMG  %.1fs", w.damage, w.cooldown);
                glyphLayout.setText(hudFont, stats);
                hudFont.draw(batch, stats, sx + (slotW - glyphLayout.width) / 2f, slotY + 18f);
            } else {
                hudFont.getData().setScale(1.0f);
                hudFont.setColor(0.28f, 0.28f, 0.32f, 0.8f);
                glyphLayout.setText(hudFont, "- empty -");
                hudFont.draw(batch, "- empty -", sx + (slotW - glyphLayout.width) / 2f, slotY + slotH / 2f + 6f);
            }
        }

        // ── Floating damage numbers ──────────────────────────────────
        hudFont.getData().setScale(1.6f);
        for (DamageNumber n : damageNumbers) {
            hudFont.setColor(1f, 0.85f, 0.1f, n.alpha);
            hudFont.draw(batch, n.text, n.x, n.y);
        }

        batch.end();
    }

    /** Finds nearest enemy not already visited by a bouncing projectile. */
    private Enemy findNearestBounceTarget(Projectile p, Array<Enemy> enemies) {
        Enemy nearest = null;
        float minDist = Float.MAX_VALUE;
        for (int i = 0; i < enemies.size; i++) {
            Enemy e = enemies.get(i);
            if (p.visitedEnemies.contains(e, true)) continue;
            float dx = e.x - p.x;
            float dy = e.y - p.y;
            float d  = dx * dx + dy * dy;
            if (d < minDist) { minDist = d; nearest = e; }
        }
        return nearest;
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

    public void showChestScreen(Chest chest) {
        // TODO M3: overlay ChestScreen
    }

    // --- WaveManager.WaveListener ---
    @Override public void onBossKilled()  { game.setScreen(new WinScreen(game)); }

    @Override
    public void onWaveEnd() {
        // Start fade-to-black; transition fires once fully black
        fadingOut = true;
        pendingTransition = () -> {
            boolean advanced = GameStateManager.getInstance().advanceLocation();
            if (advanced) {
                game.setScreen(new GameScreen(game, player, xpSystem, upgradeSystem));
            } else {
                game.setScreen(new WinScreen(game));
            }
        };
    }

    // --- XPSystem.XPListener ---
    @Override
    public void onLevelUp(int newLevel) {
        player.levelUp();
        GameStateManager.getInstance().playerLevel = newLevel;
        showUpgradeScreen();
    }

    // --- ChestSystem.ChestListener ---
    @Override
    public void onChestOpened(Chest chest) {
        showChestScreen(chest);
    }

    @Override public void resize(int width, int height) {
        camera.setToOrtho(false, width / Constants.PPM, height / Constants.PPM);
        hudCamera.setToOrtho(false, width, height);
    }
    @Override public void pause() {}
    @Override public void resume() {}

    @Override public void hide() {
        // Do NOT dispose here — UpgradeScreen/ChestScreen return to this same instance
    }

    @Override
    public void dispose() {
        if (batch != null)         { batch.dispose();         batch = null; }
        if (shapeRenderer != null) { shapeRenderer.dispose(); shapeRenderer = null; }
        if (playerTexture    != null) { playerTexture.dispose();    playerTexture    = null; }
        if (backgroundTexture != null) { backgroundTexture.dispose(); backgroundTexture = null; }
        if (hudFont       != null) { hudFont.dispose();       hudFont       = null; }
        if (hudFontBig    != null) { hudFontBig.dispose();    hudFontBig    = null; }
        player.dispose();
        uiManager.dispose();
    }
}яё
