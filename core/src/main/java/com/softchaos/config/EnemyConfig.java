package com.softchaos.config;

import com.softchaos.utils.ChestType;
import com.softchaos.utils.LocationType;

/** Data class — loaded from assets/data/enemies.json via libGDX Json. */
public class EnemyConfig {
    public String id;
    public String name;
    public LocationType location;
    public float hp;
    public float speed;
    public float damage;
    public int xpDrop;
    public float chestDropChance;
    public ChestType chestType;
    public String aiClass;   // class name for switch/instantiation
    public boolean isBoss;
}
