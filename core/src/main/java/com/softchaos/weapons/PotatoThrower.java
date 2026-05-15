package com.softchaos.weapons;

import com.badlogic.gdx.utils.Array;
import com.softchaos.entities.Enemy;
import com.softchaos.entities.Player;
import com.softchaos.entities.Projectile;
import com.softchaos.utils.ProjectileType;
import com.softchaos.utils.WeaponRarity;
import com.softchaos.utils.WeaponType;

/** Lobs a potato that applies knockback to all enemies in AOE on explosion. */
public class PotatoThrower extends Weapon {

    public static final float KNOCKBACK_FORCE    = 10f;
    public static final float KNOCKBACK_DURATION = 0.3f;

    public PotatoThrower() {
        id              = "potato_thrower";
        name            = "Potato Thrower";
        type            = WeaponType.POTATO_THROWER;
        rarity          = WeaponRarity.RARE;
        cooldown        = 1.7f;
        damage          = 30f;
        size            = 0.4f;
        projectileCount = 1;
        piercing        = 0;
        projectileSpeed = 7f;
        explosionRadius = 2.5f;
    }

    @Override
    public void fire(Player player, Array<Enemy> enemies, Array<Projectile> projectiles) {
        if (!canFire()) return;

        Enemy target = findNearest(player, enemies);
        if (target == null) return;

        float dx    = target.x - player.x;
        float dy    = target.y - player.y;
        float angle = (float) Math.atan2(dy, dx);
        float spreadStep  = 0.15f;
        float spreadStart = -((projectileCount - 1) * spreadStep) / 2f;
        for (int i = 0; i < projectileCount; i++) {
            float a = angle + spreadStart + i * spreadStep;
            Projectile p = new Projectile();
            p.projectileType  = ProjectileType.POTATO;
            p.source          = type;
            p.x               = player.x;
            p.y               = player.y;
            p.damage          = damage;
            p.size            = size;
            p.piercing        = piercing;
            p.explosionRadius = explosionRadius;
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
