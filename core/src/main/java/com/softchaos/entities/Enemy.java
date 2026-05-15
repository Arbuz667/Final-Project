package com.softchaos.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.softchaos.ai.EnemyAI;
import com.softchaos.utils.ChestType;
import com.softchaos.utils.EnemyType;

public class Enemy {

    public float hp, maxHp;
    public float speed;
    public float x, y;
    public float damage;
    public EnemyAI ai;
    public Rectangle hitbox;
    public float cringeMeter; // 0–100, used by SixSeven
    public EnemyType type;

    // Knockback state
    public float knockbackVelX, knockbackVelY;
    public float knockbackTimer;

    // From config
    public int xpDrop;
    public float chestDropChance;
    public ChestType chestType;

    public Enemy() {
        hitbox = new Rectangle(0, 0, 0.8f, 0.8f);
        cringeMeter = 0f;
    }

    public void applyKnockback(float forceX, float forceY, float duration) {
        knockbackVelX = forceX;
        knockbackVelY = forceY;
        knockbackTimer = duration;
    }

    public void update(float delta, Player player) {
        if (knockbackTimer > 0) {
            x += knockbackVelX * delta;
            y += knockbackVelY * delta;
            knockbackTimer -= delta;
            if (knockbackTimer <= 0) {
                knockbackVelX = 0;
                knockbackVelY = 0;
            }
        } else if (ai != null) {
            ai.update(this, player, delta);
        }
        hitbox.setPosition(x - hitbox.width / 2f, y - hitbox.height / 2f);
    }

    public void takeDamage(float dmg) {
        hp = Math.max(0, hp - dmg);
    }

    /** Called by SixSeven weapon. Fills CringeMeter and checks for one-shot. */
    public void takeCringeDamage(float pct) {
        cringeMeter += pct;
        checkOneshot();
    }

    private void checkOneshot() {
        if (cringeMeter >= 100f) {
            hp = 0;
        }
    }

    public boolean isDead() {
        return hp <= 0;
    }

    public void render(SpriteBatch batch) {
        // TODO M2: draw enemy sprite at (x, y)
    }

    /** Reset for Pool<Enemy> reuse. */
    public void reset() {
        hp          = maxHp;
        cringeMeter = 0f;
    }
}
