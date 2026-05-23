package com.softchaos.weapons;

import com.badlogic.gdx.utils.Array;
import com.softchaos.entities.Enemy;
import com.softchaos.entities.Player;
import com.softchaos.entities.Projectile;
import com.softchaos.utils.ProjectileType;
import com.softchaos.utils.WeaponRarity;
import com.softchaos.utils.WeaponType;

/** Fires 1 arrow toward the nearest enemy. */
public class Bow extends Weapon {

    public Bow() {
        id              = "iron_bow";
        name            = "Iron Bow";
        type            = WeaponType.BOW;
        rarity          = WeaponRarity.COMMON;
        cooldown        = 2.0f;
        damage          = 65f;
        size            = 0.3f;
        projectileCount = 1;
        piercing        = 0;
        projectileSpeed = 22f;
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
        float spreadStep  = 0.12f;
        float spreadStart = -((projectileCount - 1) * spreadStep) / 2f;
        for (int i = 0; i < projectileCount; i++) {
            float a = angle + spreadStart + i * spreadStep;
            Projectile p = new Projectile();
            p.projectileType = ProjectileType.BULLET;
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
