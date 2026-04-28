package com.softchaos.weapons.cringe;

import com.badlogic.gdx.utils.Array;
import com.softchaos.entities.Enemy;
import com.softchaos.entities.Player;
import com.softchaos.entities.Projectile;
import com.softchaos.utils.Constants;
import com.softchaos.utils.WeaponType;
import com.softchaos.weapons.Weapon;

/**
 * Does NOT create projectiles.
 * Picks 4 random enemies from the active list and calls takeCringeDamage() on each.
 */
public class SixSeven extends Weapon {

    private static final int TARGETS = 4;

    public SixSeven() {
        id              = "six_seven";
        name            = "6:7";
        type            = WeaponType.SIX_SEVEN;
        cooldown        = 1.0f;
        damage          = 0f; // damage is applied via CringeMeter
        size            = 0f;
        projectileCount = 0;
        piercing        = 0;
        projectileSpeed = 0f;
        explosionRadius = 0f;
    }

    @Override
    public void fire(Player player, Array<Enemy> enemies, Array<Projectile> projectiles) {
        if (!canFire() || enemies.isEmpty()) return;

        int count = Math.min(TARGETS, enemies.size);
        // Shuffle a copy to pick random enemies
        Array<Enemy> pool = new Array<>(enemies);
        pool.shuffle();

        for (int i = 0; i < count; i++) {
            pool.get(i).takeCringeDamage(Constants.SIX_SEVEN_CRINGE_DAMAGE);
        }

        resetCooldown();
    }
}
