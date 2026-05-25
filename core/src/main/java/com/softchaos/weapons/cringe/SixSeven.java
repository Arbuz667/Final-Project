package com.softchaos.weapons.cringe;

import com.badlogic.gdx.utils.Array;
import com.softchaos.entities.Boss;
import com.softchaos.entities.Enemy;
import com.softchaos.entities.Player;
import com.softchaos.entities.Projectile;
import com.softchaos.utils.WeaponRarity;
import com.softchaos.utils.WeaponType;
import com.softchaos.weapons.Weapon;

/**
 * Aura weapon — no projectiles.
 * Targets up to {@code projectileCount} random enemies within {@link #RADIUS} world units.
 * Each target's cringe meter fills from 0 to 100 % over {@link #FILL_TIME} seconds,
 * then the enemy is instantly killed.
 * Targets are re-evaluated every {@link #RETARGET_INTERVAL} seconds.
 * The active target list is public so GameScreen can render the zone and cringe bars.
 */
public class SixSeven extends Weapon {

    /** Radius of the aura zone in world units. */
    public static final float RADIUS            = 10f;
    /** Seconds to fill cringe meter from 0 to 100 %. */
    private static final float FILL_TIME         = 3f;
    /** Per-second cringe fill rate (% / s). */
    private static final float FILL_RATE         = 100f / FILL_TIME;
    /** How often (seconds) targets are re-evaluated. */
    private static final float RETARGET_INTERVAL = 3f;

    /** Enemies currently being targeted. Read by GameScreen for rendering. */
    public final Array<Enemy> targets = new Array<>();

    private float retargetTimer = RETARGET_INTERVAL; // fire immediately on first tick

    public SixSeven() {
        id              = "six_seven";
        name            = "6:7";
        type            = WeaponType.SIX_SEVEN;
        rarity          = WeaponRarity.LEGENDARY;
        cooldown        = 0f;   // unused — all logic in tickPassive
        damage          = 0f;
        size            = RADIUS;
        projectileCount = 3;    // default: 3 simultaneous targets (upgradeable)
        piercing        = 0;
        projectileSpeed = 0f;
        explosionRadius = 0f;
        iconPath        = "weapons/67.png";
    }

    // ── canFire / fire are intentionally unused ──────────────────────────────

    @Override
    public boolean canFire() { return false; }

    @Override
    public void fire(Player player, Array<Enemy> enemies, Array<Projectile> projectiles) { }

    // ── Core logic (called every frame by WeaponSystem) ──────────────────────

    @Override
    public void tickPassive(float delta, Player player, Array<Enemy> enemies) {
        float r2 = RADIUS * RADIUS;

        // 1. Remove targets that died or left the radius
        boolean slotFreed = false;
        for (int i = targets.size - 1; i >= 0; i--) {
            Enemy t = targets.get(i);
            if (t.isDead() || !enemies.contains(t, true) || distSq(player, t) > r2) {
                t.cringeMeter = 0f;
                targets.removeIndex(i);
                slotFreed = true;
            }
        }

        // 2. Immediately fill any open slots so we always target up to projectileCount enemies
        if (slotFreed || targets.size < projectileCount) {
            fillSlots(player, enemies, r2);
        }

        // 3. Full re-shuffle periodically (rotates to different enemies)
        retargetTimer += delta;
        if (retargetTimer >= RETARGET_INTERVAL) {
            retargetTimer = 0f;
            selectTargets(player, enemies);
        }

        // 4. Fill cringe meter on current targets
        for (Enemy t : targets) {
            t.cringeMeter += FILL_RATE * delta;
            if (t.cringeMeter >= 100f) {
                t.cringeMeter = 100f;
                t.hp = 0f; // instant kill
            }
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Full re-shuffle: resets cringe on de-selected enemies and picks a fresh random set.
     * Called periodically to rotate targets.
     */
    private void selectTargets(Player player, Array<Enemy> enemies) {
        float r2 = RADIUS * RADIUS;

        // Collect living non-boss enemies in range
        Array<Enemy> inRange = new Array<>();
        for (Enemy e : enemies) {
            if (!(e instanceof Boss) && !e.isDead() && distSq(player, e) <= r2) inRange.add(e);
        }
        inRange.shuffle();

        // Reset cringe for enemies that will no longer be targeted
        int newCount = Math.min(projectileCount, inRange.size);
        for (Enemy old : targets) {
            boolean reSelected = false;
            for (int i = 0; i < newCount; i++) {
                if (inRange.get(i) == old) { reSelected = true; break; }
            }
            if (!reSelected) old.cringeMeter = 0f;
        }

        targets.clear();
        for (int i = 0; i < newCount; i++) targets.add(inRange.get(i));
    }

    /**
     * Fills open target slots immediately (without resetting existing targets' cringe meters).
     * Only adds enemies that are not already being targeted.
     */
    private void fillSlots(Player player, Array<Enemy> enemies, float r2) {
        if (targets.size >= projectileCount) return;

        Array<Enemy> candidates = new Array<>();
        for (Enemy e : enemies) {
            if (!(e instanceof Boss) && !e.isDead() && distSq(player, e) <= r2
                    && !targets.contains(e, true)) {
                candidates.add(e);
            }
        }
        candidates.shuffle();

        for (int i = 0; i < candidates.size && targets.size < projectileCount; i++) {
            targets.add(candidates.get(i));
        }
    }

    private static float distSq(Player player, Enemy e) {
        float dx = e.x - player.x, dy = e.y - player.y;
        return dx * dx + dy * dy;
    }
}

