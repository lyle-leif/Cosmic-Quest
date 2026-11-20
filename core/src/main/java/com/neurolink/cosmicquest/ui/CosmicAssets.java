package com.neurolink.cosmicquest.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane.ScrollPaneStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Window.WindowStyle;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Disposable;
import com.neurolink.cosmicquest.logic.InvalidLessonFormatException;
import com.neurolink.cosmicquest.logic.LessonData;
import com.neurolink.cosmicquest.logic.TxtParser;
import com.neurolink.cosmicquest.model.Entity;
import com.neurolink.cosmicquest.model.LunarBeast;
import com.neurolink.cosmicquest.model.NebulaLich;
import com.neurolink.cosmicquest.model.StarWeaver;
import com.neurolink.cosmicquest.model.VoidBrute;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Loads and manages all Hades bust portraits, 24-bit pixel battle sprites,
 * cosmic backgrounds, fonts, and procedural Hades-themed UI drawables.
 */
public class CosmicAssets implements Disposable {

    private final Map<String, Texture> textures = new HashMap<>();
    private final List<Texture> generatedTextures = new ArrayList<>();
    private final Skin skin = new Skin();

    public final BitmapFont fontSmall;
    public final BitmapFont fontBody;
    public final BitmapFont fontMedium;
    public final BitmapFont fontLarge;
    public final BitmapFont fontTitle;
    public final BitmapFont fontHuge;

    public final Texture whitePixel;

    public CosmicAssets() {
        fontSmall = loadCustomFont("ui/fonts/font_small.fnt", 1.0f);
        fontBody = loadCustomFont("ui/fonts/font_body.fnt", 1.0f);
        fontMedium = loadCustomFont("ui/fonts/font_medium.fnt", 1.0f);
        fontLarge = loadCustomFont("ui/fonts/font_large.fnt", 1.0f);
        fontTitle = loadCustomFont("ui/fonts/font_title.fnt", 1.0f);
        fontHuge = loadCustomFont("ui/fonts/font_huge.fnt", 1.0f);

        whitePixel = makeSolidTexture(Color.WHITE);

        preloadArtAssets();
        buildSkin();
    }

    private BitmapFont loadCustomFont(String internalFntPath, float fallbackScale) {
        try {
            FileHandle fnt = resolveFile(internalFntPath);
            if (fnt != null && fnt.exists()) {
                BitmapFont font = new BitmapFont(fnt, false);
                font.getRegion().getTexture().setFilter(TextureFilter.Linear, TextureFilter.Linear);
                font.setUseIntegerPositions(false);
                return font;
            }
        } catch (Exception ignored) {
        }
        BitmapFont fallback = new BitmapFont();
        fallback.getRegion().getTexture().setFilter(TextureFilter.Linear, TextureFilter.Linear);
        fallback.getData().setScale(fallbackScale);
        fallback.setUseIntegerPositions(false);
        return fallback;
    }

