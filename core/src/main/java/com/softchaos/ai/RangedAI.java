package com.softchaos.ai;

import com.softchaos.entities.Enemy;
import com.softchaos.entities.Player;
import com.softchaos.entities.Projectile;
import com.softchaos.utils.ProjectileType;

/** Keeps distance from player and fires projectiles every 2.5 seconds. */
public class RangedAI implements EnemyAI {

    private static final float MIN_DIST    = 5f;
    private static final float MAX_DIST    = 8f;
    private static final float SHOOT_DIST  = 11f;
    private static final float FIRE_RATE   = 2.5f;
    private static final float BULLET_SPEED = 9f;

    private float shootTimer = FIRE_RATE; // start ready to fire

    @Override
    public void update(Enemy self, Player player, float delta) {
        float dx   = player.x - self.x;
        float dy   = player.y - self.y;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);

        if (dist > 0.01f) {
            if (dist > MAX_DIST) {
                // Move closer
                self.x += (dx / dist) * self.speed * delta;
                self.y += (dy / dist) * self.speed * delta;
            } else if (dist < MIN_DIST) {
                // Back away
                self.x -= (dx / dist) * self.speed * delta;
                self.y -= (dy / dist) * self.speed * delta;
            }
        }
        self.hitbox.setPosition(self.x - self.hitbox.width / 2f, self.y - self.hitbox.height / 2f);

        // Shoot when close enough
        if (dist <= SHOOT_DIST) {
            shootTimer += delta;
            if (shootTimer >= FIRE_RATE) {
                shootTimer = 0f;
                float angle = (float) Math.atan2(dy, dx);
                Projectile p = new Projectile();
                p.projectileType = ProjectileType.ENEMY_BULLET;
                p.x       = self.x;
                p.y       = self.y;
                p.damage  = self.damage;
                p.size    = 0.28f;
                p.piercing = 0;
                p.lifetime = 5f;
                p.setVelocity(angle, BULLET_SPEED);
                self.pendingShots.add(p);
            }
        }
    }
}
