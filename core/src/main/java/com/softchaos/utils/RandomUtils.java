package com.softchaos.utils;

import com.badlogic.gdx.math.MathUtils;

public final class RandomUtils {

    private RandomUtils() {}

    /** Returns a random float in [min, max). */
    public static float range(float min, float max) {
        return MathUtils.random(min, max);
    }

    /** Returns a random int in [min, max]. */
    public static int range(int min, int max) {
        return MathUtils.random(min, max);
    }

    /** Returns true with the given probability (0.0–1.0). */
    public static boolean chance(float probability) {
        return MathUtils.random() < probability;
    }

    /** Rolls a weighted rarity based on Constants weights. */
    public static WeaponRarity rollRarity() {
        int roll = MathUtils.random(0, 99);
        if (roll < Constants.RARITY_WEIGHT_COMMON) return WeaponRarity.COMMON;
        roll -= Constants.RARITY_WEIGHT_COMMON;
        if (roll < Constants.RARITY_WEIGHT_RARE) return WeaponRarity.RARE;
        roll -= Constants.RARITY_WEIGHT_RARE;
        if (roll < Constants.RARITY_WEIGHT_EPIC) return WeaponRarity.EPIC;
        return WeaponRarity.LEGENDARY;
    }
}
