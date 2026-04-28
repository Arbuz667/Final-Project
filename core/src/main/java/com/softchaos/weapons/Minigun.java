package com.softchaos.weapons;

import com.badlogic.gdx.utils.Array;
import com.softchaos.entities.Enemy;
import com.softchaos.entities.Player;
import com.softchaos.entities.Projectile;
import com.softchaos.utils.ProjectileType;
import com.softchaos.utils.WeaponType;

/** Very high fire rate (0.1s cooldown), 1 bullet per shot toward nearest enemy. */
public class Minigun extends Weapon {

    public Minigun() {
        id              = "minigun";
        name            = "Minigun";
        type            = WeaponType.MINIGUN;
        cooldown        = 0.1f;
        damage          = 8f;
        size            = 0.2f;
        projectileCount = 1;
        piercing        = 0;
        projectileSpeed = 20f;
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
        p.piercing = 0;
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
