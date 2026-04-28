package com.softchaos.managers;

import com.badlogic.gdx.utils.Array;
import com.softchaos.utils.LocationType;
import com.softchaos.utils.WeaponRarity;
import com.softchaos.weapons.Weapon;

/** Singleton. Persists game state across screens (location, level, HP, inventory, stats). */
public class GameStateManager {

    private static GameStateManager instance;

    public LocationType currentLocation;
    public int   playerLevel;
    public float playerHP;
    public Array<Weapon> inventory;
    public int   xp;
    public int   kills;
    public float sessionTime;

    // Selected character index (0–2)
    public int selectedCharacter;

    private GameStateManager() {
        reset();
    }

    public static GameStateManager getInstance() {
        if (instance == null) instance = new GameStateManager();
        return instance;
    }

    /** Call at the start of a new run to clear all session stats. */
    public void reset() {
        currentLocation  = LocationType.FOREST;
        playerLevel      = 1;
        playerHP         = 100f;
        inventory        = new Array<>(4);
        xp               = 0;
        kills            = 0;
        sessionTime      = 0f;
        selectedCharacter = 0;
    }
}
