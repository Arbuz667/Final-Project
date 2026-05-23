package com.softchaos.weapons;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.softchaos.entities.Enemy;
import com.softchaos.entities.Player;
import com.softchaos.entities.Projectile;
import com.softchaos.utils.ProjectileType;
import com.softchaos.utils.WeaponRarity;
import com.softchaos.utils.WeaponType;

/** Fires a shuriken that bounces between up to 3 enemies. */
public class Shurikens extends Weapon {

    public static final int MAX_BOUNCES = 3;

    public Shurikens() {
        id              = "shurikens";
        name            = "Shurikens";
        type            = WeaponType.SHURIKENS;
        rarity          = WeaponRarity.RARE;
        cooldown        = 1.3f;
        damage          = 30f;
        size            = 0.25f;
        projectileCount = 1;
        piercing        = -1;   // bounce logic controls lifetime
        projectileSpeed = 12f;
        explosionRadius = 0f;
    }

    @Override
    public void fire(Player player, Array<Enemy> enemies, Array<Projectile> projectiles) {
        if (!canFire()) return;

        for (int i = 0; i < projectileCount; i++) {
            Enemy target = findNearestExcluding(player, enemies, null);
            if (target == null) break;

            Projectile p = new Projectile();
            p.projectileType = ProjectileType.SHURIKEN;
            p.source     = type;
            p.x          = player.x;
            p.y          = player.y;
            p.damage     = damage;
            p.size       = size;
            p.piercing   = -1;           // bounce logic controls deactivation
            p.bouncesLeft = MAX_BOUNCES;
            p.setVelocityToward(target.x, target.y, projectileSpeed);
            projectiles.add(p);
        }

        resetCooldown();
    }

    private Enemy findNearestExcluding(Player player, Array<Enemy> enemies, Enemy exclude) {
        Enemy nearest = null;
        float minDist = Float.MAX_VALUE;
        for (Enemy e : enemies) {
            if (e == exclude) continue;
            float dx = e.x - player.x;
            float dy = e.y - player.y;
            float d  = dx * dx + dy * dy;
            if (d < minDist) { minDist = d; nearest = e; }
        }
        return nearest;
    }
}