    private void preloadArtAssets() {
        // High-detail Hades bust portraits (Linear filtering for smooth graphic-novel art)
        loadTexture("portraits/portrait_cadet.png", false);
        loadTexture("portraits/portrait_cadet_gunner.png", false);
        loadTexture("portraits/portrait_cadet_spear.png", false);
        loadTexture("portraits/portrait_void_brute.png", false);
        loadTexture("portraits/portrait_star_weaver.png", false);
        loadTexture("portraits/portrait_nebula_lich.png", false);
        loadTexture("portraits/portrait_lunar_beast.png", false);

        // 24-Bit Pixel Gameplay Sprites (Nearest filtering for crisp pixel art)
        loadTexture("sprites/hero_cadet.png", true);
        loadTexture("sprites/hero_cadet_hurt.png", true);
        loadTexture("sprites/hero_cadet_attack.png", true);
        loadTexture("sprites/hero_cadet_gunner.png", true);
        loadTexture("sprites/hero_cadet_gunner_hurt.png", true);
        loadTexture("sprites/hero_cadet_gunner_attack.png", true);
        loadTexture("sprites/hero_cadet_spear.png", true);
        loadTexture("sprites/hero_cadet_spear_hurt.png", true);
        loadTexture("sprites/hero_cadet_spear_attack.png", true);
        loadTexture("sprites/enemy_void_brute.png", true);
        loadTexture("sprites/enemy_void_brute_attack.png", true);
        loadTexture("sprites/enemy_star_weaver.png", true);
        loadTexture("sprites/enemy_star_weaver_attack.png", true);
        loadTexture("sprites/enemy_nebula_lich.png", true);
        loadTexture("sprites/enemy_nebula_lich_attack.png", true);
        loadTexture("sprites/enemy_lunar_beast.png", true);
        loadTexture("sprites/enemy_lunar_beast_attack.png", true);
        loadTexture("sprites/weapon_stygian_blade.png", true);
        loadTexture("sprites/weapon_astral_staff.png", true);
        loadTexture("sprites/weapon_star_gun.png", true);
        loadTexture("sprites/weapon_astral_spear.png", true);

        // 8 Cosmic Environment Backdrops + Hades II Main Menu Splash & Logo
        loadTexture("backgrounds/env_andromeda_drift.png", false);
        loadTexture("backgrounds/env_deep_field.png", false);
        loadTexture("backgrounds/env_event_horizon.png", false);
        loadTexture("backgrounds/env_loom_cluster.png", false);
        loadTexture("backgrounds/env_crimson_necropolis.png", false);
        loadTexture("backgrounds/env_shattered_moon.png", false);
        loadTexture("backgrounds/env_supernova_bloom.png", false);
        loadTexture("backgrounds/env_wreckage_drift.png", false);
        loadTexture("backgrounds/menu_hades_splash.png", false);
        loadTexture("ui/hades_logo.png", false);
    }

    public Texture loadTexture(String internalPath, boolean pixelArt) {
        if (textures.containsKey(internalPath)) {
            return textures.get(internalPath);
        }
        FileHandle handle = resolveFile(internalPath);
        Texture tex;
        if (handle != null && handle.exists()) {
            tex = new Texture(handle);
        } else {
            tex = makeSolidTexture(CosmicTheme.VOID_VIOLET);
        }
        if (pixelArt) {
            tex.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
        } else {
            tex.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        }
        textures.put(internalPath, tex);
        return tex;
    }

    private FileHandle resolveFile(String relativePath) {
        FileHandle direct = Gdx.files.internal(relativePath);
        if (direct.exists()) {
            return direct;
        }
        FileHandle prefixed = Gdx.files.internal("assets/" + relativePath);
        if (prefixed.exists()) {
            return prefixed;
        }
        return direct;
    }

    public Texture getCadetPortrait() {
        return loadTexture("portraits/portrait_cadet.png", false);
    }

    public Texture getCadetPortrait(int heroIndex) {
        switch (heroIndex % 3) {
            case 1:
                return loadTexture("portraits/portrait_cadet_gunner.png", false);
            case 2:
                return loadTexture("portraits/portrait_cadet_spear.png", false);
            default:
                return loadTexture("portraits/portrait_cadet.png", false);
        }
    }

    public Texture getCadetPixelSprite(boolean hurt) {
        return loadTexture(hurt ? "sprites/hero_cadet_hurt.png" : "sprites/hero_cadet.png", true);
    }

    public Texture getCadetPixelSprite(int heroIndex, boolean hurt) {
        return getCadetPixelSprite(heroIndex, hurt, false);
    }

    public Texture getCadetPixelSprite(int heroIndex, boolean hurt, boolean attacking) {
        switch (heroIndex % 3) {
            case 1:
                if (attacking) {
                    return loadTexture("sprites/hero_cadet_gunner_attack.png", true);
                }
                return loadTexture(hurt ? "sprites/hero_cadet_gunner_hurt.png" : "sprites/hero_cadet_gunner.png", true);
            case 2:
                if (attacking) {
                    return loadTexture("sprites/hero_cadet_spear_attack.png", true);
                }
                return loadTexture(hurt ? "sprites/hero_cadet_spear_hurt.png" : "sprites/hero_cadet_spear.png", true);
            default:
                if (attacking) {
                    return loadTexture("sprites/hero_cadet_attack.png", true);
                }
                return loadTexture(hurt ? "sprites/hero_cadet_hurt.png" : "sprites/hero_cadet.png", true);
        }
    }

