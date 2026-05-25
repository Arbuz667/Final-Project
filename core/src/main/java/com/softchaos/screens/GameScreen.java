package com.softchaos.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;
import com.softchaos.ai.MegalodonAI;
import com.softchaos.ai.PickleRickAI;
import com.softchaos.SoftChaosGame;
import com.softchaos.entities.Boss;
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
import com.softchaos.utils.ChestType;
import com.softchaos.utils.EnemyType;
import com.softchaos.utils.LocationType;
import com.softchaos.utils.MusicType;
import com.softchaos.utils.ProjectileType;
import com.softchaos.utils.WeaponRarity;
import com.softchaos.weapons.Sword;
import com.softchaos.weapons.Weapon;
import com.softchaos.weapons.WeaponSystem;
import com.softchaos.weapons.cringe.SixSeven;

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
    private Array<Projectile> enemyProjectiles;

    // Player animation
    private Texture[]  playerFrames;
    private int        playerFrameIdx  = 0;
    private float      playerFrameTimer = 0f;
    private static final float PLAYER_FRAME_DUR = 0.15f;

    // Projectile textures
    private Texture texArrow;
    private Texture texBullet;
    private Texture texShuriken;
    private Texture texEnemyBullet;
    private Texture texRocket;
    private Texture texFireball;
    private Texture texPotato;
    private Texture texSword;

    // Enemy walk animation frames (one array per type)
    private Texture[] texRabbit;     // 3 frames
    private Texture[] texAlien;      // 2 frames
    private Texture[] texJellyfish;  // 1 frame
    private Texture[] texRobot;      // 2 frames (also used for SNIPER)
    private Texture[] texShark;      // 3 frames
    private Texture[] texZombie;     // 3 frames
    // Boss textures
    private Texture[] texSlenderman; // 2 frames
    private Texture   texMegalodon;
    private Texture   texPickleRick;
    // Per-type animation state (indexed by EnemyType.ordinal())
    private final float[] enemyAnimTimers = new float[EnemyType.values().length];
    private final int[]   enemyAnimFrames = new int[EnemyType.values().length];
    private static final float ENEMY_ANIM_DUR = 0.18f;
    private float bossAnimTimer = 0f;
    private int   bossAnimFrame = 0;
    private static final float BOSS_ANIM_DUR  = 0.30f;

    // Weapon icon textures (keyed by weapon.id)
    private java.util.HashMap<String, Texture> weaponIcons;

    private Texture backgroundTexture;
    private Texture texChest;
    private Texture texHpIcon;

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

    // Explosion animation
    private Texture[] explosionFrames;
    private Array<ExplosionEffect> explosions;

    private static class ExplosionEffect {
        float x, y, timer;
        float radius;          // used to scale the visual size proportionally
        int   frame = 0;
        static final float FRAME_DUR = 0.055f;
        static final int   FRAME_COUNT = 8;
        ExplosionEffect(float x, float y, float radius) { this.x = x; this.y = y; this.radius = radius; }
        boolean isFinished() { return frame >= FRAME_COUNT; }
    }

    // Location-transition fade
    private float    fadeAlpha        = 0f;
    private boolean  fadingOut        = false;
    private Runnable pendingTransition;

    // Pause menu
    private boolean   isPaused         = false;
    private Texture   pauseRetryNormal, pauseRetryHover;
    private Texture   pauseQuitNormal,  pauseQuitHover;
    private Texture   pauseSettingsNormal;
    private Rectangle pauseRetryHit,    pauseQuitHit,    pauseSettingsHit;

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

        float worldW = Gdx.graphics.getWidth()  / Constants.PPM;
        float worldH = Gdx.graphics.getHeight() / Constants.PPM;

        camera = new OrthographicCamera();
        camera.setToOrtho(false, worldW, worldH);

        batch         = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        // Load character-specific animation frames
        int ch = GameStateManager.getInstance().selectedCharacter;
        if (ch == 0) {
            playerFrames = new Texture[]{
                new Texture(Gdx.files.internal("characters/Warrior.png")),
                new Texture(Gdx.files.internal("characters/Warrior left leg.png")),
                new Texture(Gdx.files.internal("characters/Warrior\u00a0right leg.png"))
            };
        } else if (ch == 1) {
            playerFrames = new Texture[]{
                new Texture(Gdx.files.internal("characters/elf straight.png")),
                new Texture(Gdx.files.internal("characters/elf left .png")),
                new Texture(Gdx.files.internal("characters/elf right.png"))
            };
        } else {
            playerFrames = new Texture[]{
                new Texture(Gdx.files.internal("characters/witch.png")),
                new Texture(Gdx.files.internal("characters/withc left.png"))
            };
        }

        // Load full-screen background for current location
        LocationType loc = GameStateManager.getInstance().currentLocation;
        if (loc == null) {
            loc = LocationType.FOREST;
            GameStateManager.getInstance().currentLocation = loc;
        }
        backgroundTexture = new Texture(Gdx.files.internal(loc.backgroundFile()));

        // Projectile textures
        texChest       = new Texture(Gdx.files.internal("chest.png"));
        texChest.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        texHpIcon      = new Texture(Gdx.files.internal("hud/hp_icon.png"));
        texHpIcon.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        texArrow       = new Texture(Gdx.files.internal("projectiles/arrow.png"));
        texBullet      = new Texture(Gdx.files.internal("projectiles/bullet_player.png"));
        texShuriken    = new Texture(Gdx.files.internal("weapons/shuriken icon.png"));
        texEnemyBullet = new Texture(Gdx.files.internal("projectiles/bullet_enemy.png"));
        texRocket      = new Texture(Gdx.files.internal("projectiles/rocket.png"));
        texFireball    = new Texture(Gdx.files.internal("projectiles/fireball.png"));
        texPotato      = new Texture(Gdx.files.internal("projectiles/potato.png"));
        texSword       = new Texture(Gdx.files.internal("projectiles/sword texture.png"));
        for (Texture t : new Texture[]{texArrow, texBullet, texShuriken, texEnemyBullet, texRocket, texFireball, texPotato, texSword})
            t.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

        // Weapon icons
        weaponIcons = new java.util.HashMap<>();
        String[][] iconMap = {
            {"sword",          "weapons/Yellow sword icon.png"},
            {"iron_bow",       "weapons/icon iron bow.png"},
            {"diary",          "weapons/diary icon.png"},
            {"desert_eagles",  "weapons/desert eagels icon.png"},
            {"shurikens",      "weapons/shuriken icon.png"},
            {"minigun",        "weapons/mini-gun icon.png"},
            {"potato_thrower", "weapons/patoeto-thrower icon.png"},
            {"nuclear_bazooka","weapons/Nuclear Bazooka icon.png"},
            {"six_seven",      "weapons/67.png"}
        };
        for (String[] pair : iconMap) {
            Texture t = new Texture(Gdx.files.internal(pair[1]));
            t.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            weaponIcons.put(pair[0], t);
        }

        hudCamera = new OrthographicCamera();
        hudCamera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        hudFont   = new BitmapFont();
        hudFont.getData().setScale(1.4f);
        hudFontBig = new BitmapFont();
        hudFontBig.getData().setScale(2.8f);
        glyphLayout   = new GlyphLayout();
        damageNumbers = new Array<>();
        explosions    = new Array<>();

        explosionFrames = new Texture[8];
        for (int i = 0; i < 8; i++) {
            explosionFrames[i] = new Texture(Gdx.files.internal("projectiles/explosion_f" + i + ".png"));
            explosionFrames[i].setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        }

        // Enemy walk sprites
        texRabbit     = loadN("enemies/rabbit 1.png",    "enemies/rabbit 2.png",   "enemies/rabbit3.png");
        texAlien      = loadN("enemies/alien1.png",      "enemies/alien2.png");
        texJellyfish  = loadN("enemies/jellyfish.png");
        texRobot      = loadN("enemies/robot1.png",      "enemies/robot2.png");
        texShark      = loadN("enemies/shark1.png",      "enemies/shark2.png",     "enemies/shark3.png");
        texZombie     = loadN("enemies/zombie1.png",     "enemies/zombie2.png",    "enemies/zombie3.png");
        texSlenderman = loadN("enemies/slenderman 1.png","enemies/slenderman 2.png");
        texMegalodon  = loadN("enemies/megaladon.png")[0];
        texPickleRick = loadN("enemies/Pickle Rick.png")[0];

        enemies       = new Array<>();
        projectiles   = new Array<>();
        enemyProjectiles = new Array<>();

        if (existingPlayer != null) {
            player   = existingPlayer;
            // Reset position to world centre for the new location
            player.x = worldW / 2f;
            player.y = worldH / 2f;
        } else {
            player = new Player();
            player.x = worldW / 2f;
            player.y = worldH / 2f;
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

        // Pause menu textures + hitboxes
        pauseRetryNormal = new Texture(Gdx.files.internal("buttons/retry button.png"));
        pauseRetryHover  = new Texture(Gdx.files.internal("buttons/retry button light.png"));
        pauseQuitNormal  = new Texture(Gdx.files.internal("buttons/death menu quit.png"));
        pauseQuitHover   = new Texture(Gdx.files.internal("buttons/death menu quit light.png"));
        pauseSettingsNormal = new Texture(Gdx.files.internal("buttons/settings_button.png"));
        for (Texture t : new Texture[]{pauseRetryNormal, pauseRetryHover, pauseQuitNormal, pauseQuitHover, pauseSettingsNormal})
            t.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        float pcx = Gdx.graphics.getWidth()  / 2f;
        float pcy = Gdx.graphics.getHeight() / 2f;
        int   psw = Gdx.graphics.getWidth();
        int   psh = Gdx.graphics.getHeight();
        pauseSettingsHit = new Rectangle(psw - 92f, psh - 92f, 72f, 72f);
        pauseRetryHit    = new Rectangle(pcx - 150f, pcy - 100f, 300f, 84f);
        pauseQuitHit     = new Rectangle(pcx - 150f, pcy - 220f, 300f, 84f);
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
        // ESC — toggle pause (skip during location fade-out)
        if (!fadingOut && Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            isPaused = !isPaused;
            return;
        }

        if (isPaused) {
            float pmx    = Gdx.input.getX();
            float pmy    = Gdx.graphics.getHeight() - Gdx.input.getY();
            boolean pClk = Gdx.input.justTouched();
            if ((pClk && pauseRetryHit.contains(pmx, pmy)) || Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                GameStateManager.getInstance().reset();
                game.setScreen(new CharacterSelectScreen(game));
            }
            if ((pClk && pauseQuitHit.contains(pmx, pmy)) || Gdx.input.isKeyJustPressed(Input.Keys.M)) {
                GameStateManager.getInstance().reset();
                game.setScreen(new MainMenuScreen(game));
            }
            if (pClk && pauseSettingsHit.contains(pmx, pmy)) {
                game.setScreen(new SettingsScreen(game, this));
            }
            return;
        }

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

        // Advance walk animation when player is moving
        if (player.isMoving && playerFrames.length > 1) {
            playerFrameTimer += delta;
            if (playerFrameTimer >= PLAYER_FRAME_DUR) {
                playerFrameTimer -= PLAYER_FRAME_DUR;
                playerFrameIdx = (playerFrameIdx + 1) % playerFrames.length;
            }
        } else if (!player.isMoving) {
            playerFrameTimer = 0f;
            playerFrameIdx   = 0;
        }

        // Advance enemy walk animations (per type)
        EnemyType[] walkers = {EnemyType.RABBID, EnemyType.ALIEN, EnemyType.JELLYFISH,
                                EnemyType.ROBOT, EnemyType.SHARK, EnemyType.ZOMBIE, EnemyType.SNIPER};
        for (EnemyType et : walkers) {
            int idx = et.ordinal();
            enemyAnimTimers[idx] += delta;
            if (enemyAnimTimers[idx] >= ENEMY_ANIM_DUR) {
                enemyAnimTimers[idx] -= ENEMY_ANIM_DUR;
                enemyAnimFrames[idx] = (enemyAnimFrames[idx] + 1) % enemyFrameCount(et);
            }
        }
        bossAnimTimer += delta;
        if (bossAnimTimer >= BOSS_ANIM_DUR) {
            bossAnimTimer -= BOSS_ANIM_DUR;
            bossAnimFrame = (bossAnimFrame + 1) % 2;
        }

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
                if (e instanceof Boss) {
                    waveManager.onBossKilled();
                    enemySpawner.freeEnemy(e);
                    i--;
                    continue;
                }
                // Roll chest drop using enemy's configured drop chance
                if (e.chestDropChance > 0 && MathUtils.randomBoolean(e.chestDropChance)) {
                    ChestType cType = e.chestType != null ? e.chestType : ChestType.GOLD;
                    chestSystem.spawnChest(cType, e.x, e.y);
                }
                enemySpawner.freeEnemy(e);
                i--;
                continue;
            }
            // Enemy touches player → deal damage (once per 0.8s)
            if (e.hitbox.overlaps(player.hitbox)) {
                e.meleeCooldown -= delta;
                if (e.meleeCooldown <= 0f) {
                    player.takeDamage(e.damage);
                    e.meleeCooldown = 0.8f;
                }
            } else {
                e.meleeCooldown = 0f; // reset when not touching
            }
        }

        // Drain pending shots from RangedAI enemies into enemyProjectiles
        for (Enemy e : enemies) {
            if (!e.pendingShots.isEmpty()) {
                enemyProjectiles.addAll(e.pendingShots);
                e.pendingShots.clear();
            }
        }

        // Update enemy projectiles + collision with player
        for (int i = enemyProjectiles.size - 1; i >= 0; i--) {
            Projectile p = enemyProjectiles.get(i);
            p.update(delta);
            if (!p.active) { enemyProjectiles.removeIndex(i); continue; }
            if (p.hitbox.overlaps(player.hitbox)) {
                player.takeDamage(p.damage);
                p.active = false;
                enemyProjectiles.removeIndex(i);
            }
        }

        // Update projectiles + collision
        for (int i = projectiles.size - 1; i >= 0; i--) {
            Projectile p = projectiles.get(i);
            p.update(delta);
            if (!p.active) {
                if (p.projectileType == ProjectileType.ROCKET ||
                    p.projectileType == ProjectileType.FIREBALL)
                    explosions.add(new ExplosionEffect(p.x, p.y, p.explosionRadius));
                projectiles.removeIndex(i);
                continue;
            }

            // Collision with enemies
            for (int ei = 0; ei < enemies.size; ei++) {
                Enemy e = enemies.get(ei);
                // Skip enemies already hit by this projectile in its lifetime
                if (p.visitedEnemies.contains(e, true)) continue;
                if (!p.hitbox.overlaps(e.hitbox)) continue;

                p.visitedEnemies.add(e);
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

        // Update explosion animations
        for (int i = explosions.size - 1; i >= 0; i--) {
            ExplosionEffect ef = explosions.get(i);
            ef.timer += delta;
            while (ef.timer >= ExplosionEffect.FRAME_DUR) {
                ef.timer -= ExplosionEffect.FRAME_DUR;
                ef.frame++;
            }
            if (ef.isFinished()) explosions.removeIndex(i);
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

        // Chest pickup — player walks over a chest to open it
        Array<Chest> activeChests = chestSystem.getActiveChests();
        for (int ci = activeChests.size - 1; ci >= 0; ci--) {
            Chest c = activeChests.get(ci);
            if (!c.open && c.hitbox.overlaps(player.hitbox)) {
                chestSystem.openChest(c);
                break;
            }
        }

        AudioManager.getInstance().update(delta);
        uiManager.update();
    }

    private void draw() {
        // Clear screen
        Gdx.gl.glClearColor(0.08f, 0.08f, 0.08f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();

        // 1. Background + enemy sprites (world space)
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.draw(backgroundTexture, 0, 0, camera.viewportWidth, camera.viewportHeight);
        // Chests — 2x2 world unit, shifted down to compensate for ~20% empty bottom in texture
        for (Chest c : chestSystem.getActiveChests()) {
            if (!c.open && texChest != null)
                batch.draw(texChest, c.x - 1.0f, c.y - 1.2f, 2.0f, 2.0f);
        }
        for (Enemy e : enemies) {
            Texture eTex;
            float   eSize;
            if (e instanceof Boss) {
                eTex  = getBossFrame((Boss) e);
                eSize = 5.0f;
            } else {
                eTex  = getEnemyFrame(e);
                eSize = enemyDisplaySize(e.type);
            }
            if (eTex != null) {
                // Shark + Megalodon sprites face LEFT by default; all others face RIGHT.
                boolean spriteFacesLeft = isSpriteFacingLeft(e);
                boolean flip = spriteFacesLeft ? !e.facingLeft : e.facingLeft;
                if (flip)
                    batch.draw(eTex, e.x + eSize / 2f, e.y - eSize / 2f, -eSize, eSize);
                else
                    batch.draw(eTex, e.x - eSize / 2f, e.y - eSize / 2f,  eSize, eSize);
            }
        }
        batch.end();

        // 2. Chests + HP bars (shapes drawn on top of enemy sprites)
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // SixSeven: aura zone + cringe meter bars
        for (Weapon w : player.weapons) {
            if (!(w instanceof SixSeven)) continue;
            SixSeven ss = (SixSeven) w;
            // Zone border (darker, slightly larger circle)
            shapeRenderer.setColor(0f, 0f, 0f, 0.40f);
            shapeRenderer.circle(player.x, player.y, SixSeven.RADIUS + 0.22f, 72);
            // Zone fill (gold, semi-transparent)
            shapeRenderer.setColor(1f, 0.82f, 0f, 0.14f);
            shapeRenderer.circle(player.x, player.y, SixSeven.RADIUS, 72);
        }

        // Enemy HP bars — boss gets a wider, taller bar
        for (Enemy e : enemies) {
            float frac    = Math.max(0, e.hp / e.maxHp);
            float hpBarW  = (e instanceof Boss) ? 4.0f  : enemyDisplaySize(e.type);
            float hpBarHt = (e instanceof Boss) ? 0.18f : 0.09f;
            float bx      = e.x - hpBarW / 2f;
            float by      = (e instanceof Boss) ? e.y + 2.65f : e.y + enemyDisplaySize(e.type) / 2f + 0.15f;
            shapeRenderer.setColor(0.25f, 0f, 0f, 0.9f);
            shapeRenderer.rect(bx, by, hpBarW, hpBarHt);
            shapeRenderer.setColor(0.9f, 0.12f, 0.12f, 1f);
            shapeRenderer.rect(bx, by, hpBarW * frac, hpBarHt);
        }

        // Cringe meter bars — golden bar above HP bar for each SixSeven target
        for (Weapon w : player.weapons) {
            if (!(w instanceof SixSeven)) continue;
            for (Enemy t : ((SixSeven) w).targets) {
                float hpBarW  = (t instanceof Boss) ? 4.0f  : enemyDisplaySize(t.type);
                float hpBarHt = (t instanceof Boss) ? 0.18f : 0.09f;
                float bx      = t.x - hpBarW / 2f;
                float by      = (t instanceof Boss) ? t.y + 2.65f : t.y + enemyDisplaySize(t.type) / 2f + 0.15f;
                float cringeY = by + hpBarHt + 0.05f;
                float frac    = Math.min(1f, t.cringeMeter / 100f);
                // Background
                shapeRenderer.setColor(0.25f, 0.20f, 0f, 0.9f);
                shapeRenderer.rect(bx, cringeY, hpBarW, hpBarHt);
                // Fill (gold)
                shapeRenderer.setColor(1f, 0.82f, 0f, 1f);
                shapeRenderer.rect(bx, cringeY, hpBarW * frac, hpBarHt);
            }
        }

        // Enemy projectiles — red bullets
        // (drawn as textures in the batch section below)

        // Projectiles — drawn as textures in the batch section below
        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        // 3. Projectiles (textured) + Player sprite + UI
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        // Explosion effects (draw under projectiles and player)
        for (ExplosionEffect ef : explosions) {
            if (!ef.isFinished()) {
                // Base radius 5.0 → visual 3.5; scales proportionally with upgrades
                float vis = ef.radius > 0 ? ef.radius * 0.7f : 5f;
                batch.draw(explosionFrames[ef.frame],
                    ef.x - vis/2f, ef.y - vis/2f, vis, vis);
            }
        }
        // Enemy projectiles
        for (Projectile p : enemyProjectiles) {
            float vis = Math.max(0.35f, p.size * 2.5f);
            batch.draw(texEnemyBullet, p.x - vis/2f, p.y - vis/2f, vis, vis);
        }
        // Player projectiles
        for (Projectile p : projectiles) {
            float angle = (float)(Math.atan2(p.velY, p.velX) * MathUtils.radiansToDegrees);
            switch (p.projectileType) {
                case SWING_ARC: {
                    // Direction is player→projectile midpoint (sword has no velocity)
                    float dirX = p.x - player.x;
                    float dirY = p.y - player.y;
                    float swingAngle = (float)(Math.atan2(dirY, dirX) * MathUtils.radiansToDegrees);
                    boolean flipX = dirX < 0;
                    float vis = p.size * 1.3f;
                    batch.draw(texSword, p.x - vis/2f, p.y - vis/2f,
                               vis/2f, vis/2f, vis, vis, 1f, 1f, swingAngle,
                               0, 0, texSword.getWidth(), texSword.getHeight(), flipX, false);
                    break;
                }
                case FIREBALL: {
                    // +180° because texture head points LEFT
                    float vis = p.size * 2f;
                    batch.draw(texFireball, p.x - vis/2f, p.y - vis/2f,
                               vis/2f, vis/2f, vis, vis, 1f, 1f, angle + 180f,
                               0, 0, texFireball.getWidth(), texFireball.getHeight(), false, false);
                    break;
                }
                case BULLET: {
                    if (p.source == com.softchaos.utils.WeaponType.BOW) {
                        // Bow fires arrows — rotate to face direction
                        float vis = p.size * 12f;
                        batch.draw(texArrow, p.x - vis/2f, p.y - vis/2f,
                                   vis/2f, vis/2f, vis, vis, 1f, 1f, angle,
                                   0, 0, texArrow.getWidth(), texArrow.getHeight(), false, false);
                    } else {
                        // Minigun etc. — small bullet, no rotation needed
                        float vis = p.size * 2.5f;
                        batch.draw(texBullet, p.x - vis/2f, p.y - vis/2f, vis, vis);
                    }
                    break;
                }
                case ROCKET: {
                    float vis = p.size * 2f;
                    batch.draw(texRocket, p.x - vis/2f, p.y - vis/2f,
                               vis/2f, vis/2f, vis, vis, 1f, 1f, angle - 90f,
                               0, 0, texRocket.getWidth(), texRocket.getHeight(), false, false);
                    break;
                }
                case POTATO: {
                    float vis = p.size * 3.8f;
                    batch.draw(texPotato, p.x - vis/2f, p.y - vis/2f, vis, vis);
                    break;
                }
                case SHURIKEN: {
                    float vis = p.size * 5f;
                    batch.draw(texShuriken, p.x - vis/2f, p.y - vis/2f,
                               vis/2f, vis/2f, vis, vis, 1f, 1f, angle,
                               0, 0, texShuriken.getWidth(), texShuriken.getHeight(), false, false);
                    break;
                }
                default: {
                    // DOUBLE_BULLET
                    float vis = p.size * 3.8f;
                    batch.draw(texBullet, p.x - vis/2f, p.y - vis/2f, vis, vis);
                    break;
                }
            }
        }
        // Player sprite
        batch.draw(playerFrames[playerFrameIdx], player.x - 1.046875f, player.y - 1.046875f, 2.09375f, 2.09375f);
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
            shapeRenderer.rect(0, 0, hudCamera.viewportWidth, hudCamera.viewportHeight);
            shapeRenderer.end();
            Gdx.gl.glDisable(GL20.GL_BLEND);
        }

        // 6. Pause overlay (drawn last so it appears on top of everything)
        if (isPaused) drawPauseOverlay();
    }

    private void drawPauseOverlay() {
        int   sw = Gdx.graphics.getWidth();
        int   sh = Gdx.graphics.getHeight();
        float mx = Gdx.input.getX();
        float my = sh - Gdx.input.getY();

        // Dark semi-transparent background
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.setProjectionMatrix(hudCamera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0f, 0f, 0f, 0.78f);
        shapeRenderer.rect(0, 0, sw, sh);
        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        batch.setProjectionMatrix(hudCamera.combined);
        batch.begin();
        // Settings icon — top-right corner, same style as main menu
        boolean settHov = pauseSettingsHit.contains(mx, my);
        float   settAlpha = settHov ? 1f : 0.75f;
        float   settSize  = settHov ? 72f * 1.1f : 72f;
        float   settOff   = (settSize - 72f) / 2f;
        batch.setColor(settAlpha, settAlpha, settAlpha, 1f);
        batch.draw(pauseSettingsNormal,
            pauseSettingsHit.x - settOff, pauseSettingsHit.y - settOff, settSize, settSize);
        batch.setColor(1f, 1f, 1f, 1f);
        drawPauseBtn(pauseRetryNormal, pauseRetryHover, pauseRetryHit.contains(mx, my), pauseRetryHit);
        drawPauseBtn(pauseQuitNormal,  pauseQuitHover,  pauseQuitHit.contains(mx, my),  pauseQuitHit);
        batch.end();
    }

    private void drawPauseBtn(Texture normal, Texture hover, boolean isHover, Rectangle hit) {
        if (!isHover) {
            batch.draw(normal, hit.x, hit.y, hit.width, hit.height);
        } else {
            float drawH = hit.height * ((float) hover.getHeight() / normal.getHeight());
            batch.draw(hover, hit.x, hit.y + hit.height / 2f - drawH / 2f, hit.width, drawH);
        }
    }

    public void showUpgradeScreen() {
        if (player.weapons.isEmpty()) return;
        Weapon w = player.weapons.get(MathUtils.random(player.weapons.size - 1));
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

        // HP icon widget — dimensions used in both ShapeRenderer and batch passes
        // Transparent bar interior in hp_icon.png (741x336): X=253-647, Y=145-183
        float hpIconW = 400f, hpIconH = 158f;
        float hpIconY = sh - xpBarH - hpIconH;
        float hpBarX  = hpIconW * 0.341f;   // 253/741
        float hpBarY  = hpIconY + hpIconH * 0.455f; // (336-183)/336
        float hpBarW  = hpIconW * 0.532f;   // (647-253)/741
        float hpBarH  = hpIconH * 0.113f;   // (183-145)/336

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

        // ── HP fill (drawn behind hp_icon.png; transparent bar area shows it) ──
        float r = 1f - hpFrac * 0.5f;
        float g = hpFrac;
        shapeRenderer.setColor(0.12f, 0f, 0f, 1f);
        shapeRenderer.rect(hpBarX, hpBarY, hpBarW, hpBarH);
        shapeRenderer.setColor(r, g, 0.05f, 1f);
        shapeRenderer.rect(hpBarX, hpBarY, hpBarW * hpFrac, hpBarH);
        shapeRenderer.setColor(1f, 1f, 1f, 0.18f);
        shapeRenderer.rect(hpBarX, hpBarY + hpBarH - 3f, hpBarW * hpFrac, 3f);

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
        // HP icon frame drawn on top of fill (transparent bar area reveals fill)
        if (texHpIcon != null)
            batch.draw(texHpIcon, 0f, hpIconY, hpIconW, hpIconH);

        // LVL inside XP bar (right)
        hudFont.getData().setScale(1.3f);
        hudFont.setColor(Color.WHITE);
        String lvlStr = "LVL " + lvl;
        glyphLayout.setText(hudFont, lvlStr);
        hudFont.draw(batch, lvlStr, sw - glyphLayout.width - 10f, xpBarY + xpBarH - 3f);

        // HP numbers centered inside icon bar area (shadow + main text for readability)
        hudFont.getData().setScale(1.3f);
        String hpStr = String.format("%.0f / %.0f", player.hp, player.maxHp);
        glyphLayout.setText(hudFont, hpStr);
        float hpTxtX = hpBarX + (hpBarW - glyphLayout.width) / 2f;
        float hpTxtY = hpBarY + hpBarH - 2f;
        // Shadow
        hudFont.setColor(0f, 0f, 0f, 0.85f);
        hudFont.draw(batch, hpStr, hpTxtX + 1.5f, hpTxtY - 1.5f);
        // Main text
        hudFont.setColor(Color.WHITE);
        hudFont.draw(batch, hpStr, hpTxtX, hpTxtY);

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
                // Icon on left
                Texture wIcon = weaponIcons.get(w.id);
                float iconSz = 52f;
                if (wIcon != null)
                    batch.draw(wIcon, sx + 5f, slotY + (slotH - iconSz) / 2f, iconSz, iconSz);
                // Name + stats on right
                float textX = sx + 62f;
                float textAreaW = slotW - 62f - 4f;
                hudFont.getData().setScale(1.1f);
                hudFont.setColor(Color.WHITE);
                glyphLayout.setText(hudFont, w.name);
                hudFont.draw(batch, w.name, textX + (textAreaW - glyphLayout.width) / 2f, slotY + slotH - 14f);
                hudFont.getData().setScale(0.9f);
                hudFont.setColor(Color.YELLOW);
                String dmgStr = String.format("%.0f DMG", w.damage);
                glyphLayout.setText(hudFont, dmgStr);
                hudFont.draw(batch, dmgStr, textX + (textAreaW - glyphLayout.width) / 2f, slotY + 24f);
                hudFont.getData().setScale(0.85f);
                hudFont.setColor(new Color(0.6f, 0.8f, 1f, 1f));
                String cdStr = String.format("%.1fs CD", w.cooldown);
                glyphLayout.setText(hudFont, cdStr);
                hudFont.draw(batch, cdStr, textX + (textAreaW - glyphLayout.width) / 2f, slotY + 11f);
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

    /** Load one or more textures with Nearest filter. */
    private Texture[] loadN(String... paths) {
        Texture[] arr = new Texture[paths.length];
        for (int i = 0; i < paths.length; i++) {
            arr[i] = new Texture(Gdx.files.internal(paths[i]));
            arr[i].setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        }
        return arr;
    }

    /** Frame count for the walk animation of a regular enemy type. */
    private int enemyFrameCount(EnemyType t) {
        switch (t) {
            case RABBID: case SHARK: case ZOMBIE: return 3;
            case ALIEN:  case ROBOT: case SNIPER: return 2;
            default: return 1;
        }
    }

    /** Current animation frame texture for a regular enemy. */
    private Texture getEnemyFrame(Enemy e) {
        if (e.type == null) return null;
        int fi = enemyAnimFrames[e.type.ordinal()];
        switch (e.type) {
            case RABBID:    return texRabbit[fi % texRabbit.length];
            case ALIEN:     return texAlien [fi % texAlien.length];
            case JELLYFISH: return texJellyfish[0];
            case ROBOT:
            case SNIPER:    return texRobot [fi % texRobot.length];
            case SHARK:     return texShark [fi % texShark.length];
            case ZOMBIE:    return texZombie[fi % texZombie.length];
            default:        return null;
        }
    }

    /** Current animation frame texture for a boss. */
    private Texture getBossFrame(Boss boss) {
        if (boss.type == null) return null;
        switch (boss.type) {
            case SLENDERMAN: return texSlenderman[bossAnimFrame % texSlenderman.length];
            case MEGALODON:  return texMegalodon;
            case PICKLE_RICK: return texPickleRick;
            default:         return null;
        }
    }

    /** Visual draw half-size (world units) for a regular enemy type. */
    private static float enemyDisplaySize(EnemyType t) {
        if (t == null) return 0.9f;
        switch (t) {
            case RABBID:    return 2.0f;
            case SNIPER:    return 1.5f;
            case ALIEN:     return 3.0f;
            case JELLYFISH: return 1.5f;
            case ROBOT:     return 2.5f;
            case SHARK:     return 3.0f;
            case ZOMBIE:    return 3.5f;
            default:        return 0.9f;
        }
    }

    /** Returns true for enemy types whose sprite texture faces LEFT by default (requires inverted flip logic). */
    private static boolean isSpriteFacingLeft(Enemy e) {
        if (e instanceof Boss) {
            Boss b = (Boss) e;
            return b.type == EnemyType.MEGALODON;
        }
        return e.type == EnemyType.SHARK;
    }

    /** Half the sprite display size — kept for legacy callers; HP bars now use enemyDisplaySize directly. */
    private static float enemyHalfSize(EnemyType t) {
        return enemyDisplaySize(t) / 1.5f;
    }

    /** RGB colour for each enemy type. */
    private static float[] enemyColor(EnemyType t) {
        if (t == null) return new float[]{0.85f, 0.15f, 0.15f};
        switch (t) {
            case RABBID:    return new float[]{0.9f,  0.55f, 0.9f};
            case ALIEN:     return new float[]{0.2f,  0.85f, 0.3f};
            case JELLYFISH: return new float[]{0.4f,  0.7f,  1.0f};
            case ROBOT:     return new float[]{0.55f, 0.55f, 0.65f};
            case SHARK:     return new float[]{0.2f,  0.35f, 0.7f};
            case ZOMBIE:    return new float[]{0.35f, 0.55f, 0.25f};
            case SNIPER:    return new float[]{0.9f,  0.8f,  0.1f};  // yellow
            default:        return new float[]{0.85f, 0.15f, 0.15f};
        }
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
        if (player.canAddWeapon()) {
            game.setScreen(new ChestScreen(game, chest, player, chestSystem, this));
        } else {
            // Inventory full: convert chest to upgrade for a random existing weapon
            chestSystem.freeChest(chest);
            Weapon w = player.weapons.get(MathUtils.random(player.weapons.size - 1));
            game.setScreen(new UpgradeScreen(game, w, upgradeSystem, this));
        }
    }

    // --- WaveManager.WaveListener ---
    @Override public void onBossKilled() {
        // Fade to black, then advance to the next location (or WinScreen if last)
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

    @Override
    public void onWaveEnd() {
        // Wave timer hit zero — clear regular enemies, spawn the location boss, play boss music
        enemySpawner.clearNonBossEnemies();
        Boss boss = enemySpawner.spawnBoss(camera.viewportWidth, camera.viewportHeight);
        AudioManager.getInstance().playMusic(MusicType.BOSS);

        // Attach minion spawn callbacks
        if (boss.ai instanceof MegalodonAI) {
            // Ocean: spawns 8 sharks near itself
            ((MegalodonAI) boss.ai).setMinionCallback((bx, by) -> {
                for (int i = 0; i < 8; i++)
                    enemySpawner.spawnMinionAt(EnemyType.SHARK, bx, by);
            });
        } else if (boss.ai instanceof PickleRickAI) {
            // Space: spawns 1 robot + 1 zombie near itself periodically
            ((PickleRickAI) boss.ai).setMinionCallback((bx, by) -> {
                enemySpawner.spawnMinionAt(EnemyType.ROBOT,  bx, by);
                enemySpawner.spawnMinionAt(EnemyType.ZOMBIE, bx, by);
            });
            // Immediately surround Pickle Rick with 6 ranged snipers on spawn
            for (int i = 0; i < 6; i++)
                enemySpawner.spawnMinionAt(EnemyType.SNIPER, boss.x, boss.y);
        }
        // Slenderman (Forest) has no minions
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
        if (playerFrames != null) { for (Texture t : playerFrames) if (t != null) t.dispose(); playerFrames = null; }
        if (backgroundTexture != null) { backgroundTexture.dispose(); backgroundTexture = null; }
        Texture[] projTex = { texArrow, texBullet, texShuriken, texEnemyBullet, texRocket, texFireball, texPotato, texSword };
        for (Texture t : projTex) if (t != null) t.dispose();
        texArrow = texBullet = texShuriken = texEnemyBullet = texRocket = texFireball = texPotato = texSword = null;
        if (weaponIcons != null) {
            for (Texture t : weaponIcons.values()) if (t != null) t.dispose();
            weaponIcons = null;
        }
        if (explosionFrames != null) {
            for (Texture t : explosionFrames) if (t != null) t.dispose();
            explosionFrames = null;
        }
        // Enemy sprites
        Texture[][] enemyGroups = {texRabbit, texAlien, texJellyfish, texRobot, texShark, texZombie, texSlenderman};
        for (Texture[] grp : enemyGroups)
            if (grp != null) for (Texture t : grp) if (t != null) t.dispose();
        texRabbit = texAlien = texJellyfish = texRobot = texShark = texZombie = texSlenderman = null;
        if (texMegalodon  != null) { texMegalodon.dispose();  texMegalodon  = null; }
        if (texPickleRick != null) { texPickleRick.dispose(); texPickleRick = null; }
        if (texChest      != null) { texChest.dispose();      texChest      = null; }
        if (texHpIcon     != null) { texHpIcon.dispose();     texHpIcon     = null; }
        if (hudFont       != null) { hudFont.dispose();       hudFont       = null; }
        if (hudFontBig    != null) { hudFontBig.dispose();    hudFontBig    = null; }
        if (pauseRetryHover  != null) { pauseRetryHover.dispose();  pauseRetryHover  = null; }
        if (pauseRetryNormal != null) { pauseRetryNormal.dispose(); pauseRetryNormal = null; }
        if (pauseQuitNormal  != null) { pauseQuitNormal.dispose();  pauseQuitNormal  = null; }
        if (pauseQuitHover   != null) { pauseQuitHover.dispose();   pauseQuitHover   = null; }
        if (pauseSettingsNormal != null) { pauseSettingsNormal.dispose(); pauseSettingsNormal = null; }
        if (pauseQuitNormal  != null) { pauseQuitNormal.dispose();  pauseQuitNormal  = null; }
        if (pauseQuitHover   != null) { pauseQuitHover.dispose();   pauseQuitHover   = null; }
        player.dispose();
        uiManager.dispose();
    }
}
