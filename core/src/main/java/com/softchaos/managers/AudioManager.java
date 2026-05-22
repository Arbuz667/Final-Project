package com.softchaos.managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.softchaos.utils.MusicType;
import com.softchaos.utils.SoundType;

/** Singleton. Handles music switching with 1-second fade-out and SFX playback. */
public class AudioManager {

    private static AudioManager instance;

    private Music     currentMusic;
    private Music     nextMusic;
    private MusicType currentMusicType;

    private float musicVolume = 0.7f;
    private float sfxVolume   = 1.0f;

    // Fade state
    private boolean fading    = false;
    private float   fadeTimer = 0f;
    private static final float FADE_DURATION = 1.0f;

    private AudioManager() {}

    public static AudioManager getInstance() {
        if (instance == null) instance = new AudioManager();
        return instance;
    }

    /**
     * Switch background music. If the same track is already playing, does nothing.
     * Otherwise fades out the current track (1 s) then starts the new one.
     */
    public void playMusic(MusicType type) {
        if (type == currentMusicType && currentMusic != null && currentMusic.isPlaying()) return;
        currentMusicType = type;

        String path = getMusicPath(type);

        if (path == null) {
            // No music assigned — stop whatever is playing
            if (currentMusic != null) { currentMusic.stop(); currentMusic.dispose(); currentMusic = null; }
            if (nextMusic    != null) { nextMusic.dispose();  nextMusic    = null; }
            fading = false;
            return;
        }

        Music next = Gdx.audio.newMusic(Gdx.files.internal(path));
        next.setLooping(true);

        if (currentMusic == null) {
            // Nothing playing yet — start immediately
            currentMusic = next;
            currentMusic.setVolume(musicVolume);
            currentMusic.play();
        } else {
            // Discard any pending track, begin fade-out of current
            if (nextMusic != null) nextMusic.dispose();
            nextMusic = next;
            fading    = true;
            fadeTimer = 0f;
        }
    }

    /** Play a one-shot SFX. */
    public void playSound(SoundType type) {
        // TODO: load sounds via AssetLoader and play with sfxVolume
    }

    /**
     * Drive fade transition — must be called every frame from the active Screen's render().
     */
    public void update(float delta) {
        if (!fading || currentMusic == null) return;
        fadeTimer += delta;
        float t = Math.min(fadeTimer / FADE_DURATION, 1f);
        if (t >= 1f) {
            currentMusic.stop();
            currentMusic.dispose();
            currentMusic = nextMusic;
            nextMusic    = null;
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
        if (currentMusic != null && !fading) currentMusic.setVolume(musicVolume);
    }

    public void setSFXVolume(float volume) { sfxVolume = volume; }

    public float getMusicVolume() { return musicVolume; }
    public float getSFXVolume()   { return sfxVolume; }

    /** Returns the internal asset path for a given MusicType, or null if none. */
    private String getMusicPath(MusicType type) {
        switch (type) {
            case MENU:      return "audio/Radiohead - All I Need.mp3";
            case GAME_OVER: return "audio/Seryoga_Pirat_-_Where_is_my_mind_(SkySound.cc).mp3";
            case FOREST:    return "audio/Terraria Music - Underground.mp3";
            case OCEAN:     return "audio/Grass Skirt Chase (Extended Mix) - SpongeBob SquarePants.mp3";
            case SPACE:     return "audio/Radiohead - Creep.mp3";
            case WIN:       return "audio/Favored Nations - The Set Up.mp3";
            default:        return null;
        }
    }

    public void dispose() {
        if (currentMusic != null) { currentMusic.dispose(); currentMusic = null; }
        if (nextMusic    != null) { nextMusic.dispose();    nextMusic    = null; }
    }
}
