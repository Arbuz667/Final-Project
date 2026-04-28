package com.softchaos.managers;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.softchaos.utils.MusicType;
import com.softchaos.utils.SoundType;

/** Singleton. Handles music switching with fade and SFX playback. */
public class AudioManager {

    private static AudioManager instance;

    private Music currentMusic;
    private float musicVolume = 0.7f;
    private float sfxVolume   = 1.0f;
    private MusicType currentMusicType;

    // Fade state
    private boolean fading      = false;
    private float   fadeTimer   = 0f;
    private static final float FADE_DURATION = 1.0f;
    private Music   nextMusic;

    private AudioManager() {}

    public static AudioManager getInstance() {
        if (instance == null) instance = new AudioManager();
        return instance;
    }

    /** Switch background music with a cross-fade. */
    public void playMusic(MusicType type) {
        if (type == currentMusicType) return;
        currentMusicType = type;
        // TODO M4: load via AssetLoader, start fade-out, then swap
        // Music next = AssetLoader.getInstance().get(getMusicPath(type), Music.class);
        // beginFade(next);
    }

    /** Play a one-shot SFX. */
    public void playSound(SoundType type) {
        // TODO M4: load via AssetLoader, play
        // Sound s = AssetLoader.getInstance().get(getSoundPath(type), Sound.class);
        // s.play(sfxVolume);
    }

    /** Update fade transition — call every frame from the active Screen. */
    public void update(float delta) {
        if (!fading || currentMusic == null) return;
        fadeTimer += delta;
        float t = fadeTimer / FADE_DURATION;
        if (t >= 1f) {
            currentMusic.stop();
            currentMusic = nextMusic;
            if (currentMusic != null) {
                currentMusic.setVolume(musicVolume);
                currentMusic.setLooping(true);
                currentMusic.play();
            }
            fading    = false;
            fadeTimer = 0f;
        } else {
            currentMusic.setVolume(musicVolume * (1f - t));
        }
    }

    public void setMusicVolume(float volume) {
        musicVolume = volume;
        if (currentMusic != null) currentMusic.setVolume(musicVolume);
    }

    public void setSFXVolume(float volume) {
        sfxVolume = volume;
    }

    public float getMusicVolume() { return musicVolume; }
    public float getSFXVolume()   { return sfxVolume; }

    public void dispose() {
        if (currentMusic != null) currentMusic.dispose();
    }
}
