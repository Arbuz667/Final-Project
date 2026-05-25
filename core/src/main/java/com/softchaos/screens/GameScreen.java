package com.softchaos.screens;

import com.badlogic.gdx.Screen;
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
import com.softchaos.weapons.WeaponSystem;

/** Main gameplay screen. update() + draw() loop for all systems. */
public class GameScreen implements Screen,
        WaveManager.WaveListener,
        XPSystem.XPListener,
        ChestSystem.ChestListener {

    private final SoftChaosGame game;
    private SpriteBatch batch;

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
        batch       = new SpriteBatch();
        enemies     = new Array<>();
        projectiles = new Array<>();

        player        = new Player();
        weaponSystem  = new WeaponSystem();
        enemySpawner  = new EnemySpawner(enemies);
        waveManager   = new WaveManager(enemySpawner);
        xpSystem      = new XPSystem();
        upgradeSystem = new UpgradeSystem(weaponSystem);
        chestSystem   = new ChestSystem();
        uiManager     = new UIManager(player, waveManager, xpSystem);

        waveManager.setListener(this);
        xpSystem.setListener(this);
        chestSystem.setListener(this);

        waveManager.startWave(GameStateManager.getInstance().currentLocation, 120f);

        AudioManager.getInstance().playMusic(
            com.softchaos.utils.MusicType.valueOf(
                GameStateManager.getInstance().currentLocation.name()));
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

        // Update projectiles
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
        batch.begin();
        // TODO M1: draw background
        player.render(batch);
        for (Enemy e : enemies)         e.render(batch);
        for (Projectile p : projectiles) p.render(batch);
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

    @Override public void resize(int width, int height) {}
    @Override public void pause() {}
    @Override public void resume() {}

    @Override
    public void hide() {
        dispose();
    }

    @Override
    public void dispose() {
        if (batch != null) { batch.dispose(); batch = null; }
        player.dispose();
        uiManager.dispose();
    }
}
