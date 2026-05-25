package com.softchaos.managers;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.softchaos.entities.Player;
import com.softchaos.systems.WaveManager;
import com.softchaos.systems.XPSystem;

/**
 * Renders the HUD overlay: HP bar, wave timer, weapon slots, XP bar.
 * Subscribed to Player and WaveManager state via update().
 */
public class UIManager {

    private Player player;
    private WaveManager waveManager;
    private XPSystem xpSystem;

    public UIManager(Player player, WaveManager waveManager, XPSystem xpSystem) {
        this.player      = player;
        this.waveManager = waveManager;
        this.xpSystem    = xpSystem;
    }

    public void update() {
        // TODO M4: react to Player and WaveManager events if needed
    }

    public void render(SpriteBatch batch) {
        // TODO M4: draw HUD elements using ShapeDrawer or BitmapFont
        // - HP bar    : top-left
        // - Wave timer: top-center
        // - Weapon slots (4): bottom
        // - XP bar    : bottom
    }

    public void dispose() {
        // TODO: dispose fonts/textures
    }
}
