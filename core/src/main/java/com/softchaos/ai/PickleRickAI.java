package com.softchaos.ai;

import com.softchaos.entities.Boss;
import com.softchaos.entities.Player;

/**
 * Pickle Rick AI.
 * Phase 1: Chase + melee attack.
 * Phase 2 (< 50% HP): spawns minions every 5 seconds.
 */
public class PickleRickAI extends BossAI {

    private static final float MINION_SPAWN_INTERVAL = 5f;
    private float minionTimer = 0f;

    @Override
    protected void handleChase(Boss self, Player player, float delta) {
        // TODO M3: move toward player
        float dx = player.x - self.x;
        float dy = player.y - self.y;
        float len = (float) Math.sqrt(dx * dx + dy * dy);
        if (len > 0) {
            self.x += (dx / len) * self.speed * delta;
            self.y += (dy / len) * self.speed * delta;
        }
        self.hitbox.setPosition(self.x, self.y);

        if (self.phase >= 2) {
            minionTimer += delta;
            if (minionTimer >= MINION_SPAWN_INTERVAL) {
                // TODO M3: EnemySpawner.spawnMinions(self.x, self.y)
                minionTimer = 0f;
            }
        }
    }

    @Override
    protected void handleAttack(Boss self, Player player, float delta) {
        // TODO M3: melee attack
        transitionTo(State.CHASE);
    }
}
