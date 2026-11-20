package com.neurolink.cosmicquest.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.AudioDevice;

/**
 * Lightweight non-blocking procedural retro synthesizer for Hades/2D pixel UI clicks,
 * sword slashes, boss strikes, and fanfare chimes. Safe on systems without audio devices.
 */
public final class CosmicAudio {

    private static boolean muted = false;

    private CosmicAudio() {
    }

    public static boolean isMuted() {
        return muted;
    }

    public static void toggleMute() {
        muted = !muted;
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
