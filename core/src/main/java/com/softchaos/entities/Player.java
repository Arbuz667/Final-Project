package com.softchaos.entities;

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

    public Player() {
        maxHp   = Constants.PLAYER_MAX_HP;
        hp      = maxHp;
        speed   = Constants.PLAYER_SPEED;
        weapons = new Array<>(Constants.PLAYER_MAX_WEAPONS);
        level   = 1;
        xp      = 0;
        hitbox  = new Rectangle(0, 0, 0.8f, 0.8f);
    }

    public void update(float delta) {
        // TODO M1: WASD movement via Gdx.input
        // TODO M1: sync hitbox position
        hitbox.setPosition(x - hitbox.width / 2f, y - hitbox.height / 2f);

        if (knockbackTimer > 0) {
            x += knockbackX * delta;
            y += knockbackY * delta;
            knockbackTimer -= delta;
        }

        // Update all equipped weapons
        for (Weapon w : weapons) {
            w.update(delta);
        }
    }

    public void takeDamage(float dmg) {
        hp = Math.max(0, hp - dmg);
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
        // TODO M1: draw player sprite at (x, y)
    }

    public void dispose() {
        // TODO: dispose textures
    }
}
