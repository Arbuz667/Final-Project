package com.softchaos.ai;

import com.softchaos.entities.Boss;
import com.softchaos.entities.Player;

/**
 * Megalodon AI — 3 phases.
 * Phase 1: Chase.
 * Phase 2: Chase + spawn sharks periodically.
 * Phase 3: Chase + AOE attack every 4 seconds.
 */
public class MegalodonAI extends BossAI {

    private static final float SHARK_SPAWN_INTERVAL = 6f;
    private static final float AOE_INTERVAL         = 4f;

    private float sharkTimer = 0f;
    private float aoeTimer   = 0f;

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
            sharkTimer += delta;
            if (sharkTimer >= SHARK_SPAWN_INTERVAL) {
                // TODO M4: EnemySpawner.spawnSharks(self.x, self.y)
                sharkTimer = 0f;
            }
        }
        if (self.phase >= 3) {
            aoeTimer += delta;
            if (aoeTimer >= AOE_INTERVAL) {
                transitionTo(State.ATTACK);
                aoeTimer = 0f;
            }
        }
    }

    @Override
    protected void handleAttack(Boss self, Player player, float delta) {
        // TODO M4: AOE explosion around Megalodon
        transitionTo(State.CHASE);
    }
}
