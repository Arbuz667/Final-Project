package com.softchaos.ai;

import com.softchaos.entities.Enemy;
import com.softchaos.entities.Player;

/** Slow chase. On collision with player applies knockback to the player. */
public class TankAI implements EnemyAI {

    private static final float KNOCKBACK_FORCE    = 8f;
    private static final float COLLISION_DIST     = 1.0f;
    private static final float KNOCKBACK_COOLDOWN = 1.2f; // seconds between knockbacks

    private float knockbackCooldownTimer = 0f;

    @Override
    public void update(Enemy self, Player player, float delta) {
        float dx = player.x - self.x;
        float dy = player.y - self.y;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);

        if (dist > 0) {
            self.x += (dx / dist) * self.speed * delta;
            self.y += (dy / dist) * self.speed * delta;
        }
        self.hitbox.setPosition(self.x, self.y);

        if (knockbackCooldownTimer > 0) knockbackCooldownTimer -= delta;

        if (dist <= COLLISION_DIST && knockbackCooldownTimer <= 0) {
            float nx = dist > 0 ? dx / dist : 1f;
            float ny = dist > 0 ? dy / dist : 0f;
            player.applyKnockback(nx * KNOCKBACK_FORCE, ny * KNOCKBACK_FORCE, 0.3f);
            knockbackCooldownTimer = KNOCKBACK_COOLDOWN;
        }
    }
}
