package com.softchaos.weapons;

import com.badlogic.gdx.utils.Array;
import com.softchaos.entities.Enemy;
import com.softchaos.entities.Player;
import com.softchaos.entities.Projectile;
import com.softchaos.utils.ProjectileType;
import com.softchaos.utils.WeaponRarity;
import com.softchaos.utils.WeaponType;

/**
 * Spell-book. Fires a slow fireball toward the nearest enemy.
 * Explodes on impact with a small AOE radius.
 * Weaker version of NuclearBazooka — good early-game area weapon.
 */
public class Diary extends Weapon {

    public Diary() {
        id              = "diary";
        name            = "Diary";
        type            = WeaponType.DIARY;
        rarity          = WeaponRarity.COMMON;
        cooldown        = 1.8f;
        damage          = 25f;
        size            = 0.45f;
        projectileCount = 1;
        piercing        = 0;
        projectileSpeed = 3f;    // noticeably slow — fireball feel
        explosionRadius = 1.8f;  // small AOE (NuclearBazooka has 5.0)
    }

    @Override
    public void fire(Player player, Array<Enemy> enemies, Array<Projectile> projectiles) {
        if (!canFire()) return;

        Enemy target = findNearest(player, enemies);
        if (target == null) return;

        float dx    = target.x - player.x;
        float dy    = target.y - player.y;
        float angle = (float) Math.atan2(dy, dx);

        Projectile p  = new Projectile();
        p.projectileType  = ProjectileType.FIREBALL;
        p.source          = type;
        p.x               = player.x;
        p.y               = player.y;
        p.damage          = damage;
        p.size            = size;
        p.piercing        = piercing;
        p.explosionRadius = explosionRadius;
        p.setVelocity(angle, projectileSpeed);
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
