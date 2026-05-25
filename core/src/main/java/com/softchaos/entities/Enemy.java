package com.softchaos.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
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

    /** True when the enemy last moved leftward — used to flip the sprite. */
    public boolean facingLeft = false;

    /** If true, knockback is completely ignored (used for bosses that should not be pushed). */
    public boolean knockbackImmune = false;

    // Melee attack cooldown — prevents hitting player every frame
    public float meleeCooldown = 0f;

    /** Projectiles queued by RangedAI to be collected by GameScreen each frame. */
    public final Array<Projectile> pendingShots = new Array<>();

    // From config
    public int xpDrop;
    public float chestDropChance;
    public ChestType chestType;

    public Enemy() {
        hitbox = new Rectangle(0, 0, 0.8f, 0.8f);
        cringeMeter = 0f;
    }

    public void applyKnockback(float forceX, float forceY, float duration) {
        if (knockbackImmune) return;
        knockbackVelX = forceX;
        knockbackVelY = forceY;
        knockbackTimer = duration;
    }

    public void update(float delta, Player player) {
        float prevX = x;
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
        if (x != prevX) facingLeft = x < prevX;
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
        meleeCooldown = 0f;
        pendingShots.clear();
        hp          = maxHp;
        cringeMeter = 0f;
    }
}