    public Texture getWeaponSprite(boolean staff) {
        return loadTexture(staff ? "sprites/weapon_astral_staff.png" : "sprites/weapon_stygian_blade.png", true);
    }

    public Texture getHeroWeaponSprite(int heroIndex, boolean staffFallback) {
        switch (heroIndex % 3) {
            case 1:
                return loadTexture("sprites/weapon_star_gun.png", true);
            case 2:
                return loadTexture("sprites/weapon_astral_spear.png", true);
            default:
                return getWeaponSprite(staffFallback);
        }
    }

    public Texture getBossPortrait(Entity entity) {
        if (entity instanceof VoidBrute) {
            return loadTexture("portraits/portrait_void_brute.png", false);
        } else if (entity instanceof StarWeaver) {
            return loadTexture("portraits/portrait_star_weaver.png", false);
        } else if (entity instanceof NebulaLich) {
            return loadTexture("portraits/portrait_nebula_lich.png", false);
        } else if (entity instanceof LunarBeast) {
            return loadTexture("portraits/portrait_lunar_beast.png", false);
        }
        return loadTexture("portraits/portrait_void_brute.png", false);
    }

    public Texture getBossPixelSprite(Entity entity) {
        return getBossPixelSprite(entity, false);
    }

    public Texture getBossPixelSprite(Entity entity, boolean attacking) {
        if (entity instanceof VoidBrute) {
            return loadTexture(attacking ? "sprites/enemy_void_brute_attack.png" : "sprites/enemy_void_brute.png", true);
        } else if (entity instanceof StarWeaver) {
            return loadTexture(attacking ? "sprites/enemy_star_weaver_attack.png" : "sprites/enemy_star_weaver.png", true);
        } else if (entity instanceof NebulaLich) {
            return loadTexture(attacking ? "sprites/enemy_nebula_lich_attack.png" : "sprites/enemy_nebula_lich.png", true);
        } else if (entity instanceof LunarBeast) {
            return loadTexture(attacking ? "sprites/enemy_lunar_beast_attack.png" : "sprites/enemy_lunar_beast.png", true);
        }
        return loadTexture(attacking ? "sprites/enemy_void_brute_attack.png" : "sprites/enemy_void_brute.png", true);
    }

    public Texture getBossBackdrop(Entity entity) {
        if (entity instanceof VoidBrute) {
            return loadTexture("backgrounds/env_event_horizon.png", false);
        } else if (entity instanceof StarWeaver) {
            return loadTexture("backgrounds/env_loom_cluster.png", false);
        } else if (entity instanceof NebulaLich) {
            return loadTexture("backgrounds/env_crimson_necropolis.png", false);
        } else if (entity instanceof LunarBeast) {
            return loadTexture("backgrounds/env_shattered_moon.png", false);
        }
        return loadTexture("backgrounds/env_deep_field.png", false);
    }

    public Color getBossAccentColor(Entity entity) {
        if (entity instanceof VoidBrute) {
            return CosmicTheme.ZAGREUS_CRIMSON;
        } else if (entity instanceof StarWeaver) {
            return CosmicTheme.VOID_VIOLET;
        } else if (entity instanceof NebulaLich) {
            return CosmicTheme.EMERALD_HEAL;
        } else if (entity instanceof LunarBeast) {
            return CosmicTheme.ASTRAL_CYAN;
        }
        return CosmicTheme.GOLD_BRIGHT;
    }

