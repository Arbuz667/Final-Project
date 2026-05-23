package com.softchaos.ai;

import com.badlogic.gdx.math.MathUtils;
import com.softchaos.entities.Boss;
import com.softchaos.entities.Player;

/**
 * Slenderman — Forest boss, 3 phases.
 * Phase 1: Slow walk + teleport every 3s + melee 30 dmg.
 * Phase 2 (<66% HP): Teleport every 2s + knockback to player on each teleport.
 * Phase 3 (<33% HP): Teleport every 1s + heavy knockback + 50 dmg on contact.
 */
public class SlendermanAI extends BossAI {

    private static final float TELEPORT_RADIUS    = 3f;
    private static final float TELEPORT_INTERVAL_P1 = 3f;
    private static final float TELEPORT_INTERVAL_P2 = 2f;
    private static final float TELEPORT_INTERVAL_P3 = 1f;
    private static final float MELEE_RANGE          = 1.5f;
    private static final float MELEE_COOLDOWN       = 0.8f;

    private float teleportTimer = 0f;

    @Override
    public void update(Boss self, Player player, float delta) {
        super.update(self, player, delta);
        teleportTimer += delta;
        float interval = self.phase >= 3 ? TELEPORT_INTERVAL_P3
                       : self.phase >= 2 ? TELEPORT_INTERVAL_P2
                       : TELEPORT_INTERVAL_P1;
        if (teleportTimer >= interval) {
            teleport(self, player);
            teleportTimer = 0f;
        }
    }

    private void teleport(Boss self, Player player) {
        float angle  = MathUtils.random(0f, MathUtils.PI2);
        float radius = MathUtils.random(1f, TELEPORT_RADIUS);
        self.x = player.x + MathUtils.cos(angle) * radius;
        self.y = player.y + MathUtils.sin(angle) * radius;
        self.hitbox.setPosition(self.x - 0.75f, self.y - 0.75f);

        // Phase 2+: knockback player away from teleport destination
        if (self.phase >= 2) {
            float dx   = player.x - self.x;
            float dy   = player.y - self.y;
            float dist = (float) Math.sqrt(dx * dx + dy * dy);
            if (dist < TELEPORT_RADIUS && dist > 0) {
                float force = self.phase >= 3 ? 10f : 6f;
                player.applyKnockback(-(dx / dist) * force, -(dy / dist) * force, 0.3f);
            }
        }
    }

    @Override
    protected void handleChase(Boss self, Player player, float delta) {
        // Slowly walks toward the player between teleports
        float dx  = player.x - self.x;
        float dy  = player.y - self.y;
        float len = (float) Math.sqrt(dx * dx + dy * dy);
        if (len > 0) {
            self.x += (dx / len) * self.speed * delta;
            self.y += (dy / len) * self.speed * delta;
            self.hitbox.setPosition(self.x - 0.75f, self.y - 0.75f);
        }
        if (len < MELEE_RANGE && stateTimer > MELEE_COOLDOWN) {
            transitionTo(State.ATTACK);
        }
    }

    @Override
    protected void handleAttack(Boss self, Player player, float delta) {
        float dx   = player.x - self.x;
        float dy   = player.y - self.y;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);
        if (dist < MELEE_RANGE + 0.5f) {
            float dmg = self.phase >= 3 ? 50f : 30f;
            player.takeDamage(dmg);
            if (dist > 0) {
                float force = self.phase >= 3 ? 10f : 8f;
                player.applyKnockback((dx / dist) * force, (dy / dist) * force, 0.35f);
            }
        }
        transitionTo(State.CHASE);
    }

    @Override
    protected void handlePhaseChange(Boss self, float delta) {
        if (stateTimer > 1f) {
            teleportTimer = 0f;
            transitionTo(State.CHASE);
        }
    }
}
