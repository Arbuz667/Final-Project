package com.softchaos.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.softchaos.utils.ProjectileType;
import com.softchaos.utils.WeaponType;

public class Projectile {

    public float x, y;
    public float velX, velY;
    public float damage;
    public float size;
    public int piercing;           // how many enemies can be hit; -1 = infinite
    public float explosionRadius;  // 0 = no explosion
    public boolean active;
    public ProjectileType projectileType;
    public WeaponType source;
    public Rectangle hitbox;

    private int hitCount = 0;

    public Projectile() {
        hitbox = new Rectangle();
        active = true;
    }

    public void update(float delta) {
        if (!active) return;
        x += velX * delta;
        y += velY * delta;
        hitbox.setPosition(x - size / 2f, y - size / 2f);
        hitbox.setSize(size, size);
    }

    public void onHit(Enemy enemy) {
        enemy.takeDamage(damage);
        hitCount++;

        if (explosionRadius > 0) {
            onExplosion();
        }

        if (piercing >= 0 && hitCount > piercing) {
            active = false;
        }
    }

    /** AOE damage to all enemies within explosionRadius. Called via GameScreen. */
    public void onExplosion() {
        // TODO M3: iterate active enemies, deal damage in radius
        active = false;
    }

    public void render(SpriteBatch batch) {
        // TODO M2: draw projectile sprite
    }

    /** Reset for Pool<Projectile> reuse. */
    public void reset() {
        active   = true;
        hitCount = 0;
        velX = velY = 0;
        explosionRadius = 0;
        piercing = 0;
    }

    /** Convenience factory — set velocity from angle (radians) and speed. */
    public void setVelocity(float angle, float speed) {
        velX = (float) Math.cos(angle) * speed;
        velY = (float) Math.sin(angle) * speed;
    }

    /** Convenience factory — set velocity toward a target position. */
    public void setVelocityToward(float targetX, float targetY, float speed) {
        float dx = targetX - x;
        float dy = targetY - y;
        float len = (float) Math.sqrt(dx * dx + dy * dy);
        if (len > 0) {
            velX = (dx / len) * speed;
            velY = (dy / len) * speed;
        }
    }
}