    public String getBossTitleBanner(Entity entity) {
        if (entity instanceof VoidBrute) {
            return "TITAN OF THE EVENT HORIZON  |  IDENTIFICATION TRIAL";
        } else if (entity instanceof StarWeaver) {
            return "ORACLE OF THE ASTRAL LOOM  |  ENUMERATION TRIAL";
        } else if (entity instanceof NebulaLich) {
            return "ARCH-SOVEREIGN OF NECROPOLIS  |  MULTIPLE CHOICE TRIAL";
        } else if (entity instanceof LunarBeast) {
            return "HOUND OF THE SHATTERED MOON  |  REASONING TRIAL";
        }
        return "GUARDIAN OF THE VOID";
    }

    public String getBossPromptIntro(Entity entity, int turn) {
        if (entity instanceof VoidBrute) {
            String[] lines = {
                "\"Name the sacred concept carved into this scripture, little Cadet, or be crushed by gravity!\"",
                "\"By the blood of the Titans -- speak the exact term that matches this decree!\"",
                "\"Your Stygian blade cannot pierce my obsidian plates unless you name this truth!\""
            };
            return lines[Math.abs(turn) % lines.length];
        } else if (entity instanceof StarWeaver) {
            String[] lines = {
                "\"Weave every thread of this constellation in full, mortal, lest my silk bind your boons!\"",
                "\"Name all runes belonging to this order -- leave even one unthreaded and my loom strikes!\"",
                "\"Fate demands the complete list, Star-Child. Select every matching rune!\""
            };
            return lines[Math.abs(turn) % lines.length];
        } else if (entity instanceof NebulaLich) {
            String[] lines = {
                "\"Four phylacteries glow before you, mortal. Choose the one true vessel -- or taste my Hex Bolt!\"",
                "\"Even the sharpest soul falters when my necropolis shuffles your fate. Pick wisely!\"",
                "\"Which of these four decrees holds the ancient knowledge you seek?\""
            };
            return lines[Math.abs(turn) % lines.length];
        } else {
            String[] lines = {
                "\"Howl your reasoning across the cratered waste! Which doctrine holds the sacred keywords?\"",
                "\"My lunar fangs rend the unlearned! Choose the complete explanation that proves your mastery!\"",
                "\"Speak the true essence of this law before the eclipse devours your hull!\""
            };
            return lines[Math.abs(turn) % lines.length];
        }
    }

