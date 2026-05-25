package com.softchaos.weapons.cringe;

import com.softchaos.entities.Enemy;
import com.softchaos.utils.Constants;

/**
 * Component stored inside Enemy.
 * Tracks the cringe percentage (0–100).
 * When it reaches 100, the enemy is instantly killed (one-shot).
 *
 * Note: the takeCringeDamage / checkOneshot logic is implemented directly
 * in Enemy.java for simplicity. This class is kept as a helper/reference.
 */
public class CringeMeter {

    private float value = 0f;
    private final Enemy owner;

    public CringeMeter(Enemy owner) {
        this.owner = owner;
    }

    public void add(float pct) {
        value += pct;
        checkOneshot();
    }

    private void checkOneshot() {
        if (value >= Constants.CRINGE_ONESHOT_THRESHOLD) {
            owner.hp = 0;
        }
    }

    public float getValue() {
        return value;
    }

    public void reset() {
        value = 0f;
    }
}
