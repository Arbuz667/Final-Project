package com.softchaos.ai;

import com.softchaos.entities.Enemy;
import com.softchaos.entities.Player;

/** Slow chase. On collision with player applies knockback to the player. */
public class TankAI implements EnemyAI {

    private static final float KNOCKBACK_FORCE = 8f;
    private static final float COLLISION_DIST  = 1.0f;

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

        if (dist <= COLLISION_DIST) {
            // TODO M3: apply knockback to player (player.applyKnockback)
        }
    }
}
