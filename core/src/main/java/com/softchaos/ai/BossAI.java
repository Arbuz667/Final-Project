package com.softchaos.ai;

import com.softchaos.entities.Boss;
import com.softchaos.entities.Enemy;
import com.softchaos.entities.Player;

/**
 * Abstract FSM-based AI for all bosses.
 * States: IDLE → CHASE → ATTACK → PHASE_CHANGE
 * Each boss subclass overrides handleAttack() and handlePhaseChange().
 */
public abstract class BossAI implements EnemyAI {

    public enum State { IDLE, CHASE, ATTACK, PHASE_CHANGE }

    protected State state = State.IDLE;
    protected float stateTimer = 0f;

    @Override
    public void update(Enemy self, Player player, float delta) {
        update((Boss) self, player, delta);
    }

    public void update(Boss self, Player player, float delta) {
        stateTimer += delta;
        switch (state) {
            case IDLE:
                handleIdle(self, player, delta);
                break;
            case CHASE:
                handleChase(self, player, delta);
                break;
            case ATTACK:
                handleAttack(self, player, delta);
                break;
            case PHASE_CHANGE:
                handlePhaseChange(self, delta);
                break;
        }
    }

    protected void handleIdle(Boss self, Player player, float delta) {
        // TODO: transition to CHASE after short delay
        if (stateTimer > 1f) { state = State.CHASE; stateTimer = 0f; }
    }

    protected void handleChase(Boss self, Player player, float delta) {
        // TODO M3: move toward player
    }

    protected abstract void handleAttack(Boss self, Player player, float delta);

    protected void handlePhaseChange(Boss self, float delta) {
        // TODO M3: play animation, then return to CHASE
        if (stateTimer > 1.5f) { state = State.CHASE; stateTimer = 0f; }
    }

    public void transitionTo(State next) {
        state = next;
        stateTimer = 0f;
    }
}
