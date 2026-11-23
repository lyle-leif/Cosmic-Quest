package com.neurolink.cosmicquest.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.neurolink.cosmicquest.CosmicQuestGame;
import com.neurolink.cosmicquest.logic.Term;
import com.neurolink.cosmicquest.ui.CosmicAssets;
import com.neurolink.cosmicquest.ui.CosmicAudio;
import com.neurolink.cosmicquest.ui.CosmicTheme;
import java.util.List;
import java.util.Map;

/**
 * Pantheon Codex & Art Gallery Screen.
 * Showcases all 5 characters' Detailed Hades Portraits alongside their 1-to-1
 * 24-Bit Pixel Battle Sprites, combat mechanics, and loaded lesson knowledge.
 */
public class CodexScreen extends ScreenAdapter {

    private final CosmicQuestGame game;
    private final CosmicAssets assets;
    private final Stage stage;
    private int selectedEntry = 0;
    private float animTime = 0f;
    private Table leftVisualCard;

    /** Share of the screen width used by the portrait card (520 px at the 1280 px design width). */
    private static final float LEFT_CARD_SHARE = 520f / 1280f;
    /** Size of the left card at the 1280x720 design size; the art below is laid out for it. */
    private static final float DESIGN_CARD_W = 520f;
    private static final float DESIGN_CARD_H = 550f;

    private Label entryTitleLabel;
    private Label entrySubLabel;
    private Label entryLoreLabel;
    private Label entryMechanicLabel;
    private final TextButton[] tabButtons = new TextButton[7];

    private static final String[] NAMES = {
        "CADET VANGUARD (STYGIAN STAR-BLADE)",
        "CADET STRIKER (ADAMANT STAR-GUN)",
        "CADET LANCER (ETERNAL ASTRAL SPEAR)",
        "VOID BRUTE (EVENT HORIZON TITAN)",
        "STAR WEAVER (ASTRAL LOOM ORACLE)",
        "NEBULA LICH (CRIMSON NECROPOLIS)",
        "LUNAR BEAST (SHATTERED MOON HOUND)"
    };

    private static final String[] SUBTITLES = {
        "Squad Hero I (Sword)  //  Solo & Squad Lead Challenger",
        "Squad Hero II (Gun)  //  Long-Range Plasma Marksman",
        "Squad Hero III (Spear)  //  Astral Valkyrie Lancer",
        "Guardian Boss I  //  Identification Trial",
        "Guardian Boss II  //  Reasoning Trial",
        "Guardian Boss III  //  Multiple Choice Trial",
        "Guardian Boss IV  //  Enumeration Trial"
    };

    private static final String[] LORE = {
        "Clad in dark obsidian space-armor, a crimson underworld toga, and a golden laurel wreath over spiky dark hair with heterochromia eyes. Wields the Stygian Star-Blade in close-quarters dash slashes.",
        "A cool, sharp-eyed cosmic sharpshooter with swept-back silver hair, a golden crescent-laurel headband, and an emerald targeting reticle. In Squad Pantheon mode, he steps in on Turn 2 to fire emerald-cyan plasma volleys from his Adamant Star-Gun.",
        "A fierce cosmic valkyrie with flowing crimson-copper hair, a golden winged laurel circlet, and a teal astral cloak over bronze-obsidian armor. In Squad Pantheon mode, she vaults into battle on Turn 3 to thrust her Eternal Astral Spear.",
        "A colossal four-horned obsidian juggernaut whose chest houses a raging crimson singularity. Its pauldrons are carved with skull reliefs that glow with molten cracks.",
        "An enigmatic six-eyed celestial sovereign crowned with a golden crescent halo and violet nebula ribbons, spinning fate-threads across the Astral Loom.",
        "An undead arch-sovereign wearing a jagged golden crown and bone pauldrons over tattered crimson vestments, wielding a floating emerald-and-magenta phylactery orb.",
        "A ferocious horned cosmic predator wreathed in cyan lightning fur and golden skull-crescent armor, prowling the craters of the Shattered Moon."
    };

