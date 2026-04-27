package com.softchaos.utils;

public final class Constants {

    private Constants() {}

    // Window
    public static final int SCREEN_WIDTH  = 1280;
    public static final int SCREEN_HEIGHT = 720;
    public static final String TITLE = "Soft Chaos";

    // World units per pixel (libGDX camera scaling)
    public static final float PPM = 32f; // pixels per meter

    // Player
    public static final float PLAYER_MAX_HP    = 100f;
    public static final float PLAYER_SPEED     = 5f;
    public static final int   PLAYER_MAX_WEAPONS = 4;

    // XP
    public static final int XP_BASE_THRESHOLD = 100; // level * 100

    // Upgrade rarities (weights summing to 100)
    public static final int RARITY_WEIGHT_COMMON    = 60;
    public static final int RARITY_WEIGHT_RARE      = 28;
    public static final int RARITY_WEIGHT_EPIC      = 10;
    public static final int RARITY_WEIGHT_LEGENDARY = 2;

    // Wave
    public static final float RARE_EVENT_CHANCE = 0.001f;

    // CringeMeter
    public static final float CRINGE_ONESHOT_THRESHOLD = 100f;
    public static final float SIX_SEVEN_CRINGE_DAMAGE  = 12f;

    // Asset paths
    public static final String WEAPONS_JSON   = "data/weapons.json";
    public static final String ENEMIES_JSON   = "data/enemies.json";
    public static final String UPGRADES_JSON  = "data/upgrades.json";
}
