package com.softchaos.weapons;

import com.badlogic.gdx.utils.Array;
import com.softchaos.entities.Enemy;
import com.softchaos.entities.Player;
import com.softchaos.entities.Projectile;
import com.softchaos.utils.WeaponRarity;
import com.softchaos.utils.WeaponType;

/** Base class for all weapons. Parameters are loaded from weapons.json via WeaponConfig. */
public abstract class Weapon {

    public String id;
    public String name;
    public WeaponType type;
    public WeaponRarity rarity;

    public float cooldown;
    public float currentCooldown;
    public float damage;
    public float size;
    public int   projectileCount;
    public int   piercing;
    public float projectileSpeed;
    public float explosionRadius;
    public String iconPath = "weapons/icon for weapon.png";

    /** Tick the cooldown timer. */
    public void update(float delta) {
        if (currentCooldown > 0) {
            currentCooldown -= delta;
        }
    }

    public boolean canFire() {
        return currentCooldown <= 0;
    }

    public void resetCooldown() {
        currentCooldown = cooldown;
    }

    /**
     * Fire the weapon. Implementations create Projectile objects (or call
     * enemy.takeCringeDamage() in case of SixSeven) and add them to the
     * provided projectile list managed by GameScreen.
     */
    public abstract void fire(Player player, Array<Enemy> enemies,
                              Array<Projectile> projectiles);

    /**
     * Called every frame regardless of cooldown.
     * Override for passive / aura weapons (e.g. SixSeven) that need per-frame logic.
     */
    public void tickPassive(float delta, Player player, Array<Enemy> enemies) { }
}
