package com.softchaos.entities;

import com.softchaos.ai.BossAI;

public class Boss extends Enemy {

    public int phase;
    public float[] phaseThreshold; // HP percentages at which phase changes occur

    public Boss() {
        phase = 1;
        phaseThreshold = new float[]{0.66f, 0.33f}; // phase 2 at 66%, phase 3 at 33%
    }

    @Override
    public void update(float delta, Player player) {
        checkPhase();
        super.update(delta, player);
    }

    public void checkPhase() {
        if (maxHp <= 0) return;
        float hpPercent = hp / maxHp;
        int newPhase = 1;
        for (int i = 0; i < phaseThreshold.length; i++) {
            if (hpPercent <= phaseThreshold[i]) {
                newPhase = i + 2;
            }
        }
        if (newPhase != phase) {
            phase = newPhase;
            onPhaseChange(phase);
        }
    }

    public void onPhaseChange(int newPhase) {
        // TODO M3: notify BossAI to transition to PHASE_CHANGE state
        if (ai instanceof BossAI) {
            ((BossAI) ai).transitionTo(BossAI.State.PHASE_CHANGE);
        }
    }
}
