package com.softchaos.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.softchaos.utils.Constants;
import com.softchaos.weapons.Weapon;

public class Player {

    public float hp, maxHp;
    public float speed;
    public float x, y;
    public Array<Weapon> weapons;
    public int level, xp;
    public Rectangle hitbox;

    // Knockback state
    public float knockbackX, knockbackY;
    private float knockbackTimer;

    // Regen: 1 HP every 2 seconds
    private static final float REGEN_INTERVAL = 2.0f;
    private static final float REGEN_AMOUNT   = 1.0f;
    private float regenTimer = 0f;

    // Invincibility frames after taking a hit
    private static final float IFRAME_DURATION = 1.0f;
    private float iframeTimer = 0f;

    /** True while the player is invincible (flashing after a hit). */
    public boolean isInvincible() { return iframeTimer > 0f; }

    public Player() {
        maxHp   = Constants.PLAYER_MAX_HP;
        hp      = maxHp;
        speed   = Constants.PLAYER_SPEED;
        weapons = new Array<>(Constants.PLAYER_MAX_WEAPONS);
        level   = 1;
        xp      = 0;
        hitbox  = new Rectangle(0, 0, 0.8f, 0.8f);
        // Start at screen center (world units)
        x = Constants.SCREEN_WIDTH  / Constants.PPM / 2f;
        y = Constants.SCREEN_HEIGHT / Constants.PPM / 2f;
    }

    public void update(float delta) {
        if (knockbackTimer > 0) {
            x += knockbackX * delta;
            y += knockbackY * delta;
            knockbackTimer -= delta;
        } else {
            // WASD movement
            float moveX = 0, moveY = 0;
            if (Gdx.input.isKeyPressed(Input.Keys.W)) moveY += 1;
            if (Gdx.input.isKeyPressed(Input.Keys.S)) moveY -= 1;
            if (Gdx.input.isKeyPressed(Input.Keys.A)) moveX -= 1;
            if (Gdx.input.isKeyPressed(Input.Keys.D)) moveX += 1;
            // Normalize diagonal movement so speed is consistent
            if (moveX != 0 && moveY != 0) {
                moveX *= 0.7071f;
                moveY *= 0.7071f;
            }
            x += moveX * speed * delta;
            y += moveY * speed * delta;
        }

        // Clamp to actual screen bounds (fullscreen resolution may differ from Constants)
        float half   = hitbox.width / 2f;
        float worldW = Gdx.graphics.getWidth()  / Constants.PPM;
        float worldH = Gdx.graphics.getHeight() / Constants.PPM;
        x = Math.max(half, Math.min(worldW - half, x));
        y = Math.max(half, Math.min(worldH - half, y));

        hitbox.setPosition(x - hitbox.width / 2f, y - hitbox.height / 2f);

        // Invincibility frames countdown
        if (iframeTimer > 0f) iframeTimer = Math.max(0f, iframeTimer - delta);

        // HP regeneration
        regenTimer += delta;
        if (regenTimer >= REGEN_INTERVAL) {
            regenTimer = 0f;
            if (hp < maxHp) hp = Math.min(maxHp, hp + REGEN_AMOUNT);
        }

        // Update all equipped weapons
        for (Weapon w : weapons) {
            w.update(delta);
        }
    }

    public void takeDamage(float dmg) {
        if (iframeTimer > 0f) return;   // invincible — ignore hit
        hp = Math.max(0, hp - dmg);
        iframeTimer = IFRAME_DURATION;   // start i-frames
        regenTimer  = 0f;               // reset regen cooldown on hit
    }

    public void applyKnockback(float vx, float vy, float duration) {
        knockbackX = vx;
        knockbackY = vy;
        knockbackTimer = duration;
    }

    public void levelUp() {
        level++;
        // TODO M3: trigger UpgradeScreen via GameScreen
    }

    public boolean isDead() {
        return hp <= 0;
    }

    public boolean canAddWeapon() {
        return weapons.size < Constants.PLAYER_MAX_WEAPONS;
    }

    public void render(SpriteBatch batch) {
        // TODO M4: draw player sprite at (x, y)
    }

    public void dispose() {
        // TODO: dispose textures
    }
}
