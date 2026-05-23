package com.softchaos.systems;

import com.badlogic.gdx.math.MathUtils;
import com.softchaos.utils.Constants;
import com.softchaos.utils.LocationType;

/**
 * Manages the wave timer, triggers EnemySpawner, detects rare events, and
 * switches to endless mode when the countdown reaches zero.
 */
public class WaveManager {

    private LocationType currentLocation;
    private float waveTimer;        // counts down from wave duration
    private float totalTime;        // total elapsed session time
    private boolean endless;
    private boolean bossPhase;      // true while boss is alive (no regular spawns)
    private boolean bossKilled;
    private boolean waveActive;

    private EnemySpawner enemySpawner;

    // Callback reference — set by GameScreen
    private WaveListener listener;

    public interface WaveListener {
        void onBossKilled();
        void onWaveEnd();
    }

    public WaveManager(EnemySpawner enemySpawner) {
        this.enemySpawner = enemySpawner;
        endless    = false;
        bossKilled = false;
        waveActive = false;
    }

    public void setListener(WaveListener listener) {
        this.listener = listener;
    }

    /** Start a timed wave for the given location. */
    public void startWave(LocationType location, float durationSeconds) {
        this.currentLocation = location;
        waveTimer  = durationSeconds;
        totalTime  = 0f;
        endless    = false;
        waveActive = true;
        enemySpawner.setLocation(location);
    }

    public void update(float delta) {
        if (!waveActive) return;

        totalTime += delta;
        if (!bossPhase) {
            enemySpawner.update(delta, totalTime);
            checkRareEvent();
        }

        if (!endless) {
            waveTimer -= delta;
            if (waveTimer <= 0) {
                startBossPhase();
            }
        }
    }

    private void checkRareEvent() {
        if (MathUtils.random() < Constants.RARE_EVENT_CHANCE) {
            // TODO M4: enemySpawner.spawnCartman()
        }
    }

    /** Timer reached zero — stop regular spawning and signal GameScreen to spawn the boss. */
    private void startBossPhase() {
        endless   = true;
        bossPhase = true;
        if (listener != null) listener.onWaveEnd();
    }

    /** Called when the boss is defeated — triggers win screen. */
    public void onBossKilled() {
        bossKilled = true;
        waveActive = false;
        if (listener != null) listener.onBossKilled();
    }

    public float getWaveTimer()   { return waveTimer; }
    public float getTotalTime()   { return totalTime; }
    public boolean isEndless()    { return endless; }
    public boolean isBossKilled() { return bossKilled; }
}
