package com.neurolink.cosmicquest.ui;

import com.badlogic.gdx.graphics.Color;

/**
 * Central visual design tokens for Cosmic Quest (1280x720 virtual space, colors, and spacing).
 * Colors match the dark cosmic HUD theme: void navy surfaces, plasma cyan CTAs,
 * violet/magenta alien accents, solar gold victory highlights, and crimson danger states.
 */
public final class CosmicTheme {

    public static final float VIRTUAL_WIDTH = 1280f;
    public static final float VIRTUAL_HEIGHT = 720f;

    // Backgrounds & Translucent HUD Surfaces
    public static final Color BG_VOID = Color.valueOf("080B18FF");
    public static final Color BG_DEEP_NAVY = Color.valueOf("0B1021FF");
    public static final Color PANEL_DARK = Color.valueOf("0D1224E0");
    public static final Color PANEL_ELEVATED = Color.valueOf("131A33F0");
    public static final Color PANEL_BORDER = Color.valueOf("1E294BFF");
    public static final Color PANEL_BORDER_GLOW = Color.valueOf("22E6FF8C");

    // Primary Accents & Combat Indicators
    public static final Color CYAN_PRIMARY = Color.valueOf("22E6FFFF");
    public static final Color CYAN_DARK = Color.valueOf("0891B2FF");
    public static final Color VIOLET_NEBULA = Color.valueOf("A855F7FF");
    public static final Color MAGENTA_ENEMY = Color.valueOf("D946EFFF");
    public static final Color GOLD_STAR = Color.valueOf("FBBF24FF");
    public static final Color GOLD_DIM = Color.valueOf("785312FF");
    public static final Color CRIMSON_DANGER = Color.valueOf("F43F5EFF");
    public static final Color EMERALD_SUCCESS = Color.valueOf("10B981FF");
    public static final Color AMBER_WARNING = Color.valueOf("F59E0BFF");

    // Typography Colors
    public static final Color TEXT_PRIMARY = Color.valueOf("F8FAFCFF");
    public static final Color TEXT_SECONDARY = Color.valueOf("94A3B8FF");
    public static final Color TEXT_MUTED = Color.valueOf("64748BFF");
    public static final Color TEXT_ON_CYAN = Color.valueOf("06101EFF");
    public static final Color TEXT_ON_GOLD = Color.valueOf("1A1002FF");

    // Environment Theme Accents
    public static final Color ENV_ANDROMEDA = Color.valueOf("7C3AEDFF");
    public static final Color ENV_DEEP_FIELD = Color.valueOf("1D4ED8FF");
    public static final Color ENV_EVENT_HORIZON = Color.valueOf("F97316FF");
    public static final Color ENV_LOOM_CLUSTER = Color.valueOf("14B8A6FF");
    public static final Color ENV_CRIMSON_NECROPOLIS = Color.valueOf("BE123CFF");
    public static final Color ENV_SHATTERED_MOON = Color.valueOf("38BDF8FF");
    public static final Color ENV_SUPERNOVA_BLOOM = Color.valueOf("FBBF24FF");
    public static final Color ENV_WRECKAGE_DRIFT = Color.valueOf("9F1239FF");

    // Layout Spacing Constants
    public static final float PADDING_SM = 8f;
    public static final float PADDING_MD = 16f;
    public static final float PADDING_LG = 24f;
    public static final float PADDING_XL = 36f;

    private CosmicTheme() {
        // Utility class
    }

    /** Returns a new Color copy with the specified alpha [0..1]. */
    public static Color withAlpha(Color base, float alpha) {
        return new Color(base.r, base.g, base.b, alpha);
    }
}
