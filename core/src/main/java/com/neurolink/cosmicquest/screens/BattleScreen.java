package com.neurolink.cosmicquest.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.neurolink.cosmicquest.CosmicQuestGame;
import com.neurolink.cosmicquest.logic.BattleMode;
import com.neurolink.cosmicquest.logic.BattleState;
import com.neurolink.cosmicquest.logic.PowerUp;
import com.neurolink.cosmicquest.logic.PowerUpInventory;
import com.neurolink.cosmicquest.logic.SoloBossBattle;
import com.neurolink.cosmicquest.logic.SquadBossBattle;
import com.neurolink.cosmicquest.logic.Term;
import com.neurolink.cosmicquest.logic.TurnResult;
import com.neurolink.cosmicquest.model.Entity;
import com.neurolink.cosmicquest.model.EnumerationQuestion;
import com.neurolink.cosmicquest.model.IdentificationQuestion;
import com.neurolink.cosmicquest.model.LunarBeast;
import com.neurolink.cosmicquest.model.MultipleChoiceQuestion;
import com.neurolink.cosmicquest.model.NebulaLich;
import com.neurolink.cosmicquest.model.Player;
import com.neurolink.cosmicquest.model.Question;
import com.neurolink.cosmicquest.model.ReasoningQuestion;
import com.neurolink.cosmicquest.model.StarWeaver;
import com.neurolink.cosmicquest.model.VoidBrute;
import com.neurolink.cosmicquest.ui.CosmicAssets;
import com.neurolink.cosmicquest.ui.CosmicAudio;
import com.neurolink.cosmicquest.ui.CosmicTheme;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Core Gameplay Screen implementing the alternating Hades Dialogue Q&A -> 2D Pixel Battle loop:
 * 1) HADES_DIALOGUE phase: High-detail Hades portraits + ornate gold/obsidian dialogue box
 *    with clickable Boon Answer Cards & Power-Ups.
 * 2) PIXEL_CUTSCENE phase: Dialogue slides out to reveal the 2D Pixel Battle Stage where
 *    the 24-bit pixel Cadet and Boss perform animated lunges, sword slashes, projectiles,
 *    hit-flashes, screen-shake, and floating damage callouts, then smoothly cycles back!
 */
public class BattleScreen extends ScreenAdapter {

    private enum Phase {
        PRE_FIGHT_INTRO,
        HADES_DIALOGUE,
        PIXEL_CUTSCENE
    }

    private static final float BASE_TURN_SECONDS = 30f;
    private static final float GRAVITY_SLAM_PENALTY = 5f;

    // 2D Pixel Stage Anchor Coordinates (Pokemon-inspired diagonal battle staging)
    private static final float HERO_BASE_X = 210f;
    private static final float HERO_BASE_Y = 305f;
    private static final float BOSS_BASE_X = 835f;
    private static final float BOSS_BASE_Y = 395f;

    private final CosmicQuestGame game;
    private final CosmicAssets assets;
    private final BattleMode battle;
    private final int bossIndex;
    private final int bossLevel;
    private final boolean squadMode;
    private final Stage stage;
    private final Random uiRandom = new Random();

    private Phase phase = Phase.HADES_DIALOGUE;
    private float animTime = 0f;

    // Smooth transition factor: 1.0 = Full Detailed Hades Dialogue, 0.0 = Pure 2D Pixel Battle Stage
    private float dialogueSlide = 0f;

    // Turn Timer
    private float maxTurnTime = BASE_TURN_SECONDS;
    private float remainingTime = BASE_TURN_SECONDS;
    private boolean gravitySlamActiveNextTurn = false;

    // Smooth HP Bar display values
    private float displayedHeroHp;
    private float displayedBossHp;

    // Cutscene Animation State
    private TurnResult lastTurnResult;
    private float cutsceneTimer = 0f;
    private float cutsceneDuration = 1.75f;
    private boolean playerStrikeSoundPlayed = false;
    private boolean bossStrikeSoundPlayed = false;
    private float screenShakeTime = 0f;
    private float screenShakeIntensity = 0f;

    // Floating Combat Texts & Particle Sparks on the 2D Pixel Stage
    private static class FloatingText {
        String text;
        float x;
        float y;
        float vy;
        float life;
        float maxLife;
        Color color;
        boolean large;
    }

    private static class Spark {
        float x;
        float y;
        float vx;
        float vy;
        float size;
        float life;
        float maxLife;
        Color color;
    }

    private final List<FloatingText> floatingTexts = new ArrayList<>();
    private final List<Spark> sparks = new ArrayList<>();

    // Scene2D Dialogue Widgets
    private Table dialogueRoot;
    private Label speakerTitleLabel;
    private Label turnBadgeLabel;
    private Label bossFlavorLabel;
    private Label questionTextLabel;
    private Label hintBannerLabel;
    private Table optionsTable;
    private final TextButton[] powerUpButtons = new TextButton[PowerUpInventory.MAX_SLOTS];
    private final List<String> selectedEnumItems = new ArrayList<>();
    private int actingHeroIndex = 0;

    public BattleScreen(CosmicQuestGame game, BattleMode battle, int bossIndex, int bossLevel, boolean squadMode) {
        this.game = game;
        this.assets = game.getAssets();
        this.battle = battle;
        this.bossIndex = bossIndex;
        this.bossLevel = bossLevel;
        this.squadMode = squadMode;
        this.stage = new Stage(new FitViewport(CosmicTheme.VIRTUAL_WIDTH, CosmicTheme.VIRTUAL_HEIGHT), game.getBatch());

        this.displayedHeroHp = getActiveHeroHp();
        this.displayedBossHp = battle.getEntity().getHp();
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    private Player getActivePlayer() {
        if (battle instanceof SoloBossBattle solo) {
            return solo.getPlayer();
        } else if (battle instanceof SquadBossBattle squad) {
            return squad.getCurrentPlayer();
        }
        return null;
    }

    private int getDialogueHeroIndex() {
        if (battle instanceof SquadBossBattle squad) {
            int idx = squad.getTeam().getPlayers().indexOf(squad.getCurrentPlayer());
            return Math.max(0, idx);
        }
        return 0;
    }

    private int getDisplayedHeroIndex() {
        return (phase == Phase.PIXEL_CUTSCENE) ? actingHeroIndex : getDialogueHeroIndex();
    }

    private String getHeroWeaponName(int heroIdx) {
        switch (heroIdx % 3) {
            case 1:
                return "ADAMANT STAR-GUN";
            case 2:
                return "ETERNAL ASTRAL SPEAR";
            default:
                return game.isUseAstralStaff() ? "ASTRAL STAFF" : "STYGIAN STAR-BLADE";
        }
    }

    private int getActiveHeroHp() {
        Player p = getActivePlayer();
        return p != null ? p.getHp() : 100;
    }

    private int getActiveHeroMaxHp() {
        Player p = getActivePlayer();
        return p != null ? p.getMaxHp() : 100;
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(CosmicTheme.BG_OBSIDIAN);
    }

    @Override public void resize(int width, int height) {}
    @Override public void dispose() {}
}
