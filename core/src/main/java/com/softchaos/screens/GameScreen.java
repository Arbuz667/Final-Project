package com.softchaos.screens;

import com.badlogic.gdx.Gdx;
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
    private Texture grassTexture;

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

    public GameScreen(SoftChaosGame game) {
        this.game = game;

        float worldW = Constants.SCREEN_WIDTH  / Constants.PPM;
        float worldH = Constants.SCREEN_HEIGHT / Constants.PPM;

        camera = new OrthographicCamera();
        camera.setToOrtho(false, worldW, worldH);

        batch         = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        playerTexture = new Texture(Gdx.files.internal("player.png"));
        grassTexture  = new Texture(Gdx.files.internal("grass.png"));
        grassTexture.setWrap(com.badlogic.gdx.graphics.Texture.TextureWrap.Repeat,
                             com.badlogic.gdx.graphics.Texture.TextureWrap.Repeat);

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

        player        = new Player();
        weaponSystem  = new WeaponSystem();
        enemySpawner  = new EnemySpawner(enemies);
        waveManager   = new WaveManager(enemySpawner);
        xpSystem      = new XPSystem();
        upgradeSystem = new UpgradeSystem(weaponSystem);
        chestSystem   = new ChestSystem();
        uiManager     = new UIManager(player, waveManager, xpSystem);

        // Give player a starting weapon
        player.weapons.add(new Sword());

        waveManager.setListener(this);
        xpSystem.setListener(this);
        chestSystem.setListener(this);

        // Default to FOREST if no location selected yet
        LocationType loc = GameStateManager.getInstance().currentLocation;
        if (loc == null) {
            loc = LocationType.FOREST;
            GameStateManager.getInstance().currentLocation = loc;
        }
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
            }
        }

        // Update projectiles + collision
        for (int i = projectiles.size - 1; i >= 0; i--) {
            Projectile p = projectiles.get(i);
            p.update(delta);
            if (!p.active) { projectiles.removeIndex(i); continue; }

            // Collision with enemies
            for (Enemy e : enemies) {
                if (p.hitbox.overlaps(e.hitbox)) {
                    float hpBefore = e.hp;
                    p.onHit(e);
                    float dealt = hpBefore - e.hp;
                    if (dealt > 0) damageNumbers.add(new DamageNumber(e.x, e.y, dealt));
                    if (!p.active) break;
                }
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

        // 1. Background
        float worldW = Constants.SCREEN_WIDTH  / Constants.PPM;
        float worldH = Constants.SCREEN_HEIGHT / Constants.PPM;
        float tileSize = grassTexture.getWidth() / Constants.PPM;
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        for (float tx = 0; tx < worldW; tx += tileSize) {
            for (float ty = 0; ty < worldH; ty += tileSize) {
                batch.draw(grassTexture, tx, ty, tileSize, tileSize);
            }
        }
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

        // ── Weapon stats panel (bottom-left) ─────────────────────────
        if (!player.weapons.isEmpty()) {
            shapeRenderer.setColor(0f, 0f, 0f, 0.5f);
            shapeRenderer.rect(4f, 4f, 188f, 92f);
            shapeRenderer.setColor(0.2f, 0.2f, 0.2f, 0.7f);
            shapeRenderer.rect(4f, 92f, 188f, 2f);
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

        // Weapon stats
        if (!player.weapons.isEmpty()) {
            Weapon w = player.weapons.first();
            hudFont.getData().setScale(1.3f);
            hudFont.setColor(new Color(0.8f, 0.8f, 0.8f, 1f));
            hudFont.draw(batch, w.name, 10f, 92f);
            hudFont.setColor(Color.YELLOW);
            hudFont.draw(batch, String.format("DMG  %.1f",  w.damage),    10f, 70f);
            hudFont.setColor(new Color(0.35f, 0.9f, 0.35f, 1f));
            hudFont.draw(batch, String.format("CD   %.2fs", w.cooldown),   10f, 48f);
            if (w.projectileCount > 1) {
                hudFont.setColor(Color.CYAN);
                hudFont.draw(batch, "PROJ " + w.projectileCount, 10f, 26f);
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

    public void showChestScreen(Chest chest) {
        // TODO M3: overlay ChestScreen
    }

    // --- WaveManager.WaveListener ---
    @Override public void onBossKilled()  { game.setScreen(new WinScreen(game)); }
    @Override public void onWaveEnd()     { /* endless mode started, continue */ }

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
        if (playerTexture != null) { playerTexture.dispose(); playerTexture = null; }
        if (grassTexture  != null) { grassTexture.dispose();  grassTexture  = null; }
        if (hudFont       != null) { hudFont.dispose();       hudFont       = null; }
        if (hudFontBig    != null) { hudFontBig.dispose();    hudFontBig    = null; }
        player.dispose();
        uiManager.dispose();
    }
}
