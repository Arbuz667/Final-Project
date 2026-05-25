package com.softchaos.ai;

import com.softchaos.entities.Enemy;
import com.softchaos.entities.Player;

/** Moves directly toward the player at enemy's own speed. */
public class ChaseAI implements EnemyAI {

    @Override
    public void update(Enemy self, Player player, float delta) {
        float dx = player.x - self.x;
        float dy = player.y - self.y;
        float len = (float) Math.sqrt(dx * dx + dy * dy);
        if (len > 0) {
            self.x += (dx / len) * self.speed * delta;
            self.y += (dy / len) * self.speed * delta;
        }
        self.hitbox.setPosition(self.x, self.y);
    }
}
