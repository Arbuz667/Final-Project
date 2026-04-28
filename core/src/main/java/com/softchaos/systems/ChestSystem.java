package com.softchaos.systems;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.softchaos.entities.Chest;
import com.softchaos.utils.ChestType;
import com.softchaos.utils.RandomUtils;
import com.softchaos.utils.WeaponRarity;
import com.softchaos.weapons.Weapon;

public class ChestSystem {

    private final Array<Chest> activeChests = new Array<>();
    private Array<Weapon> weaponPool = new Array<>(); // filled from weapons.json

    private final Pool<Chest> chestPool = new Pool<Chest>() {
        @Override protected Chest newObject() { return new Chest(); }
    };

    private ChestListener listener;

    public interface ChestListener {
        void onChestOpened(Chest chest);
    }

    public void setListener(ChestListener listener) {
        this.listener = listener;
    }

    public void setWeaponPool(Array<Weapon> weapons) {
        this.weaponPool = weapons;
    }

    public void spawnChest(ChestType type, float x, float y) {
        Chest c = chestPool.obtain();
        c.type         = type;
        c.weaponRarity = rollWeaponRarity(type);
        c.weapon       = getWeaponByRarity(c.weaponRarity);
        c.setPosition(x, y);
        activeChests.add(c);
    }

    public WeaponRarity rollWeaponRarity(ChestType type) {
        // Gold chests bias toward higher rarities
        if (type == ChestType.GOLD) {
            // shift weights: skip COMMON tier, reroll
            WeaponRarity r = RandomUtils.rollRarity();
            return r == WeaponRarity.COMMON ? WeaponRarity.RARE : r;
        }
        return RandomUtils.rollRarity();
    }

    public Weapon getWeaponByRarity(WeaponRarity rarity) {
        Array<Weapon> matching = new Array<>();
        for (Weapon w : weaponPool) {
            if (w.rarity == rarity) matching.add(w);
        }
        if (matching.isEmpty()) return weaponPool.random();
        return matching.random();
    }

    public void openChest(Chest chest) {
        chest.open = true;
        if (listener != null) listener.onChestOpened(chest);
    }

    public void freeChest(Chest chest) {
        activeChests.removeValue(chest, true);
        chestPool.free(chest);
    }

    public Array<Chest> getActiveChests() {
        return activeChests;
    }
}
