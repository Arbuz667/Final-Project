package com.softchaos.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
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

    private WeaponSystem   weaponSystem;
    private WaveManager    waveManager;
    private EnemySpawner   enemySpawner;
    private XPSystem       xpSystem;
    private UpgradeSystem  upgradeSystem;
    private ChestSystem    chestSystem;
    private UIManager      uiManager;

    public GameScreen(SoftChaosGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        float worldW = Constants.SCREEN_WIDTH  / Constants.PPM;
        float worldH = Constants.SCREEN_HEIGHT / Constants.PPM;

        camera = new OrthographicCamera();
        camera.setToOrtho(false, worldW, worldH);

        batch         = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
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
                    p.onHit(e);
                    if (!p.active) break;
                }
            }
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

        // Debug shapes — replaced with sprites in M4
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Player: blue square
        shapeRenderer.setColor(Color.BLUE);
        shapeRenderer.rect(player.x - 0.4f, player.y - 0.4f, 0.8f, 0.8f);

        // Enemies: red squares
        shapeRenderer.setColor(Color.RED);
        for (Enemy e : enemies) {
            shapeRenderer.rect(e.x - 0.4f, e.y - 0.4f, 0.8f, 0.8f);
        }

        // Projectiles: yellow small squares
        shapeRenderer.setColor(Color.YELLOW);
        for (Projectile p : projectiles) {
            float h = p.size / 2f;
            shapeRenderer.rect(p.x - h, p.y - h, p.size, p.size);
        }

        shapeRenderer.end();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        // TODO M4: draw background, sprite overlays
        uiManager.render(batch);
        batch.end();
    }

    public void showUpgradeScreen() {
        // TODO M3: pause and overlay UpgradeScreen
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
    }
    @Override public void pause() {}
    @Override public void resume() {}

    @Override
    public void hide() {
        dispose();
    }

    @Override
    public void dispose() {
        if (batch != null)         { batch.dispose();         batch = null; }
        if (shapeRenderer != null) { shapeRenderer.dispose(); shapeRenderer = null; }
        player.dispose();
        uiManager.dispose();
    }
}
