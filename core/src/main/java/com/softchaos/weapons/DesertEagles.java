package com.softchaos.weapons;

import com.badlogic.gdx.utils.Array;
import com.softchaos.entities.Enemy;
import com.softchaos.entities.Player;
import com.softchaos.entities.Projectile;
import com.softchaos.utils.ProjectileType;
import com.softchaos.utils.WeaponRarity;
import com.softchaos.utils.WeaponType;

/** Fires 2 bullets simultaneously toward the nearest enemy. */
public class DesertEagles extends Weapon {

    private static final float SPREAD = 0.15f; // radians between the two bullets

    public DesertEagles() {
        id              = "desert_eagles";
        name            = "Desert Eagles";
        type            = WeaponType.DESERT_EAGLES;
        rarity          = WeaponRarity.RARE;
        cooldown        = 1.05f;
        damage          = 32f;
        size            = 0.25f;
        projectileCount = 2;
        piercing        = 0;
        projectileSpeed = 18f;
        explosionRadius = 0f;
    }

    @Override
    public void fire(Player player, Array<Enemy> enemies, Array<Projectile> projectiles) {
        if (!canFire()) return;

        Enemy target = findNearest(player, enemies);
        if (target == null) return;

        float dx    = target.x - player.x;
        float dy    = target.y - player.y;
        float angle = (float) Math.atan2(dy, dx);

        float spreadTotal = (projectileCount - 1) * SPREAD;
        for (int i = 0; i < projectileCount; i++) {
            float a = angle - spreadTotal / 2f + i * SPREAD;
            Projectile p = new Projectile();
            p.projectileType = ProjectileType.DOUBLE_BULLET;
            p.source  = type;
            p.x       = player.x;
            p.y       = player.y;
            p.damage  = damage;
            p.size    = size;
            p.piercing = piercing;
            p.setVelocity(a, projectileSpeed);
            projectiles.add(p);
        }

        resetCooldown();
    }

    private Enemy findNearest(Player player, Array<Enemy> enemies) {
        Enemy nearest = null;
        float minDist = Float.MAX_VALUE;
        for (Enemy e : enemies) {
            float dx = e.x - player.x;
            float dy = e.y - player.y;
            float d  = dx * dx + dy * dy;
            if (d < minDist) { minDist = d; nearest = e; }
        }
        return nearest;
    }
}
