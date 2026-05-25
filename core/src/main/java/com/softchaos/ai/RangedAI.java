package com.softchaos.ai;

import com.softchaos.entities.Enemy;
import com.softchaos.entities.Player;

/** Keeps a distance of 5–8 units from the player and shoots when within 10 units. */
public class RangedAI implements EnemyAI {

    private static final float MIN_DIST  = 5f;
    private static final float MAX_DIST  = 8f;
    private static final float SHOOT_DIST = 10f;

    @Override
    public void update(Enemy self, Player player, float delta) {
        // TODO M3: ranged attack projectile spawn
        float dx = player.x - self.x;
        float dy = player.y - self.y;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);

        if (dist > MAX_DIST) {
            // Move closer
            self.x += (dx / dist) * self.speed * delta;
            self.y += (dy / dist) * self.speed * delta;
        } else if (dist < MIN_DIST) {
            // Back away
            self.x -= (dx / dist) * self.speed * delta;
            self.y -= (dy / dist) * self.speed * delta;
        }
        self.hitbox.setPosition(self.x, self.y);

        if (dist <= SHOOT_DIST) {
            // TODO M3: fire projectile toward player
        }
    }
}
