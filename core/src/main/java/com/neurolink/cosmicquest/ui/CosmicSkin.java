package com.neurolink.cosmicquest.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar.ProgressBarStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane.ScrollPaneStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider.SliderStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldStyle;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

/**
 * Procedurally generates the Cosmic Quest Scene2D Skin (panels, glowing borders,
 * CTA buttons, health/timer bars, text fields, and typography styles) so the UI
 * renders crisply without external texture dependencies.
 */
public class CosmicSkin implements Disposable {

    private final Skin skin;
    private final Array<Texture> ownedTextures = new Array<>();

    private final BitmapFont fontBody;
    private final BitmapFont fontHeading;
    private final BitmapFont fontTitle;
    private final BitmapFont fontSmall;

    public CosmicSkin() {
        this.skin = new Skin();

        this.fontSmall = createFont(0.88f);
        this.fontBody = createFont(1.08f);
        this.fontHeading = createFont(1.42f);
        this.fontTitle = createFont(2.65f);

        registerDrawables();
        registerLabelStyles();
        registerButtonStyles();
        registerProgressBarStyles();
        registerTextFieldStyles();
        registerScrollAndSliderStyles();
    }

    public Skin getSkin() {
        return skin;
    }

    private BitmapFont createFont(float scale) {
        BitmapFont font = new BitmapFont();
        font.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        font.getData().setScale(scale);
        return font;
    }

    private void registerDrawables() {
        // Panels
        skin.add("panel-dark", makeBorderedBox(CosmicTheme.PANEL_DARK, CosmicTheme.PANEL_BORDER, 1), Drawable.class);
        skin.add("panel-elevated", makeBorderedBox(CosmicTheme.PANEL_ELEVATED, Color.valueOf("2A3B6EFF"), 1), Drawable.class);
        skin.add("panel-glow-cyan", makeBorderedBox(CosmicTheme.withAlpha(CosmicTheme.PANEL_ELEVATED, 0.96f), CosmicTheme.CYAN_PRIMARY, 2), Drawable.class);
        skin.add("panel-danger", makeBorderedBox(Color.valueOf("240D16E6"), CosmicTheme.CRIMSON_DANGER, 1), Drawable.class);
        skin.add("panel-gold", makeBorderedBox(Color.valueOf("241C0BE6"), CosmicTheme.GOLD_STAR, 1), Drawable.class);

        // Primary Cyan CTA Button
        skin.add("btn-cyan-up", makeBorderedBox(CosmicTheme.CYAN_PRIMARY, CosmicTheme.CYAN_PRIMARY, 1), Drawable.class);
        skin.add("btn-cyan-over", makeBorderedBox(Color.valueOf("5CEEFF"), Color.WHITE, 1), Drawable.class);
        skin.add("btn-cyan-down", makeBorderedBox(CosmicTheme.CYAN_DARK, CosmicTheme.CYAN_PRIMARY, 1), Drawable.class);

        // Gold CTA Button (Victory Next Level)
        skin.add("btn-gold-up", makeBorderedBox(CosmicTheme.GOLD_STAR, CosmicTheme.GOLD_STAR, 1), Drawable.class);
        skin.add("btn-gold-over", makeBorderedBox(Color.valueOf("FCD34DFF"), Color.WHITE, 1), Drawable.class);
        skin.add("btn-gold-down", makeBorderedBox(Color.valueOf("D97706FF"), CosmicTheme.GOLD_STAR, 1), Drawable.class);

        // Danger CTA Button (Retry Mission)
        skin.add("btn-danger-up", makeBorderedBox(CosmicTheme.CRIMSON_DANGER, CosmicTheme.CRIMSON_DANGER, 1), Drawable.class);
        skin.add("btn-danger-over", makeBorderedBox(Color.valueOf("FB7185FF"), Color.WHITE, 1), Drawable.class);
        skin.add("btn-danger-down", makeBorderedBox(Color.valueOf("BE123CFF"), CosmicTheme.CRIMSON_DANGER, 1), Drawable.class);

        // Ghost / Translucent Secondary Button
        skin.add("btn-ghost-up", makeBorderedBox(CosmicTheme.PANEL_DARK, CosmicTheme.PANEL_BORDER, 1), Drawable.class);
        skin.add("btn-ghost-over", makeBorderedBox(CosmicTheme.PANEL_ELEVATED, CosmicTheme.CYAN_PRIMARY, 1), Drawable.class);
        skin.add("btn-ghost-down", makeBorderedBox(Color.valueOf("09152EFF"), CosmicTheme.CYAN_PRIMARY, 2), Drawable.class);
        skin.add("btn-ghost-disabled", makeBorderedBox(Color.valueOf("0A0E1C99"), Color.valueOf("161E3880"), 1), Drawable.class);

        // Progress Bars
        skin.add("bar-track", makeSolid(Color.valueOf("151D38FF")), Drawable.class);
        skin.add("bar-cyan", makeSolid(CosmicTheme.CYAN_PRIMARY), Drawable.class);
        skin.add("bar-magenta", makeSolid(CosmicTheme.MAGENTA_ENEMY), Drawable.class);
        skin.add("bar-gold", makeSolid(CosmicTheme.GOLD_STAR), Drawable.class);
        skin.add("bar-emerald", makeSolid(CosmicTheme.EMERALD_SUCCESS), Drawable.class);
        skin.add("bar-crimson", makeSolid(CosmicTheme.CRIMSON_DANGER), Drawable.class);

        // Cursor & Selection
        skin.add("cursor-cyan", makeSolid(CosmicTheme.CYAN_PRIMARY), Drawable.class);
        skin.add("selection-cyan", makeSolid(CosmicTheme.withAlpha(CosmicTheme.CYAN_PRIMARY, 0.30f)), Drawable.class);
        skin.add("knob-cyan", makeBorderedBox(CosmicTheme.CYAN_PRIMARY, Color.WHITE, 1), Drawable.class);
    }