    private static final String[] MECHANICS = {
        "SWORD PILOT: Each right answer in a row adds +10% damage, up to +50%. Every 3 right answers in a row earn a random power-up (Shield, Repair, Double Damage or Hint); you can hold 3. Click a power-up slot before you answer to use it.",
        "GUNNER (SQUAD PILOT 2): In a Squad battle pilots take turns in order, and the Gunner fires right after Pilot 1. Each pilot keeps their own hull, streak and power-ups. A pilot at 0 hull is skipped.",
        "SPEAR (SQUAD PILOT 3): Joins a 3-pilot squad and takes the turn after the Gunner. A wrong answer only hurts the pilot who answered; the squad loses when every pilot is down.",
        "IDENTIFICATION TRIAL: Shows a definition; click the matching term card. GROUND SLAM: every 3rd turn its strike deals 1.5 times its attack power.",
        "REASONING TRIAL: Asks you to explain a term; pick the card that explains it (the right one uses at least 60% of the key words). THREAD BIND: each strike locks one of your power-ups for 2 turns.",
        "MULTIPLE CHOICE TRIAL: Pick A, B, C or D. A Hint power-up hides 2 wrong options. HEX BOLT (level 3): a missed question comes back later with its choices reshuffled.",
        "ENUMERATION TRIAL: Select every item of the list, then press WEAVE & STRIKE. Each wrong item cancels a right one, and a partial list deals partial damage. MOON POUNCE: its strike deals 2 extra damage for each item you missed."
    };

    private static final String[] PORTRAIT_PATHS = {
        "portraits/portrait_cadet.png",
        "portraits/portrait_cadet_gunner.png",
        "portraits/portrait_cadet_spear.png",
        "portraits/portrait_void_brute.png",
        "portraits/portrait_star_weaver.png",
        "portraits/portrait_nebula_lich.png",
        "portraits/portrait_lunar_beast.png"
    };

    private static final String[] PIXEL_PATHS = {
        "sprites/hero_cadet.png",
        "sprites/hero_cadet_gunner.png",
        "sprites/hero_cadet_spear.png",
        "sprites/enemy_void_brute.png",
        "sprites/enemy_star_weaver.png",
        "sprites/enemy_nebula_lich.png",
        "sprites/enemy_lunar_beast.png"
    };

    public CodexScreen(CosmicQuestGame game) {
        this.game = game;
        this.assets = game.getAssets();
        this.stage = new Stage(CosmicTheme.newViewport(), game.getBatch());

        buildUi();
        refreshEntry();
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
        CosmicAudio.playMusic(CosmicAudio.MENU_THEME);
    }

