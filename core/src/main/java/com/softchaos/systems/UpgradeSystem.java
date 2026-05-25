package com.softchaos.systems;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectIntMap;
import com.softchaos.config.UpgradeConfig;
import com.softchaos.utils.WeaponRarity;
import com.softchaos.weapons.Weapon;
import com.softchaos.weapons.WeaponSystem;

/**
 * Rolls random upgrade options and applies them to a weapon.
 * Falls back to procedurally generated upgrades if no JSON data is loaded.
 */
public class UpgradeSystem {

    private static final int CHOICES = 3;

    private Array<UpgradeConfig> allUpgrades = new Array<>();
    private final WeaponSystem weaponSystem;
    /** key = upgradeKey(u), value = times picked */
    private final ObjectIntMap<String> pickedCounts = new ObjectIntMap<>();

    public UpgradeSystem(WeaponSystem weaponSystem) {
        this.weaponSystem = weaponSystem;
    }

    /** Unique key per upgrade: weaponId + ":" + upgradeName. */
    private String upgradeKey(UpgradeConfig u) {
        String name = u.description.contains("|") ? u.description.split("\\|")[0] : u.description;
        return u.weaponId + ":" + name;
    }

    /** Max times an upgrade can be chosen (rarity-based default if not set). */
    public int getMaxLevel(UpgradeConfig u) {
        if (u.maxLevel > 0) return u.maxLevel;
        if (u.rarity == null) return 5;
        switch (u.rarity) {
            case LEGENDARY: return 1;
            case EPIC:      return 2;
            case RARE:      return 3;
            default:        return 5; // COMMON
        }
    }

    /** How many times this upgrade has already been picked. */
    public int getPickedCount(UpgradeConfig u) {
        return pickedCounts.get(upgradeKey(u), 0);
    }

    public void setUpgrades(Array<UpgradeConfig> upgrades) {
        this.allUpgrades = upgrades;
    }

    /** Roll 3 upgrade choices for the given weapon (excludes maxed-out upgrades). */
    public Array<UpgradeConfig> rollChoices(Weapon weapon) {
        Array<UpgradeConfig> pool = getUpgradesForWeapon(weapon);
        if (pool.isEmpty()) pool = buildFallbackUpgrades(weapon);
        // Remove upgrades that have already reached their max level
        for (int i = pool.size - 1; i >= 0; i--) {
            UpgradeConfig u = pool.get(i);
            if (getPickedCount(u) >= getMaxLevel(u)) pool.removeIndex(i);
        }
        // If everything is maxed, allow all (edge case)
        if (pool.isEmpty()) pool = buildFallbackUpgrades(weapon);

        // Pick CHOICES upgrades using weighted rarity:
        // Common=66%, Rare=25%, Epic=8%, Legendary=1%
        Array<UpgradeConfig> result = new Array<>();
        Array<UpgradeConfig> remaining = new Array<>(pool);
        for (int i = 0; i < CHOICES && !remaining.isEmpty(); i++) {
            UpgradeConfig picked = weightedPick(remaining);
            result.add(picked);
            remaining.removeValue(picked, true);
        }
        return result;
    }

    /**
     * Picks one upgrade from the pool using rarity weights.
     * Common=66, Rare=25, Epic=8, Legendary=1 (out of 100).
     * Falls back to uniform random if all candidates have null rarity.
     */
    private UpgradeConfig weightedPick(Array<UpgradeConfig> pool) {
        // Build cumulative weight list
        float[] weights = new float[pool.size];
        float total = 0f;
        for (int i = 0; i < pool.size; i++) {
            total += rarityWeight(pool.get(i).rarity);
            weights[i] = total;
        }
        float roll = MathUtils.random(0f, total);
        for (int i = 0; i < pool.size; i++) {
            if (roll <= weights[i]) return pool.get(i);
        }
        return pool.peek(); // fallback
    }

    /** Weight for each rarity tier (higher = more likely). */
    private float rarityWeight(WeaponRarity rarity) {
        if (rarity == null) return 66f;
        switch (rarity) {
            case LEGENDARY: return 1f;
            case EPIC:      return 8f;
            case RARE:      return 25f;
            default:        return 66f; // COMMON
        }
    }

    private Array<UpgradeConfig> getUpgradesForWeapon(Weapon weapon) {
        Array<UpgradeConfig> matching = new Array<>();
        for (UpgradeConfig u : allUpgrades) {
            if (u.weaponId.equals(weapon.id)) matching.add(u);
        }
        return matching;
    }

    /** Generate standard upgrade options when no JSON is available. */
    private Array<UpgradeConfig> buildFallbackUpgrades(Weapon weapon) {
        Array<UpgradeConfig> list = new Array<>();
        boolean isAreaWeapon = weapon.id.equals("sword")
                            || weapon.id.equals("diary")
                            || weapon.id.equals("nuclear_bazooka");
        list.add(make(weapon.id, WeaponRarity.COMMON,   "+20% Damage",      "Damage x1.2",                    1.20f, 1.00f, 0, 0));
        list.add(make(weapon.id, WeaponRarity.COMMON,   "+30% Damage",      "Damage x1.3",                    1.30f, 1.00f, 0, 0));
        list.add(make(weapon.id, WeaponRarity.RARE,     "Faster Attack",    "Cooldown -20%",                  1.00f, 0.80f, 0, 0));
        list.add(make(weapon.id, WeaponRarity.RARE,     "Double Shot",      "+1 projectile",                  1.00f, 1.00f, 1, 0));
        if (isAreaWeapon) {
            String sizeName = weapon.id.equals("sword") ? "Wider Swing" : "Bigger Explosion";
            String sizeDesc = weapon.id.equals("sword") ? "+30% swing area"  : "+30% explosion radius";
            list.add(makeSize(weapon.id, WeaponRarity.EPIC, sizeName, sizeDesc, 1.30f));
        } else {
            list.add(make(weapon.id, WeaponRarity.EPIC, "Piercing", "Pierce 1 more enemy", 1.00f, 1.00f, 0, 1));
        }
        list.add(make(weapon.id, WeaponRarity.EPIC,     "Power Surge",      "Damage x1.5, Cooldown -10%",     1.50f, 0.90f, 0, 0));
        list.add(make(weapon.id, WeaponRarity.LEGENDARY,"OVERCLOCK",        "Damage x2, Speed x2, +1 proj",   2.00f, 0.50f, 1, 0));
        return list;
    }

    private UpgradeConfig make(String weaponId, WeaponRarity rarity,
                                String name, String statLine,
                                float dmgMul, float cdMul,
                                int projAdd, int pierceAdd) {
        UpgradeConfig c    = new UpgradeConfig();
        c.weaponId         = weaponId;
        c.rarity           = rarity;
        c.description      = name + "|" + statLine;  // | used as separator in UpgradeScreen
        c.damageMultiplier = dmgMul;
        c.cooldownMultiplier = cdMul;
        c.projectileCountAdd = projAdd;
        c.piercingAdd        = pierceAdd;
        return c;
    }
    private UpgradeConfig makeSize(String weaponId, WeaponRarity rarity,
                                   String name, String statLine, float sizeMul) {
        UpgradeConfig c  = new UpgradeConfig();
        c.weaponId       = weaponId;
        c.rarity         = rarity;
        c.description    = name + "|" + statLine;
        c.sizeMultiplier = sizeMul;
        return c;
    }
    public void applyUpgrade(Weapon weapon, UpgradeConfig upgrade) {
        weaponSystem.applyUpgrade(weapon, upgrade);
        pickedCounts.getAndIncrement(upgradeKey(upgrade), 0, 1);
    }
}