    private void registerLabelStyles() {
        skin.add("default", new LabelStyle(fontBody, CosmicTheme.TEXT_PRIMARY));
        skin.add("small", new LabelStyle(fontSmall, CosmicTheme.TEXT_SECONDARY));
        skin.add("muted", new LabelStyle(fontBody, CosmicTheme.TEXT_SECONDARY));
        skin.add("heading", new LabelStyle(fontHeading, CosmicTheme.TEXT_PRIMARY));
        skin.add("title", new LabelStyle(fontTitle, CosmicTheme.TEXT_PRIMARY));
        skin.add("cyan", new LabelStyle(fontBody, CosmicTheme.CYAN_PRIMARY));
        skin.add("cyan-heading", new LabelStyle(fontHeading, CosmicTheme.CYAN_PRIMARY));
        skin.add("gold", new LabelStyle(fontBody, CosmicTheme.GOLD_STAR));
        skin.add("gold-title", new LabelStyle(fontTitle, CosmicTheme.GOLD_STAR));
        skin.add("danger", new LabelStyle(fontBody, CosmicTheme.CRIMSON_DANGER));
        skin.add("danger-title", new LabelStyle(fontTitle, CosmicTheme.CRIMSON_DANGER));
        skin.add("success", new LabelStyle(fontBody, CosmicTheme.EMERALD_SUCCESS));
    }

    private void registerButtonStyles() {
        TextButtonStyle ghost = new TextButtonStyle();
        ghost.font = fontBody;
        ghost.fontColor = CosmicTheme.TEXT_PRIMARY;
        ghost.overFontColor = CosmicTheme.CYAN_PRIMARY;
        ghost.downFontColor = CosmicTheme.TEXT_PRIMARY;
        ghost.disabledFontColor = CosmicTheme.TEXT_MUTED;
        ghost.up = skin.getDrawable("btn-ghost-up");
        ghost.over = skin.getDrawable("btn-ghost-over");
        ghost.down = skin.getDrawable("btn-ghost-down");
        ghost.disabled = skin.getDrawable("btn-ghost-disabled");
        skin.add("default", ghost);

        TextButtonStyle cyan = new TextButtonStyle();
        cyan.font = fontBody;
        cyan.fontColor = CosmicTheme.TEXT_ON_CYAN;
        cyan.overFontColor = CosmicTheme.TEXT_ON_CYAN;
        cyan.downFontColor = CosmicTheme.TEXT_PRIMARY;
        cyan.disabledFontColor = CosmicTheme.TEXT_MUTED;
        cyan.up = skin.getDrawable("btn-cyan-up");
        cyan.over = skin.getDrawable("btn-cyan-over");
        cyan.down = skin.getDrawable("btn-cyan-down");
        cyan.disabled = skin.getDrawable("btn-ghost-disabled");
        skin.add("cyan", cyan);

        TextButtonStyle gold = new TextButtonStyle();
        gold.font = fontBody;
        gold.fontColor = CosmicTheme.TEXT_ON_GOLD;
        gold.overFontColor = CosmicTheme.TEXT_ON_GOLD;
        gold.downFontColor = CosmicTheme.TEXT_PRIMARY;
        gold.up = skin.getDrawable("btn-gold-up");
        gold.over = skin.getDrawable("btn-gold-over");
        gold.down = skin.getDrawable("btn-gold-down");
        skin.add("gold", gold);

        TextButtonStyle danger = new TextButtonStyle();
        danger.font = fontBody;
        danger.fontColor = CosmicTheme.TEXT_PRIMARY;
        danger.overFontColor = CosmicTheme.TEXT_PRIMARY;
        danger.downFontColor = CosmicTheme.TEXT_PRIMARY;
        danger.up = skin.getDrawable("btn-danger-up");
        danger.over = skin.getDrawable("btn-danger-over");
        danger.down = skin.getDrawable("btn-danger-down");
        skin.add("danger", danger);

        TextButtonStyle option = new TextButtonStyle(ghost);
        option.checked = skin.getDrawable("panel-glow-cyan");
        option.checkedFontColor = CosmicTheme.CYAN_PRIMARY;
        skin.add("option", option);
    }

