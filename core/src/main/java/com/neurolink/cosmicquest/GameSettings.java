package com.neurolink.cosmicquest;

import com.badlogic.gdx.Preferences;
import java.util.List;

/** Desktop preferences shared by the menu, battle timer and audio. */
public final class GameSettings {
    public static final List<Integer> TIMER_CHOICES = List.of(15, 30, 45, 60);
    private final Preferences prefs;
    private int musicPercent;
    private volatile int effectsPercent;
    private volatile boolean muted;
    private int turnSeconds;

    public GameSettings(Preferences prefs) {
        this.prefs = prefs;
        musicPercent = percent(prefs.getInteger("musicPercent", 45));
        effectsPercent = percent(prefs.getInteger("effectsPercent", 100));
        muted = prefs.getBoolean("muted", false);
        int savedTime = prefs.getInteger("turnSeconds", 30);
        turnSeconds = TIMER_CHOICES.contains(savedTime) ? savedTime : 30;
    }

    private static int percent(int value) {
        return Math.max(0, Math.min(100, value));
    }

    public int getMusicPercent() { return musicPercent; }
    public int getEffectsPercent() { return effectsPercent; }
    public boolean isMuted() { return muted; }
    public int getTurnSeconds() { return turnSeconds; }

    // Preview while dragging, then save on release.
    public void setMusicPercent(int value) { musicPercent = percent(value); }
    public void setEffectsPercent(int value) { effectsPercent = percent(value); }

    public void toggleMute() {
        muted = !muted;
        save();
    }

    public void setTurnSeconds(int value) {
        if (!TIMER_CHOICES.contains(value)) {
            throw new IllegalArgumentException("Unsupported question timer: " + value);
        }
        turnSeconds = value;
        save();
    }

    public void save() {
        prefs.putInteger("musicPercent", musicPercent);
        prefs.putInteger("effectsPercent", effectsPercent);
        prefs.putBoolean("muted", muted);
        prefs.putInteger("turnSeconds", turnSeconds);
        prefs.flush();
    }
}
