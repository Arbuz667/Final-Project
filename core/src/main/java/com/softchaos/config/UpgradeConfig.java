package com.softchaos.config;

import com.softchaos.utils.WeaponRarity;

/** Data class — loaded from assets/data/upgrades.json via libGDX Json. */
public class UpgradeConfig {
    public String weaponId;
    public WeaponRarity rarity;
    public String description;
    public float damageMultiplier    = 1f;
    public float cooldownMultiplier  = 1f;
    public float sizeMultiplier      = 1f;
    public int   projectileCountAdd  = 0;
    public int   piercingAdd         = 0;
    public String specialEffect;
    /** Max times this upgrade can be picked. 0 = not set (use rarity default). */
    public int maxLevel = 0;
}
