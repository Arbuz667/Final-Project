package com.softchaos.weapons;

import com.badlogic.gdx.utils.Array;
import com.softchaos.config.UpgradeConfig;
import com.softchaos.entities.Enemy;
import com.softchaos.entities.Player;
import com.softchaos.entities.Projectile;
import com.softchaos.utils.WeaponType;

/**
 * Manages all weapons held by the player.
 * Called each frame from GameScreen to update cooldowns and trigger auto-fire.
 */
public class WeaponSystem {

    public void update(float delta, Player player, Array<Enemy> enemies,
                       Array<Projectile> projectiles) {
        for (Weapon w : player.weapons) {
            w.update(delta);
            if (w.canFire() && !enemies.isEmpty()) {
                w.fire(player, enemies, projectiles);
            }
        }
    }

    /** Apply an upgrade's multipliers/additions to a weapon. */
    public void applyUpgrade(Weapon w, UpgradeConfig upgrade) {
        w.damage          *= upgrade.damageMultiplier;
        w.cooldown        *= upgrade.cooldownMultiplier;
        w.size            *= upgrade.sizeMultiplier;
        w.explosionRadius *= upgrade.sizeMultiplier;
        w.projectileCount += upgrade.projectileCountAdd;
        w.piercing        += upgrade.piercingAdd;
        // TODO M3: handle specialEffect string
    }
}
