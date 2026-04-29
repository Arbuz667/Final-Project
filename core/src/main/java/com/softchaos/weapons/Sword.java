package com.softchaos.weapons;

import com.badlogic.gdx.utils.Array;
import com.softchaos.entities.Enemy;
import com.softchaos.entities.Player;
import com.softchaos.entities.Projectile;
import com.softchaos.utils.ProjectileType;
import com.softchaos.utils.WeaponType;

/** Swing arc toward the nearest enemy. Piercing = unlimited. */
public class Sword extends Weapon {

    public Sword() {
        id             = "sword";
        name           = "Sword";
        type           = WeaponType.SWORD;
        cooldown       = 0.8f;
        damage         = 25f;
        size           = 1.2f;
        projectileCount = 1;
        piercing       = -1; // hits all enemies in arc
        projectileSpeed = 0f; // arc stays in place briefly
        explosionRadius = 0f;
    }

    @Override
    public void fire(Player player, Array<Enemy> enemies, Array<Projectile> projectiles) {
        if (!canFire()) return;

        Enemy target = findNearest(player, enemies);
        if (target == null) return;

        Projectile p = new Projectile();
        p.projectileType = ProjectileType.SWING_ARC;
        p.source  = type;
        p.x       = player.x;
        p.y       = player.y;
        p.damage  = damage;
        p.size    = size;
        p.piercing = piercing;
        p.setVelocityToward(target.x, target.y, projectileSpeed);
        p.lifetime = 0.4f; // melee swing arc lasts 0.4s
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