    private void buildSkin() {
        skin.add("font-small", fontSmall);
        skin.add("font-body", fontBody);
        skin.add("font-medium", fontMedium);
        skin.add("font-large", fontLarge);
        skin.add("font-title", fontTitle);
        skin.add("font-huge", fontHuge);

        // Procedural Hades-style ornate panels
        skin.add("panel-dialogue", makeOrnateBoxDrawable(CosmicTheme.BG_DIALOGUE, CosmicTheme.GOLD_TRIM, CosmicTheme.GOLD_BRIGHT, 3), Drawable.class);
        skin.add("panel-dark", makeOrnateBoxDrawable(CosmicTheme.BG_PANEL, CosmicTheme.GOLD_DEEP, CosmicTheme.GOLD_TRIM, 2), Drawable.class);
        skin.add("panel-hud", makeOrnateBoxDrawable(new Color(0.08f, 0.06f, 0.14f, 0.92f), CosmicTheme.GOLD_TRIM, CosmicTheme.ASTRAL_CYAN, 2), Drawable.class);
        skin.add("panel-crimson", makeOrnateBoxDrawable(new Color(0.16f, 0.04f, 0.08f, 0.94f), CosmicTheme.ZAGREUS_CRIMSON, CosmicTheme.GOLD_BRIGHT, 2), Drawable.class);

        // Label Styles
        skin.add("default", new LabelStyle(fontBody, CosmicTheme.TEXT_PARCMENT));
        skin.add("small", new LabelStyle(fontSmall, CosmicTheme.TEXT_PARCMENT));
        skin.add("muted", new LabelStyle(fontSmall, CosmicTheme.TEXT_MUTED));
        skin.add("medium", new LabelStyle(fontMedium, CosmicTheme.TEXT_PARCMENT));
        skin.add("gold", new LabelStyle(fontMedium, CosmicTheme.GOLD_BRIGHT));
        skin.add("cyan", new LabelStyle(fontMedium, CosmicTheme.ASTRAL_CYAN));
        skin.add("crimson", new LabelStyle(fontMedium, CosmicTheme.BLOOD_RED));
        skin.add("title", new LabelStyle(fontTitle, CosmicTheme.GOLD_BRIGHT));
        skin.add("huge", new LabelStyle(fontHuge, CosmicTheme.GOLD_BRIGHT));

        // Primary Gold/Crimson Action Button (Hades Menu Style)
        TextButtonStyle goldBtn = new TextButtonStyle();
        goldBtn.font = fontMedium;
        goldBtn.fontColor = CosmicTheme.TEXT_WARM;
        goldBtn.overFontColor = Color.WHITE;
        goldBtn.downFontColor = CosmicTheme.GOLD_BRIGHT;
        goldBtn.up = makeOrnateBoxDrawable(new Color(0.17f, 0.07f, 0.12f, 0.96f), CosmicTheme.GOLD_TRIM, CosmicTheme.ZAGREUS_CRIMSON, 2);
        goldBtn.over = makeOrnateBoxDrawable(new Color(0.28f, 0.09f, 0.16f, 0.99f), CosmicTheme.GOLD_BRIGHT, CosmicTheme.ASTRAL_CYAN, 2);
        goldBtn.down = makeOrnateBoxDrawable(new Color(0.36f, 0.12f, 0.20f, 1.0f), CosmicTheme.GOLD_BRIGHT, CosmicTheme.GOLD_BRIGHT, 3);
        goldBtn.disabled = makeOrnateBoxDrawable(new Color(0.10f, 0.09f, 0.13f, 0.65f), CosmicTheme.TEXT_DIM, CosmicTheme.TEXT_DIM, 1);
        goldBtn.disabledFontColor = CosmicTheme.TEXT_DIM;
        skin.add("default", goldBtn);

        // Hades Boon Answer Card Button (Clickable Q&A Option inside Dialogue)
        TextButtonStyle boonBtn = new TextButtonStyle();
        boonBtn.font = fontBody;
        boonBtn.fontColor = CosmicTheme.TEXT_PARCMENT;
        boonBtn.overFontColor = CosmicTheme.GOLD_BRIGHT;
        boonBtn.downFontColor = CosmicTheme.ASTRAL_CYAN;
        boonBtn.up = makeOrnateBoxDrawable(CosmicTheme.BG_CARD, CosmicTheme.GOLD_DEEP, CosmicTheme.GOLD_TRIM, 2);
        boonBtn.over = makeOrnateBoxDrawable(CosmicTheme.BG_CARD_HOVER, CosmicTheme.GOLD_BRIGHT, CosmicTheme.ASTRAL_CYAN, 2);
        boonBtn.down = makeOrnateBoxDrawable(CosmicTheme.BG_CARD_SELECTED, CosmicTheme.ASTRAL_CYAN, CosmicTheme.GOLD_BRIGHT, 2);
        boonBtn.checked = makeOrnateBoxDrawable(CosmicTheme.BG_CARD_SELECTED, CosmicTheme.GOLD_BRIGHT, CosmicTheme.ASTRAL_CYAN, 2);
        boonBtn.checkedFontColor = CosmicTheme.GOLD_BRIGHT;
        boonBtn.disabled = makeOrnateBoxDrawable(new Color(0.07f, 0.06f, 0.10f, 0.45f), CosmicTheme.TEXT_DIM, CosmicTheme.TEXT_DIM, 1);
        boonBtn.disabledFontColor = CosmicTheme.TEXT_DIM;
        skin.add("boon", boonBtn);

        // Hades II Minimalist Title Menu Item Style (like PLAY / OPTIONS on Hades II title screen)
        TextButtonStyle hadesMenuBtn = new TextButtonStyle();
        hadesMenuBtn.font = fontLarge;
        hadesMenuBtn.fontColor = CosmicTheme.TEXT_PARCMENT;
        hadesMenuBtn.overFontColor = Color.valueOf("34F59B");
        hadesMenuBtn.downFontColor = CosmicTheme.ASTRAL_CYAN;
        hadesMenuBtn.up = makeMenuGlowBarDrawable(new Color(0f, 0f, 0f, 0f), new Color(0f, 0f, 0f, 0f));
        hadesMenuBtn.over = makeMenuGlowBarDrawable(new Color(0.05f, 0.38f, 0.24f, 0.55f), Color.valueOf("34F59B"));
        hadesMenuBtn.down = makeMenuGlowBarDrawable(new Color(0.08f, 0.52f, 0.34f, 0.75f), CosmicTheme.ASTRAL_CYAN);
        skin.add("hades-menu", hadesMenuBtn);

        // Compact Power-Up / Utility Button
        TextButtonStyle utilBtn = new TextButtonStyle();
        utilBtn.font = fontSmall;
        utilBtn.fontColor = CosmicTheme.ASTRAL_CYAN;
        utilBtn.overFontColor = CosmicTheme.GOLD_BRIGHT;
        utilBtn.up = makeOrnateBoxDrawable(new Color(0.09f, 0.12f, 0.22f, 0.94f), CosmicTheme.ASTRAL_CYAN, CosmicTheme.GOLD_TRIM, 2);
        utilBtn.over = makeOrnateBoxDrawable(new Color(0.14f, 0.20f, 0.34f, 0.98f), CosmicTheme.GOLD_BRIGHT, CosmicTheme.ASTRAL_CYAN, 2);
        utilBtn.down = makeOrnateBoxDrawable(new Color(0.18f, 0.28f, 0.44f, 1.0f), CosmicTheme.GOLD_BRIGHT, Color.WHITE, 2);
        utilBtn.disabled = makeOrnateBoxDrawable(new Color(0.08f, 0.08f, 0.12f, 0.5f), CosmicTheme.TEXT_DIM, CosmicTheme.TEXT_DIM, 1);
        utilBtn.disabledFontColor = CosmicTheme.TEXT_DIM;
        skin.add("utility", utilBtn);

        // TextField Style
        TextFieldStyle tfStyle = new TextFieldStyle();
        tfStyle.font = fontBody;
        tfStyle.fontColor = CosmicTheme.TEXT_PARCMENT;
        tfStyle.messageFont = fontBody;
        tfStyle.messageFontColor = CosmicTheme.TEXT_DIM;
        tfStyle.background = makeOrnateBoxDrawable(new Color(0.05f, 0.04f, 0.09f, 0.96f), CosmicTheme.GOLD_TRIM, CosmicTheme.ASTRAL_CYAN, 2);
        tfStyle.focusedBackground = makeOrnateBoxDrawable(new Color(0.08f, 0.06f, 0.15f, 0.99f), CosmicTheme.ASTRAL_CYAN, CosmicTheme.GOLD_BRIGHT, 2);
        tfStyle.cursor = new TextureRegionDrawable(new TextureRegion(makeSolidTexture(CosmicTheme.GOLD_BRIGHT)));
        tfStyle.selection = new TextureRegionDrawable(new TextureRegion(makeSolidTexture(new Color(0.2f, 0.5f, 0.8f, 0.45f))));
        skin.add("default", tfStyle);

        // ScrollPane Style
        ScrollPaneStyle spStyle = new ScrollPaneStyle();
        spStyle.background = makeOrnateBoxDrawable(new Color(0.05f, 0.04f, 0.09f, 0.85f), CosmicTheme.GOLD_DEEP, CosmicTheme.GOLD_TRIM, 1);
        spStyle.vScroll = new TextureRegionDrawable(new TextureRegion(makeSolidTexture(new Color(0.1f, 0.1f, 0.16f, 0.8f))));
        spStyle.vScrollKnob = new TextureRegionDrawable(new TextureRegion(makeSolidTexture(CosmicTheme.GOLD_TRIM)));
        skin.add("default", spStyle);

        // Window / Dialog Style
        WindowStyle winStyle = new WindowStyle();
        winStyle.titleFont = fontMedium;
        winStyle.titleFontColor = CosmicTheme.GOLD_BRIGHT;
        winStyle.background = makeOrnateBoxDrawable(CosmicTheme.BG_DIALOGUE, CosmicTheme.GOLD_TRIM, CosmicTheme.GOLD_BRIGHT, 3);
        winStyle.stageBackground = new TextureRegionDrawable(new TextureRegion(makeSolidTexture(new Color(0f, 0f, 0f, 0.65f))));
        skin.add("default", winStyle);
    }

