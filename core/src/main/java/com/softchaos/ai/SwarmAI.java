package com.softchaos.ai;

import com.badlogic.gdx.math.MathUtils;
import com.softchaos.entities.Enemy;
import com.softchaos.entities.Player;

/** Chase + random slight deviation + separation from nearby enemies (used by Rabbids). */
public class SwarmAI implements EnemyAI {

    private static final float DEVIATION = 0.8f;      // max random offset per tick
    private static final float SEPARATION_DIST = 1.2f; // units

    @Override
    public void update(Enemy self, Player player, float delta) {
        // TODO M3: full separation logic with neighbour list
        float dx = player.x - self.x + MathUtils.random(-DEVIATION, DEVIATION);
        float dy = player.y - self.y + MathUtils.random(-DEVIATION, DEVIATION);
        float len = (float) Math.sqrt(dx * dx + dy * dy);
        if (len > 0) {
            self.x += (dx / len) * self.speed * delta;
            self.y += (dy / len) * self.speed * delta;
        }
        self.hitbox.setPosition(self.x, self.y);
    }
}
