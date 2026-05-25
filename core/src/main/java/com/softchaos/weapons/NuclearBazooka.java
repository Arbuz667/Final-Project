package com.softchaos.weapons;

import com.badlogic.gdx.utils.Array;
import com.softchaos.entities.Enemy;
import com.softchaos.entities.Player;
import com.softchaos.entities.Projectile;
import com.softchaos.utils.ProjectileType;
import com.softchaos.utils.WeaponType;

/** Slow projectile that causes a large AOE explosion (radius 5.0) on hit. */
public class NuclearBazooka extends Weapon {

    public NuclearBazooka() {
        id              = "nuclear_bazooka";
        name            = "Nuclear Bazooka";
        type            = WeaponType.NUCLEAR_BAZOOKA;
        cooldown        = 3f;
        damage          = 60f;
        size            = 0.5f;
        projectileCount = 1;
        piercing        = 0;
        projectileSpeed = 4f;   // slow
        explosionRadius = 5.0f;
    }

    @Override
    public void fire(Player player, Array<Enemy> enemies, Array<Projectile> projectiles) {
        if (!canFire()) return;

        Enemy target = findNearest(player, enemies);
        if (target == null) return;

        Projectile p = new Projectile();
        p.projectileType = ProjectileType.ROCKET;
        p.source         = type;
        p.x              = player.x;
        p.y              = player.y;
        p.damage         = damage;
        p.size           = size;
        p.piercing       = 0;
        p.explosionRadius = explosionRadius;
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
