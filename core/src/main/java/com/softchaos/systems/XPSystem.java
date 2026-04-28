package com.softchaos.systems;

import com.softchaos.utils.Constants;

/** Tracks player XP and triggers level-ups. */
public class XPSystem {

    private int xp    = 0;
    private int level = 1;

    private XPListener listener;

    public interface XPListener {
        void onLevelUp(int newLevel);
    }

    public void setListener(XPListener listener) {
        this.listener = listener;
    }

    public void addXP(int amount) {
        xp += amount;
        checkLevelUp();
    }

    private void checkLevelUp() {
        while (xp >= getThreshold(level)) {
            xp -= getThreshold(level);
            level++;
            if (listener != null) listener.onLevelUp(level);
        }
    }

    /** XP required to reach the next level. */
    public int getThreshold(int level) {
        return Constants.XP_BASE_THRESHOLD * level;
    }

    public int getXP()    { return xp; }
    public int getLevel() { return level; }

    /** Fraction of progress toward next level (0.0–1.0) for XP bar. */
    public float getProgress() {
        return (float) xp / getThreshold(level);
    }

    public void reset() {
        xp    = 0;
        level = 1;
    }
}