    public Drawable makeMenuGlowBarDrawable(Color centerFill, Color accentDot) {
        int w = 64;
        int h = 24;
        Pixmap pm = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        for (int x = 0; x < w; x++) {
            float dist = Math.abs(x - w * 0.5f) / (w * 0.5f);
            float alpha = Math.max(0f, (1f - dist * dist)) * centerFill.a;
            pm.setColor(centerFill.r, centerFill.g, centerFill.b, alpha);
            pm.drawLine(x, 2, x, h - 3);
        }
        Texture tex = new Texture(pm);
        tex.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        pm.dispose();
        generatedTextures.add(tex);
        return new TextureRegionDrawable(new TextureRegion(tex));
    }

    public Drawable makeOrnateBoxDrawable(Color fill, Color outerBorder, Color cornerGem, int thickness) {
        int size = 24;
        Pixmap pm = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        pm.setColor(fill);
        pm.fill();

        // Outer metallic gold border
        pm.setColor(outerBorder);
        for (int t = 0; t < thickness; t++) {
            pm.drawRectangle(t, t, size - 1 - t * 2, size - 1 - t * 2);
        }

        // Inner subtle shadow trim
        pm.setColor(new Color(outerBorder.r * 0.55f, outerBorder.g * 0.55f, outerBorder.b * 0.55f, 0.65f));
        pm.drawRectangle(thickness + 1, thickness + 1, size - 1 - (thickness + 1) * 2, size - 1 - (thickness + 1) * 2);

        // Hades-style corner diamond rivets
        pm.setColor(cornerGem);
        int c = thickness + 2;
        pm.fillRectangle(1, 1, c, c);
        pm.fillRectangle(size - 1 - c, 1, c, c);
        pm.fillRectangle(1, size - 1 - c, c, c);
        pm.fillRectangle(size - 1 - c, size - 1 - c, c, c);

        Texture tex = new Texture(pm);
        pm.dispose();
        generatedTextures.add(tex);
        NinePatch patch = new NinePatch(tex, 6, 6, 6, 6);
        return new NinePatchDrawable(patch);
    }

