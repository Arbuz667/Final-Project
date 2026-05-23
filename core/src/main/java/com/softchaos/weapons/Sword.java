package com.softchaos.weapons;

import com.badlogic.gdx.utils.Array;
import com.softchaos.entities.Enemy;
import com.softchaos.entities.Player;
import com.softchaos.entities.Projectile;
import com.softchaos.utils.ProjectileType;
import com.softchaos.utils.WeaponRarity;
import com.softchaos.utils.WeaponType;

/** Melee swing: attacks nearest enemy within range, hitbox placed toward target. */
public class Sword extends Weapon {

    private static final float MELEE_RANGE = 2.5f; // world units

    public Sword() {
        id             = "sword";
        name           = "Sword";
        type           = WeaponType.SWORD;
        rarity         = WeaponRarity.COMMON;
        cooldown       = 0.85f;
        damage         = 20f;
        size           = 1.5f;  // hitbox size
        projectileCount = 1;
        piercing       = -1;    // hits all enemies in arc
        projectileSpeed = 0f;
        explosionRadius = 0f;
    }

    @Override
    public void fire(Player player, Array<Enemy> enemies, Array<Projectile> projectiles) {
        if (!canFire()) return;

        Enemy target = findNearestInRange(player, enemies, MELEE_RANGE);
        if (target == null) return; // no enemy close enough — wait

        // Place hitbox halfway between player and enemy
        Projectile p = new Projectile();
        p.projectileType = ProjectileType.SWING_ARC;
        p.source   = type;
        p.x        = (player.x + target.x) / 2f;
        p.y        = (player.y + target.y) / 2f;
        p.damage   = damage;
        p.size     = size;
        p.piercing = piercing;
        p.lifetime = 0.25f; // flash briefly and disappear
        projectiles.add(p);

        resetCooldown();
    }

    private Enemy findNearestInRange(Player player, Array<Enemy> enemies, float maxRange) {
        Enemy nearest = null;
        float minDist = maxRange * maxRange; // compare squared distances
        for (Enemy e : enemies) {
            float dx = e.x - player.x;
            float dy = e.y - player.y;
            float d  = dx * dx + dy * dy;
            if (d < minDist) { minDist = d; nearest = e; }
        }
        return nearest;
    }
}
