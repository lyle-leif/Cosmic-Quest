package com.neurolink.cosmicquest.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.AudioDevice;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.utils.Timer;
import java.util.ArrayList;
import java.util.List;

/**
 * Lightweight non-blocking procedural retro synthesizer for Hades/2D pixel UI clicks,
 * sword slashes, boss strikes, and fanfare chimes, plus looping background music
 * (menu theme and one battle theme per alien). Safe on systems without audio devices.
 */
public final class CosmicAudio {

    /** Background music tracks in assets/audio/music (seamless loops). */
    public static final String MENU_THEME = "audio/music/menu_theme.ogg";
    private static final String[] ALIEN_THEMES = {
        "audio/music/battle_void_brute.ogg",
        "audio/music/battle_star_weaver.ogg",
        "audio/music/battle_nebula_lich.ogg",
        "audio/music/battle_lunar_beast.ogg",
    };
    private static final String SQUAD_THEME = "audio/music/battle_squad_boss.ogg";

    /** Music sits under the sound effects. */
    private static final float MUSIC_VOLUME = 0.45f;
    private static final float FADE_IN_SECONDS = 1.2f;
    private static final float FADE_OUT_SECONDS = 0.8f;
    private static final float FADE_TICK = 0.05f;

    private static boolean muted = false;

    private static Music music;
    private static String musicPath;
    private static float musicVolume;
    private static final List<Music> fadingOut = new ArrayList<>();
    private static Timer.Task fader;

    private CosmicAudio() {
    }

    public static boolean isMuted() {
        return muted;
    }

    public static void toggleMute() {
        muted = !muted;
        if (music != null) {
            try {
                music.setVolume(muted ? 0f : musicVolume);
            } catch (Throwable ignored) {
            }
        }
    }

    /** Battle theme for an alien index in {@code CosmicQuestGame.ALIENS} order, or the squad theme. */
    public static String battleTheme(int alienIdx, boolean squad) {
        if (squad) {
            return SQUAD_THEME;
        }
        return ALIEN_THEMES[Math.floorMod(alienIdx, ALIEN_THEMES.length)];
    }

    /**
     * Loops the given track, cross-fading from whatever is playing. Asking for the track that is
     * already playing does nothing, so screens that share a theme keep it going without a restart.
     */
    public static void playMusic(String path) {
        if (Gdx.audio == null || Gdx.files == null) {
            return;
        }
        if (path.equals(musicPath) && music != null) {
            return;
        }
        fadeOutCurrent();
        try {
            Music next = Gdx.audio.newMusic(Gdx.files.internal(path));
            next.setLooping(true);
            next.setVolume(0f);
            next.play();
            music = next;
            musicPath = path;
            musicVolume = 0f;
        } catch (Throwable missingOrNoDevice) {
            music = null;
            musicPath = null;
        }
        startFader();
    }

    /** Fades the current track out. */
    public static void stopMusic() {
        fadeOutCurrent();
        startFader();
    }

    /** Releases every music stream immediately (game exit). */
    public static void disposeMusic() {
        if (fader != null) {
            fader.cancel();
            fader = null;
        }
        fadeOutCurrent();
        for (Music m : fadingOut) {
            disposeQuietly(m);
        }
        fadingOut.clear();
    }

    private static void fadeOutCurrent() {
        if (music != null) {
            fadingOut.add(music);
        }
        music = null;
        musicPath = null;
        musicVolume = 0f;
    }

    private static void startFader() {
        if (fader != null && fader.isScheduled()) {
            return;
        }
        fader = Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                stepFade();
            }
        }, 0f, FADE_TICK);
    }

    /** Runs on the render thread (LibGDX Timer posts its tasks there). */
    private static void stepFade() {
        boolean busy = false;
        if (music != null && musicVolume < MUSIC_VOLUME) {
            musicVolume = Math.min(MUSIC_VOLUME, musicVolume + MUSIC_VOLUME * FADE_TICK / FADE_IN_SECONDS);
            try {
                music.setVolume(muted ? 0f : musicVolume);
            } catch (Throwable ignored) {
            }
            busy = musicVolume < MUSIC_VOLUME;
        }
        for (int i = fadingOut.size() - 1; i >= 0; i--) {
            Music m = fadingOut.get(i);
            float v;
            try {
                v = m.getVolume() - MUSIC_VOLUME * FADE_TICK / FADE_OUT_SECONDS;
                if (v > 0f) {
                    m.setVolume(v);
                }
            } catch (Throwable broken) {
                v = 0f;
            }
            if (v <= 0f) {
                disposeQuietly(m);
                fadingOut.remove(i);
            } else {
                busy = true;
            }
        }
        if (!busy && fader != null) {
            fader.cancel();
            fader = null;
        }
    }

    private static void disposeQuietly(Music m) {
        try {
            m.stop();
            m.dispose();
        } catch (Throwable ignored) {
        }
    }

    public static void playClick() {
        playToneAsync(new float[]{540f, 760f}, new float[]{0.035f, 0.045f}, 0.16f);
    }

    public static void playPlayerSlash() {
        playToneAsync(new float[]{380f, 620f, 920f, 1180f}, new float[]{0.04f, 0.05f, 0.06f, 0.09f}, 0.22f);
    }

    public static void playBossHit() {
        playToneAsync(new float[]{210f, 155f, 110f}, new float[]{0.07f, 0.08f, 0.12f}, 0.25f);
    }

    public static void playBoonActivate() {
        playToneAsync(new float[]{523.25f, 659.25f, 783.99f, 1046.5f}, new float[]{0.05f, 0.05f, 0.06f, 0.10f}, 0.20f);
    }

    public static void playVictory() {
        playToneAsync(new float[]{440f, 554.37f, 659.25f, 880f}, new float[]{0.09f, 0.09f, 0.10f, 0.22f}, 0.22f);
    }

    public static void playDefeat() {
        playToneAsync(new float[]{311.13f, 277.18f, 246.94f, 196.0f}, new float[]{0.10f, 0.10f, 0.11f, 0.24f}, 0.22f);
    }

    private static void playToneAsync(float[] freqs, float[] durations, float volume) {
        if (muted || Gdx.audio == null) {
            return;
        }
        Thread t = new Thread(() -> {
            AudioDevice device = null;
            try {
                int sampleRate = 22050;
                device = Gdx.audio.newAudioDevice(sampleRate, true);
                for (int i = 0; i < freqs.length; i++) {
                    int samples = Math.max(1, (int) (sampleRate * durations[i]));
                    short[] pcm = new short[samples];
                    float freq = freqs[i];
                    for (int s = 0; s < samples; s++) {
                        float env = 1.0f - ((float) s / samples);
                        double angle = 2.0 * Math.PI * freq * s / sampleRate;
                        // Blend sine + subtle square harmonic for 16-bit/Hades chime feel
                        double wave = 0.7 * Math.sin(angle) + 0.3 * Math.signum(Math.sin(angle));
                        pcm[s] = (short) (wave * Short.MAX_VALUE * volume * env);
                    }
                    device.writeSamples(pcm, 0, pcm.length);
                }
            } catch (Throwable ignored) {
                // Ignore audio hardware limitations
            } finally {
                if (device != null) {
                    try {
                        device.dispose();
                    } catch (Throwable ignored) {
                    }
                }
            }
        }, "CosmicAudio-Synth");
        t.setDaemon(true);
        t.start();
    }
}
