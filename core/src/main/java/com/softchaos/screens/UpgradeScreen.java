package com.softchaos.screens;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;
import com.softchaos.SoftChaosGame;
import com.softchaos.config.UpgradeConfig;
import com.softchaos.systems.UpgradeSystem;
import com.softchaos.weapons.Weapon;

/** Shows 3 upgrade choices. On pick: apply upgrade → return to GameScreen. */
public class UpgradeScreen implements Screen {

    private final SoftChaosGame  game;
    private final Weapon         weapon;
    private final UpgradeSystem  upgradeSystem;
    private final Array<UpgradeConfig> choices;
    private SpriteBatch batch;

    public UpgradeScreen(SoftChaosGame game, Weapon weapon, UpgradeSystem upgradeSystem) {
        this.game          = game;
        this.weapon        = weapon;
        this.upgradeSystem = upgradeSystem;
        this.choices       = upgradeSystem.rollChoices(weapon);
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
    }

    @Override
    public void render(float delta) {
        // TODO M3: draw 3 upgrade cards (description, rarity colour)
        // On click: upgradeSystem.applyUpgrade(weapon, choices.get(i)); returnToGame();
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
