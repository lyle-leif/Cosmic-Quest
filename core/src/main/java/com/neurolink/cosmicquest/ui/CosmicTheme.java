package com.neurolink.cosmicquest.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * Shared Hades x Cosmic 2D Pixel color palette and layout constants.
 */
public final class CosmicTheme {

    public static final float VIRTUAL_WIDTH = 1280f;
    public static final float VIRTUAL_HEIGHT = 720f;

    // Hades Underworld & Cosmic Void Backgrounds
    public static final Color BG_OBSIDIAN = Color.valueOf("0A0812");
    public static final Color BG_PANEL = Color.valueOf("120E1E");
    public static final Color BG_DIALOGUE = new Color(0.06f, 0.05f, 0.11f, 0.94f);
    public static final Color BG_CARD = new Color(0.11f, 0.09f, 0.19f, 0.95f);
    public static final Color BG_CARD_HOVER = new Color(0.18f, 0.14f, 0.29f, 0.98f);
    public static final Color BG_CARD_SELECTED = new Color(0.26f, 0.18f, 0.08f, 0.98f);

    // Ornate Gold & Metallic Accents (Hades Boons & Borders)
    public static final Color GOLD_TRIM = Color.valueOf("D4A541");
    public static final Color GOLD_BRIGHT = Color.valueOf("FBBF24");
    public static final Color GOLD_DEEP = Color.valueOf("785516");

    // Character & Combat Accents
    public static final Color ZAGREUS_CRIMSON = Color.valueOf("E11D48");
    public static final Color BLOOD_RED = Color.valueOf("FB7185");
    public static final Color ASTRAL_CYAN = Color.valueOf("22E6FF");
    public static final Color VOID_VIOLET = Color.valueOf("A855F7");
    public static final Color EMERALD_HEAL = Color.valueOf("10B981");
    public static final Color WARNING_ORANGE = Color.valueOf("F97316");

    // Typography Colors
    public static final Color TEXT_PARCMENT = Color.valueOf("F8FAFC");
    public static final Color TEXT_WARM = Color.valueOf("FEF3C7");
    public static final Color TEXT_MUTED = Color.valueOf("94A3B8");
    public static final Color TEXT_DIM = Color.valueOf("64748B");

    /** Largest world the viewport grows to before it letterboxes (covers up to about 21:9 and 4:3). */
    public static final float MAX_WORLD_WIDTH = 2240f;
    public static final float MAX_WORLD_HEIGHT = 1080f;

    private CosmicTheme() {
    }

    /**
     * Viewport used by every screen: keeps the 1280x720 design at 16:9 and extends the world on
     * wider or taller windows, so the game fills any window shape instead of showing black bars.
     */
    public static Viewport newViewport() {
        return new ExtendViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, MAX_WORLD_WIDTH, MAX_WORLD_HEIGHT);
    }

    /** Draws a background so it covers the whole visible world (cropped, never stretched). */
    public static void drawCover(SpriteBatch batch, Texture tex, Viewport viewport) {
        drawCover(batch, tex, viewport.getWorldWidth() * 0.5f, viewport.getWorldHeight() * 0.5f,
                viewport.getWorldWidth(), viewport.getWorldHeight());
    }

    /** Draws a background centred on (cx, cy) so it covers a w x h area (cropped, never stretched). */
    public static void drawCover(SpriteBatch batch, Texture tex, float cx, float cy, float w, float h) {
        float scale = Math.max(w / tex.getWidth(), h / tex.getHeight());
        float dw = tex.getWidth() * scale;
        float dh = tex.getHeight() * scale;
        batch.draw(tex, cx - dw * 0.5f, cy - dh * 0.5f, dw, dh);
    }
}
