package com.softchaos.screens;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.softchaos.SoftChaosGame;
import com.softchaos.entities.Chest;
import com.softchaos.entities.Player;

/**
 * Shows the weapon found in a chest.
 * Buttons: Take (add to free slot) / Replace (swap with existing) / Skip.
 * If all 4 slots are filled, shows the inventory for the player to choose which slot to replace.
 */
public class ChestScreen implements Screen {

    private final SoftChaosGame game;
    private final Chest  chest;
    private final Player player;
    private SpriteBatch batch;

    public ChestScreen(SoftChaosGame game, Chest chest, Player player) {
        this.game   = game;
        this.chest  = chest;
        this.player = player;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
    }

    @Override
    public void render(float delta) {
        // TODO M3: show weapon card (name, rarity, stats)
        // If player.canAddWeapon():  show Take / Skip
        // Else:                      show all 4 slots + Skip → let player pick which to replace
    }

    private void takeWeapon() {
        if (player.canAddWeapon()) {
            player.weapons.add(chest.weapon);
        }
        returnToGame();
    }

    private void replaceWeapon(int slotIndex) {
        player.weapons.set(slotIndex, chest.weapon);
        returnToGame();
    }

    private void skip() {
        returnToGame();
    }

    private void returnToGame() {
        game.setScreen(new GameScreen(game));
    }

    @Override public void resize(int width, int height) {}
    @Override public void pause() {}
    @Override public void resume() {}

    @Override
    public void hide() {
        dispose();
    }

    @Override
    public void dispose() {
        if (batch != null) { batch.dispose(); batch = null; }
    }
}
