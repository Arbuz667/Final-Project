package com.softchaos.ai;

import com.softchaos.entities.Boss;
import com.softchaos.entities.Player;

/**
 * Pickle Rick — Space boss, 3 phases.
 * Phase 1: Fast chase + melee (25 dmg, 1s cooldown).
 * Phase 2 (<66% HP): + 1.5× speed + spawn 2 minions every 5s.
 * Phase 3 (<33% HP): + minions every 3s + 40 dmg per hit.
 */
public class PickleRickAI extends BossAI {

    private static final float MELEE_RANGE      = 1.5f;
    private static final float MELEE_COOLDOWN   = 1f;
    private static final float SPAWN_INTERVAL_P2 = 5f;
    private static final float SPAWN_INTERVAL_P3 = 3f;

    private float minionTimer = 0f;

    @Override
    protected void handleChase(Boss self, Player player, float delta) {
        float dx = player.x - self.x;
        float dy = player.y - self.y;
        float len = (float) Math.sqrt(dx * dx + dy * dy);
        if (len > 0) {
            float speedMul = self.phase >= 2 ? 1.5f : 1f;
            self.x += (dx / len) * self.speed * speedMul * delta;
            self.y += (dy / len) * self.speed * speedMul * delta;
            self.hitbox.setPosition(self.x - 0.75f, self.y - 0.75f);
        }

        // Phase 2+: spawn minions periodically
        if (self.phase >= 2 && minionCallback != null) {
            minionTimer += delta;
            float interval = self.phase >= 3 ? SPAWN_INTERVAL_P3 : SPAWN_INTERVAL_P2;
            if (minionTimer >= interval) {
                minionCallback.spawn(self.x, self.y);
                minionTimer = 0f;
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
        if (dist < MELEE_RANGE + 0.5f) {
            float dmg = self.phase >= 3 ? 35f : 25f;
            player.takeDamage(dmg);
            if (dist > 0) {
                player.applyKnockback((dx / dist) * 5f, (dy / dist) * 5f, 0.2f);
            }
        }
        transitionTo(State.CHASE);
    }

    @Override
    protected void handlePhaseChange(Boss self, float delta) {
        if (stateTimer > 1f) {
            minionTimer = 0f;
            transitionTo(State.CHASE);
        }
    }
}
