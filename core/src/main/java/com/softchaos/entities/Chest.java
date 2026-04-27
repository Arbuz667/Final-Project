package com.softchaos.entities;

import com.badlogic.gdx.math.Rectangle;
import com.softchaos.utils.ChestType;
import com.softchaos.utils.WeaponRarity;
import com.softchaos.weapons.Weapon;

public class Chest {

    public float x, y;
    public ChestType type;
    public WeaponRarity weaponRarity;
    public Weapon weapon; // the weapon inside (set when chest spawns)
    public boolean open;
    public Rectangle hitbox;

    public Chest() {
        hitbox = new Rectangle(0, 0, 0.8f, 0.8f);
        open = false;
    }

    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
        hitbox.setPosition(x - hitbox.width / 2f, y - hitbox.height / 2f);
    }

    public void reset() {
        open   = false;
        weapon = null;
    }
}
