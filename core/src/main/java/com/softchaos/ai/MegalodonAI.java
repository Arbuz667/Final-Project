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
        float dx = player.x - self.x;
        float dy = player.y - self.y;
        float len = (float) Math.sqrt(dx * dx + dy * dy);
        if (len > 0) {
            self.x += (dx / len) * self.speed * delta;
            self.y += (dy / len) * self.speed * delta;
        }

        if (self.phase >= 2) {
            sharkTimer += delta;
            if (sharkTimer >= SHARK_SPAWN_INTERVAL) {
                sharkTimer = 0f; // TODO: spawn shark minions
            }
        }
        if (self.phase >= 3) {
            aoeTimer += delta;
            if (aoeTimer >= AOE_INTERVAL) {
                transitionTo(State.ATTACK);
                aoeTimer = 0f;
            }
        }
        // Melee-range attack trigger (all phases)
        if (len < 1.5f && stateTimer > 1.5f) {
            transitionTo(State.ATTACK);
            stateTimer = 0f;
        }
    }

    @Override
    protected void handleAttack(Boss self, Player player, float delta) {
        // AOE burst — damages player if within 4 world units
        float dx = player.x - self.x;
        float dy = player.y - self.y;
        if ((float) Math.sqrt(dx * dx + dy * dy) <= 4f) {
            player.takeDamage(35f);
        }
        transitionTo(State.CHASE);
    }
}
