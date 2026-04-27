package com.softchaos.config;

import com.softchaos.utils.WeaponRarity;

/** Data class — loaded from assets/data/weapons.json via libGDX Json. */
public class WeaponConfig {
    public String id;
    public String name;
    public WeaponRarity rarity;
    public float cooldown;
    public float damage;
    public float size;
    public int projectileCount;
    public float projectileSpeed;
    public int piercing;
    public float explosionRadius;
    public String weaponClass; // class name for switch/instantiation
}