    public Texture makeSolidTexture(Color color) {
        Pixmap pm = new Pixmap(4, 4, Pixmap.Format.RGBA8888);
        pm.setColor(color);
        pm.fill();
        Texture tex = new Texture(pm);
        pm.dispose();
        generatedTextures.add(tex);
        return tex;
    }

    public Skin getSkin() {
        return skin;
    }

    /**
     * Resolves and parses a lesson file, checking both workingDir=assets/ and root dir.
     */
    public static LessonData loadLesson(String requestedPath) throws IOException, InvalidLessonFormatException {
        TxtParser parser = new TxtParser();
        String[] candidates = {
            requestedPath,
            "lessons/" + requestedPath,
            "assets/lessons/" + requestedPath,
            "assets/" + requestedPath,
            "lessons/OOP_Fundamentals.txt",
            "assets/lessons/OOP_Fundamentals.txt"
        };
        for (String candidate : candidates) {
            if (candidate == null || candidate.isBlank()) {
                continue;
            }
            Path p = Path.of(candidate);
            if (Files.exists(p) && Files.isRegularFile(p)) {
                return parser.parseFile(p);
            }
        }
        throw new IOException("Could not find lesson file: " + requestedPath);
    }

    @Override
    public void dispose() {
        for (Texture tex : textures.values()) {
            tex.dispose();
        }
        for (Texture tex : generatedTextures) {
            tex.dispose();
        }
        fontSmall.dispose();
        fontBody.dispose();
        fontMedium.dispose();
        fontLarge.dispose();
        fontTitle.dispose();
        fontHuge.dispose();
        skin.dispose();
    }
}
