package com.softchaos.weapons;

import com.badlogic.gdx.utils.Array;
import com.softchaos.entities.Enemy;
import com.softchaos.entities.Player;
import com.softchaos.entities.Projectile;
import com.softchaos.utils.ProjectileType;
import com.softchaos.utils.WeaponType;

/** Lobs a potato that applies knockback to all enemies in AOE on explosion. */
public class PotatoThrower extends Weapon {

    public static final float KNOCKBACK_FORCE    = 10f;
    public static final float KNOCKBACK_DURATION = 0.3f;

    public PotatoThrower() {
        id              = "potato_thrower";
        name            = "Potato Thrower";
        type            = WeaponType.POTATO_THROWER;
        cooldown        = 1.2f;
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

        Projectile p = new Projectile();
        p.projectileType  = ProjectileType.POTATO;
        p.source          = type;
        p.x               = player.x;
        p.y               = player.y;
        p.damage          = damage;
        p.size            = size;
        p.piercing        = 0;
        p.explosionRadius = explosionRadius;
        p.setVelocityToward(target.x, target.y, projectileSpeed);
        projectiles.add(p);

        resetCooldown();
        // TODO M3: onExplosion() applies knockback to all enemies in radius via GameScreen
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
