package com.softchaos.weapons;

import com.badlogic.gdx.utils.Array;
import com.softchaos.entities.Enemy;
import com.softchaos.entities.Player;
import com.softchaos.entities.Projectile;
import com.softchaos.utils.ProjectileType;
import com.softchaos.utils.WeaponType;

/** Fires 1 arrow toward the nearest enemy. */
public class Bow extends Weapon {

    public Bow() {
        id              = "iron_bow";
        name            = "Iron Bow";
        type            = WeaponType.BOW;
        cooldown        = 0.5f;
        damage          = 18f;
        size            = 0.3f;
        projectileCount = 1;
        piercing        = 0;
        projectileSpeed = 14f;
        explosionRadius = 0f;
    }

    @Override
    public void fire(Player player, Array<Enemy> enemies, Array<Projectile> projectiles) {
        if (!canFire()) return;

        Enemy target = findNearest(player, enemies);
        if (target == null) return;

        Projectile p = new Projectile();
        p.projectileType = ProjectileType.BULLET;
        p.source  = type;
        p.x       = player.x;
        p.y       = player.y;
        p.damage  = damage;
        p.size    = size;
        p.piercing = piercing;
        p.setVelocityToward(target.x, target.y, projectileSpeed);
        projectiles.add(p);

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
