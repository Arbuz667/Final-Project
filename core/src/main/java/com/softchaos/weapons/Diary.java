package com.softchaos.weapons;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.softchaos.entities.Enemy;
import com.softchaos.entities.Player;
import com.softchaos.entities.Projectile;
import com.softchaos.utils.ProjectileType;
import com.softchaos.utils.WeaponType;

/** Fires 3 fireballs in random directions. Each fireball explodes on hit. */
public class Diary extends Weapon {

    public Diary() {
        id              = "diary";
        name            = "Diary";
        type            = WeaponType.DIARY;
        cooldown        = 1.5f;
        damage          = 20f;
        size            = 0.4f;
        projectileCount = 3;
        piercing        = 0;
        projectileSpeed = 8f;
        explosionRadius = 1.5f;
    }

    @Override
    public void fire(Player player, Array<Enemy> enemies, Array<Projectile> projectiles) {
        if (!canFire()) return;

        for (int i = 0; i < projectileCount; i++) {
            float angle = MathUtils.random(0f, MathUtils.PI2);
            Projectile p = new Projectile();
            p.projectileType = ProjectileType.FIREBALL;
            p.source         = type;
            p.x              = player.x;
            p.y              = player.y;
            p.damage         = damage;
            p.size           = size;
            p.piercing       = 0;
            p.explosionRadius = explosionRadius;
            p.setVelocity(angle, projectileSpeed);
            projectiles.add(p);
        }

        resetCooldown();
    }
}