    private void registerProgressBarStyles() {
        skin.add("hull-cyan", makeBarStyle("bar-track", "bar-cyan"));
        skin.add("enemy-magenta", makeBarStyle("bar-track", "bar-magenta"));
        skin.add("timer-gold", makeBarStyle("bar-track", "bar-gold"));
        skin.add("loading-cyan", makeBarStyle("bar-track", "bar-cyan"));
        skin.add("default-horizontal", makeBarStyle("bar-track", "bar-cyan"));
    }

    private ProgressBarStyle makeBarStyle(String bgName, String fillName) {
        ProgressBarStyle style = new ProgressBarStyle();
        style.background = skin.getDrawable(bgName);
        style.knobBefore = skin.getDrawable(fillName);
        style.background.setMinHeight(12f);
        style.knobBefore.setMinHeight(12f);
        return style;
    }

    private void registerTextFieldStyles() {
        TextFieldStyle field = new TextFieldStyle();
        field.font = fontBody;
        field.fontColor = CosmicTheme.TEXT_PRIMARY;
        field.messageFont = fontBody;
        field.messageFontColor = CosmicTheme.TEXT_MUTED;
        field.background = skin.getDrawable("panel-dark");
        field.focusedBackground = skin.getDrawable("panel-glow-cyan");
        field.cursor = skin.getDrawable("cursor-cyan");
        field.selection = skin.getDrawable("selection-cyan");
        skin.add("default", field);
    }

    private void registerScrollAndSliderStyles() {
        ScrollPaneStyle scroll = new ScrollPaneStyle();
        scroll.vScroll = skin.getDrawable("bar-track");
        scroll.vScrollKnob = skin.getDrawable("bar-cyan");
        skin.add("default", scroll);

        SliderStyle slider = new SliderStyle();
        slider.background = skin.getDrawable("bar-track");
        slider.knobBefore = skin.getDrawable("bar-cyan");
        slider.knob = skin.getDrawable("knob-cyan");
        slider.background.setMinHeight(8f);
        slider.knobBefore.setMinHeight(8f);
        slider.knob.setMinWidth(18f);
        slider.knob.setMinHeight(18f);
        skin.add("default-horizontal", slider);
    }

    private Drawable makeBorderedBox(Color fill, Color border, int borderWidth) {
        int size = 16;
        Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        pixmap.setColor(fill);
        pixmap.fill();

        pixmap.setColor(border);
        for (int i = 0; i < borderWidth; i++) {
            pixmap.drawRectangle(i, i, size - 2 * i, size - 2 * i);
        }

        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        ownedTextures.add(texture);

        int inset = Math.max(borderWidth + 2, 4);
        NinePatch patch = new NinePatch(new TextureRegion(texture), inset, inset, inset, inset);
        return new NinePatchDrawable(patch);
    }

    private Drawable makeSolid(Color color) {
        Pixmap pixmap = new Pixmap(4, 4, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        ownedTextures.add(texture);
        return new TextureRegionDrawable(new TextureRegion(texture));
    }

    @Override
    public void dispose() {
        skin.dispose();
        fontSmall.dispose();
        fontBody.dispose();
        fontHeading.dispose();
        fontTitle.dispose();
        for (Texture texture : ownedTextures) {
            texture.dispose();
        }
        ownedTextures.clear();
    }
}
