package com.softchaos.ai;

import com.softchaos.entities.Boss;
import com.softchaos.entities.Player;

/**
 * Megalodon — Ocean boss, 3 phases.
 * Phase 1: Chase + melee (35 dmg, 1.5s cooldown).
 * Phase 2 (<66% HP): + 1.4× speed + spawn 2 sharks every 6s.
 * Phase 3 (<33% HP): + AOE attack every 4s (35 dmg, 4-unit radius) + knockback.
 */
public class MegalodonAI extends BossAI {

    private static final float MELEE_RANGE          = 1.5f;
    private static final float MELEE_COOLDOWN       = 1.5f;
    private static final float SHARK_SPAWN_INTERVAL = 6f;
    private static final float AOE_INTERVAL         = 4f;
    private static final float AOE_RADIUS           = 4f;

    private float sharkTimer = 0f;
    private float aoeTimer   = 0f;

    @Override
    protected void handleChase(Boss self, Player player, float delta) {
        float dx = player.x - self.x;
        float dy = player.y - self.y;
        float len = (float) Math.sqrt(dx * dx + dy * dy);
        if (len > 0) {
            float speedMul = self.phase >= 2 ? 1.4f : 1f;
            self.x += (dx / len) * self.speed * speedMul * delta;
            self.y += (dy / len) * self.speed * speedMul * delta;
            self.hitbox.setPosition(self.x - 0.75f, self.y - 0.75f);
        }

        // Phase 2+: spawn shark minions periodically
        if (self.phase >= 2 && minionCallback != null) {
            sharkTimer += delta;
            if (sharkTimer >= SHARK_SPAWN_INTERVAL) {
                minionCallback.spawn(self.x, self.y);
                sharkTimer = 0f;
            }
        }

        // Phase 3: periodic AOE attack
        if (self.phase >= 3) {
            aoeTimer += delta;
            if (aoeTimer >= AOE_INTERVAL) {
                aoeTimer = 0f;
                transitionTo(State.ATTACK);
                return;
            }
        }

        // Melee attack when close
        if (len < MELEE_RANGE && stateTimer > MELEE_COOLDOWN) {
            transitionTo(State.ATTACK);
        }
    }

    @Override
    protected void handleAttack(Boss self, Player player, float delta) {
        float dx = player.x - self.x;
        float dy = player.y - self.y;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);
        // Phase 3 uses wide AOE; earlier phases use melee range
        float hitRange = self.phase >= 3 ? AOE_RADIUS : MELEE_RANGE + 0.5f;
        if (dist <= hitRange) {
            player.takeDamage(35f);
            if (dist > 0) {
                float force = self.phase >= 3 ? 8f : 5f;
                player.applyKnockback((dx / dist) * force, (dy / dist) * force, 0.25f);
            }
        }
        transitionTo(State.CHASE);
    }

    @Override
    protected void handlePhaseChange(Boss self, float delta) {
        // Brief pause, then resume
        if (stateTimer > 1.2f) {
            sharkTimer = 0f;
            aoeTimer   = 0f;
            transitionTo(State.CHASE);
        }
    }
}
