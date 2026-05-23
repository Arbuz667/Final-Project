package com.softchaos.ai;

import com.badlogic.gdx.math.MathUtils;
import com.softchaos.entities.Boss;
import com.softchaos.entities.Player;

/** Slenderman AI — teleports to a random position near the player every 3 seconds. */
public class SlendermanAI extends BossAI {

    private static final float TELEPORT_INTERVAL = 3f;
    private static final float TELEPORT_RADIUS   = 3f;

    private float teleportTimer = 0f;

    @Override
    public void update(Boss self, Player player, float delta) {
        super.update(self, player, delta);
        teleportTimer += delta;
        if (teleportTimer >= TELEPORT_INTERVAL) {
            teleport(self, player);
            teleportTimer = 0f;
        }
    }

    private void teleport(Boss self, Player player) {
        float angle = MathUtils.random(0f, MathUtils.PI2);
        float radius = MathUtils.random(1f, TELEPORT_RADIUS);
        self.x = player.x + MathUtils.cos(angle) * radius;
        self.y = player.y + MathUtils.sin(angle) * radius;
        self.hitbox.setPosition(self.x, self.y);
    }

    @Override
    protected void handleChase(Boss self, Player player, float delta) {
        super.handleChase(self, player, delta);
        float dx = player.x - self.x;
        float dy = player.y - self.y;
        if ((float) Math.sqrt(dx * dx + dy * dy) < 1.5f && stateTimer > 0.8f) {
            transitionTo(State.ATTACK);
            stateTimer = 0f;
        }
    }

    @Override
    protected void handleAttack(Boss self, Player player, float delta) {
        // Close-range burst damage after teleport
        float dx = player.x - self.x;
        float dy = player.y - self.y;
        if ((float) Math.sqrt(dx * dx + dy * dy) < 2f) {
            player.takeDamage(30f);
        }
        transitionTo(State.CHASE);
    }
}