    private void buildUi() {
        Skin skin = assets.getSkin();

        Table root = new Table();
        root.setFillParent(true);
        root.pad(18f, 24f, 18f, 24f);
        stage.addActor(root);

        // Header
        Table header = new Table();
        header.setBackground(skin.getDrawable("panel-dark"));
        header.pad(10f, 18f, 10f, 18f);
        header.add(new Label("PANTHEON CODEX  //  HADES PORTRAITS & 24-BIT PIXEL SPRITES", skin, "gold")).expandX().left();

        TextButton backBtn = new TextButton("BACK TO MAIN MENU", skin, "utility");
        backBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                CosmicAudio.playClick();
                game.transitionTo(new MainMenuScreen(game));
            }
        });
        header.add(backBtn).width(210f).height(38f).right();
        root.add(header).growX().height(64f).colspan(2).padBottom(14f).row();

        // Character Selector Tabs
        Table tabRow = new Table();
        String[] tabNames = {
            "1. SWORD",
            "2. STAR-GUN",
            "3. SPEAR",
            "4. VOID BRUTE",
            "5. STAR WEAVER",
            "6. NEBULA LICH",
            "7. LUNAR BEAST"
        };
        for (int i = 0; i < 7; i++) {
            final int idx = i;
            TextButton tBtn = new TextButton(tabNames[i], skin, "boon");
            tBtn.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    CosmicAudio.playClick();
                    selectedEntry = idx;
                    refreshEntry();
                }
            });
            tabButtons[i] = tBtn;
            tabRow.add(tBtn).growX().height(42f).padRight(i < 6 ? 6f : 0f);
        }
        root.add(tabRow).growX().colspan(2).padBottom(14f).row();

        // Main Content Split: Left reserved for Side-by-Side Portrait + Pixel Sprite, Right holds Lore & Loaded Lesson Scroll
        Table leftVisualCard = new Table();
        leftVisualCard.setBackground(skin.getDrawable("panel-dialogue"));
        leftVisualCard.pad(16f);
        leftVisualCard.add(new Label("1-TO-1 DESIGN COMPARISON", skin, "gold")).top().left().expandX().row();
        leftVisualCard.add().expandY().row();

        Table rightInfoCard = new Table();
        rightInfoCard.setBackground(skin.getDrawable("panel-dialogue"));
        rightInfoCard.pad(18f);

        entryTitleLabel = new Label("", skin, "title");
        entrySubLabel = new Label("", skin, "cyan");
        entryLoreLabel = new Label("", skin, "default");
        entryLoreLabel.setWrap(true);
        entryMechanicLabel = new Label("", skin, "small");
        entryMechanicLabel.setColor(CosmicTheme.TEXT_WARM);
        entryMechanicLabel.setWrap(true);

        rightInfoCard.add(entryTitleLabel).growX().left().row();
        rightInfoCard.add(entrySubLabel).growX().left().padBottom(10f).row();
        rightInfoCard.add(entryLoreLabel).growX().left().padBottom(10f).row();
        rightInfoCard.add(entryMechanicLabel).growX().left().padBottom(14f).row();

        // Active Lesson Knowledge Scroll
        rightInfoCard.add(new Label("ACTIVE LESSON SCROLL: " + game.getLessonName(), skin, "gold")).left().padBottom(6f).row();
        Table lessonTable = new Table();
        lessonTable.top().pad(8f);
        for (Term t : game.getLessonData().getTerms()) {
            Label tl = new Label("* " + t.getTerm() + " :: " + t.getDefinition(), skin, "small");
            tl.setWrap(true);
            lessonTable.add(tl).growX().left().padBottom(5f).row();
        }
        for (Map.Entry<String, List<String>> en : game.getLessonData().getEnumerations().entrySet()) {
            Label el = new Label("[ENUM] " + en.getKey() + " :: " + String.join(", ", en.getValue()), skin, "small");
            el.setColor(CosmicTheme.ASTRAL_CYAN);
            el.setWrap(true);
            lessonTable.add(el).growX().left().padBottom(5f).row();
        }
        ScrollPane scroll = new ScrollPane(lessonTable, skin);
        scroll.setFadeScrollBars(false);
        scroll.setScrollingDisabled(true, false);
        scroll.setOverscroll(false, false);
        rightInfoCard.add(scroll).grow();

        this.leftVisualCard = leftVisualCard;
        root.add(leftVisualCard).width(Value.percentWidth(LEFT_CARD_SHARE, root)).growY().padRight(16f);
        root.add(rightInfoCard).grow();
    }

    private void refreshEntry() {
        for (int i = 0; i < tabButtons.length; i++) {
            tabButtons[i].setChecked(i == selectedEntry);
        }
        entryTitleLabel.setText(NAMES[selectedEntry]);
        entrySubLabel.setText(SUBTITLES[selectedEntry]);
        entryLoreLabel.setText(LORE[selectedEntry]);
        entryMechanicLabel.setText(MECHANICS[selectedEntry]);
    }

    @Override
    public void render(float delta) {
        animTime += delta;
        ScreenUtils.clear(CosmicTheme.BG_OBSIDIAN);

        stage.getViewport().apply();
        SpriteBatch batch = game.getBatch();
        batch.setProjectionMatrix(stage.getCamera().combined);

        Texture bg = assets.loadTexture("backgrounds/env_deep_field.png", false);
        batch.begin();
        CosmicTheme.drawCover(batch, bg, stage.getViewport());
        batch.end();

        stage.act(delta);
        stage.draw();

        // Draw Detailed Hades Portrait + 24-Bit Pixel Sprite centred inside the left card,
        // scaled with the card (offsets are from the card centre at the 1280x720 design size).
        float s = Math.min(leftVisualCard.getWidth() / DESIGN_CARD_W, leftVisualCard.getHeight() / DESIGN_CARD_H);
        s = MathUtils.clamp(s, 0.6f, 1.6f);
        float cx = leftVisualCard.getX() + leftVisualCard.getWidth() * 0.5f;
        float cy = leftVisualCard.getY() + leftVisualCard.getHeight() * 0.5f;
        float bob = MathUtils.sin(animTime * 3.2f) * 5f;
        Texture portrait = assets.loadTexture(PORTRAIT_PATHS[selectedEntry], false);
        Texture pixel = assets.loadTexture(PIXEL_PATHS[selectedEntry], true);

        batch.begin();
        batch.draw(portrait, cx - 242f * s, cy - 138f * s + bob * 0.4f, 245f * s, 245f * s);
        batch.draw(pixel, cx + 21f * s, cy - 118f * s + bob, 204f * s, 185f * s);

        assets.fontSmall.setColor(CosmicTheme.GOLD_BRIGHT);
        assets.fontSmall.draw(batch, "DETAILED HADES PORTRAIT\n(Used in Q&A Dialogue)", cx - 216f * s, cy - 168f * s);
        assets.fontSmall.setColor(CosmicTheme.ASTRAL_CYAN);
        assets.fontSmall.draw(batch, "24-BIT PIXEL SPRITE\n(Used in 2D Attack Cutscene)", cx + 46f * s, cy - 168f * s);
        assets.fontSmall.setColor(Color.WHITE);
        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}
