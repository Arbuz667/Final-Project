package com.softchaos.systems;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.softchaos.ai.ChaseAI;
import com.softchaos.entities.Enemy;
import com.softchaos.utils.EnemyType;
import com.softchaos.utils.LocationType;

public class EnemySpawner {

    private static final float INITIAL_INTERVAL = 1.5f;  // seconds between spawns
    private static final float MIN_INTERVAL     = 0.3f;  // floor for scaling
    private static final float SCALE_RATE       = 0.003f; // reduction per second

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
        e.maxHp  = 30f;
        e.hp     = e.maxHp;
        e.speed  = 3.5f;
        e.damage = 5f;
        e.xpDrop = 8;

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
        // TODO M2: use actual camera/viewport bounds
        float screenW = com.softchaos.utils.Constants.SCREEN_WIDTH  / com.softchaos.utils.Constants.PPM;
        float screenH = com.softchaos.utils.Constants.SCREEN_HEIGHT / com.softchaos.utils.Constants.PPM;
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
            case CITY:   return EnemyType.ROBOT;
            case OCEAN:  return EnemyType.SHARK;
            default:     return EnemyType.ALIEN;
        }
    }

    public void freeEnemy(Enemy e) {
        activeEnemies.removeValue(e, true);
        enemyPool.free(e);
    }
}
