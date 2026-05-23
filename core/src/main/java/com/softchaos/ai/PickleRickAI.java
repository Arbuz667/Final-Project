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
        float dx = player.x - self.x;
        float dy = player.y - self.y;
        float len = (float) Math.sqrt(dx * dx + dy * dy);
        if (len > 0) {
            self.x += (dx / len) * self.speed * delta;
            self.y += (dy / len) * self.speed * delta;
        }

        if (self.phase >= 2) {
            minionTimer += delta;
            if (minionTimer >= MINION_SPAWN_INTERVAL) {
                minionTimer = 0f; // TODO: spawn minion enemies
            }
        }
        // Trigger melee attack when within range
        if (len < 1.5f && stateTimer > 1f) {
            transitionTo(State.ATTACK);
            stateTimer = 0f;
        }
    }

    @Override
    protected void handleAttack(Boss self, Player player, float delta) {
        float dx = player.x - self.x;
        float dy = player.y - self.y;
        if ((float) Math.sqrt(dx * dx + dy * dy) < 2f) {
            player.takeDamage(25f);
        }
        transitionTo(State.CHASE);
    }
}
