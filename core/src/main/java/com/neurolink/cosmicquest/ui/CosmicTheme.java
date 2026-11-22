package com.neurolink.cosmicquest.ui;

import com.badlogic.gdx.graphics.Color;

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

    private CosmicTheme() {
    }
}
