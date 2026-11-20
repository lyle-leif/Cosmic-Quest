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
        skin.add("default", fontBody, BitmapFont.class);
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
