package com.softchaos.systems;

import com.badlogic.gdx.utils.Array;
import com.softchaos.config.UpgradeConfig;
import com.softchaos.utils.RandomUtils;
import com.softchaos.utils.WeaponRarity;
import com.softchaos.weapons.Weapon;
import com.softchaos.weapons.WeaponSystem;

/**
 * Rolls random upgrade options and applies them to a weapon.
 * Upgrade data is loaded from assets/data/upgrades.json.
 */
public class UpgradeSystem {

    private static final int CHOICES = 3;

    private Array<UpgradeConfig> allUpgrades = new Array<>(); // populated by AssetLoader
    private final WeaponSystem weaponSystem;

    public UpgradeSystem(WeaponSystem weaponSystem) {
        this.weaponSystem = weaponSystem;
    }

    public void setUpgrades(Array<UpgradeConfig> upgrades) {
        this.allUpgrades = upgrades;
    }

    /** Roll a rarity using weighted random, then return 3 upgrade options for the given weapon. */
    public Array<UpgradeConfig> rollChoices(Weapon weapon) {
        WeaponRarity rarity = RandomUtils.rollRarity();
        return getUpgradesForWeapon(weapon, rarity);
    }

    public Array<UpgradeConfig> getUpgradesForWeapon(Weapon weapon, WeaponRarity rarity) {
        Array<UpgradeConfig> matching = new Array<>();
        for (UpgradeConfig u : allUpgrades) {
            if (u.weaponId.equals(weapon.id) && u.rarity == rarity) {
                matching.add(u);
            }
        }
        matching.shuffle();
        if (matching.size > CHOICES) matching.removeRange(CHOICES, matching.size - 1);
        return matching;
    }

    public void applyUpgrade(Weapon weapon, UpgradeConfig upgrade) {
        weaponSystem.applyUpgrade(weapon, upgrade);
    }
}
