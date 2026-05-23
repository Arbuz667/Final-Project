package com.softchaos.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.softchaos.ai.ChaseAI;
import com.softchaos.ai.MegalodonAI;
import com.softchaos.ai.PickleRickAI;
import com.softchaos.ai.SlendermanAI;
import com.softchaos.entities.Boss;
import com.softchaos.entities.Enemy;
import com.softchaos.utils.EnemyType;
import com.softchaos.utils.LocationType;

public class EnemySpawner {

    private static final float INITIAL_INTERVAL = 0.8f;  // seconds between spawns
    private static final float MIN_INTERVAL     = 0.15f; // floor for scaling
    private static final float SCALE_RATE       = 0.005f; // reduction per second

    private final Array<Enemy> activeEnemies;
    private float spawnTimer;
    private float spawnInterval;
    private LocationType location;

    // Object pool to avoid GC pressure
    private final Pool<Enemy> enemyPool = new Pool<Enemy>() {
        @Override protected Enemy newObject() { return new Enemy(); }
    };

    public EnemySpawner(Array<Enemy> activeEnemies) {
        this.activeEnemies = activeEnemies;
        spawnInterval      = INITIAL_INTERVAL;
    }

    public void setLocation(LocationType location) {
        this.location = location;
    }

    public void update(float delta, float sessionTime) {
        scaleDifficulty(sessionTime);
        spawnTimer += delta;
        if (spawnTimer >= spawnInterval) {
            spawnEnemy(getEnemyTypeForLocation());
            spawnTimer = 0f;
        }
    }

    public void spawnEnemy(EnemyType type) {
        Enemy e = enemyPool.obtain();
        e.type = type;
        // TODO M2: load stats from EnemyConfig via AssetLoader
        e.maxHp  = 100f;
        e.hp     = e.maxHp;
        e.speed  = 3.5f;
        e.damage = 5f;
        e.xpDrop = 8;
        e.chestDropChance = 0.01f;  // 1% chance to drop a gold chest on death
        e.chestType = com.softchaos.utils.ChestType.GOLD;

        // TODO M2: assign AI based on EnemyConfig.aiClass
        e.ai = new ChaseAI();

        float[] spawn = getSpawnPoint();
        e.x = spawn[0];
        e.y = spawn[1];
        e.hitbox.setPosition(e.x, e.y);
        activeEnemies.add(e);
    }

    /** Returns a random position just outside the visible screen edges. */
    public float[] getSpawnPoint() {
        float screenW = Gdx.graphics.getWidth()  / com.softchaos.utils.Constants.PPM;
        float screenH = Gdx.graphics.getHeight() / com.softchaos.utils.Constants.PPM;
        int side = MathUtils.random(3);
        float margin = 1.5f;
        switch (side) {
            case 0: return new float[]{ MathUtils.random(0, screenW), screenH + margin };
            case 1: return new float[]{ MathUtils.random(0, screenW), -margin };
            case 2: return new float[]{ -margin,          MathUtils.random(0, screenH) };
            default:return new float[]{ screenW + margin, MathUtils.random(0, screenH) };
        }
    }

    /** Reduces spawn interval over time to scale difficulty. */
    private void scaleDifficulty(float sessionTime) {
        spawnInterval = Math.max(MIN_INTERVAL, INITIAL_INTERVAL - sessionTime * SCALE_RATE);
    }

    private EnemyType getEnemyTypeForLocation() {
        // TODO M3: choose type based on location + wave progression
        if (location == null) return EnemyType.ALIEN;
        switch (location) {
            case FOREST: return EnemyType.ALIEN;
            case OCEAN:  return EnemyType.SHARK;
            case SPACE:  return EnemyType.ROBOT;
            default:     return EnemyType.ALIEN;
        }
    }

    public void freeEnemy(Enemy e) {
        activeEnemies.removeValue(e, true);
        if (!(e instanceof Boss)) {
            enemyPool.free(e);
        }
    }

    /** Removes all regular enemies from the field (called when boss phase begins). */
    public void clearNonBossEnemies() {
        for (int i = activeEnemies.size - 1; i >= 0; i--) {
            Enemy e = activeEnemies.get(i);
            if (!(e instanceof Boss)) {
                enemyPool.free(e);
                activeEnemies.removeIndex(i);
            }
        }
    }

    /**
     * Spawns a random boss (Megalodon / PickleRick / Slenderman) at the world centre.
     * Phase thresholds: phase 2 at 66% HP, phase 3 at 33% HP.
     */
    public Boss spawnBoss(float worldW, float worldH) {
        Boss boss = new Boss();
        int roll = MathUtils.random(2);
        switch (roll) {
            case 0:
                boss.ai     = new MegalodonAI();
                boss.maxHp  = 750f;
                boss.speed  = 3f;
                boss.damage = 15f;
                break;
            case 1:
                boss.ai     = new PickleRickAI();
                boss.maxHp  = 600f;
                boss.speed  = 4.5f;
                boss.damage = 10f;
                break;
            default:
                boss.ai     = new SlendermanAI();
                boss.maxHp  = 550f;
                boss.speed  = 2f;
                boss.damage = 20f;
                break;
        }
        boss.hp              = boss.maxHp;
        boss.xpDrop          = 200;
        boss.chestDropChance = 0f;
        boss.hitbox          = new Rectangle(0, 0, 1.5f, 1.5f);
        boss.x = worldW / 2f;
        boss.y = worldH / 2f;
        boss.hitbox.setPosition(boss.x - 0.75f, boss.y - 0.75f);
        activeEnemies.add(boss);
        return boss;
    }
}
