package com.softchaos.managers;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.softchaos.utils.Constants;

/**
 * Wrapper around libGDX AssetManager.
 * Singleton — call getInstance().load() once during SoftChaosGame.create().
 */
public class AssetLoader {

    private static AssetLoader instance;
    private final AssetManager manager = new AssetManager();

    private AssetLoader() {}

    public static AssetLoader getInstance() {
        if (instance == null) instance = new AssetLoader();
        return instance;
    }

    /** Queue all assets for loading. Call manager.finishLoading() or use a LoadingScreen. */
    public void load() {
        // TODO M1: queue TextureAtlases, Music, Sounds, JSON configs
        // Example:
        // manager.load("atlas/game.atlas", TextureAtlas.class);
        // manager.load("music/menu.mp3",   Music.class);
        // manager.load(Constants.WEAPONS_JSON, String.class); // read as text then parse with libGDX Json

        manager.finishLoading(); // blocking load for now; replace with LoadingScreen in M4
    }

    /** Retrieve a loaded asset by file path. */
    public <T> T get(String path, Class<T> type) {
        return manager.get(path, type);
    }

    public boolean isLoaded(String path) {
        return manager.isLoaded(path);
    }

    public void dispose() {
        manager.dispose();
    }
}
