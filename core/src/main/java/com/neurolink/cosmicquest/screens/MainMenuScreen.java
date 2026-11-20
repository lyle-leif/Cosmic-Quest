package com.neurolink.cosmicquest.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.neurolink.cosmicquest.CosmicQuestGame;
import com.neurolink.cosmicquest.console.ConsoleMain;
import com.neurolink.cosmicquest.logic.QuestionGenerator;
import com.neurolink.cosmicquest.logic.SoloBossBattle;
import com.neurolink.cosmicquest.logic.SquadBossBattle;
import com.neurolink.cosmicquest.model.Entity;
import com.neurolink.cosmicquest.model.LunarBeast;
import com.neurolink.cosmicquest.model.NebulaLich;
import com.neurolink.cosmicquest.model.Player;
import com.neurolink.cosmicquest.model.StarWeaver;
import com.neurolink.cosmicquest.model.Team;
import com.neurolink.cosmicquest.model.VoidBrute;
import com.neurolink.cosmicquest.ui.CosmicAssets;
import com.neurolink.cosmicquest.ui.CosmicAudio;
import com.neurolink.cosmicquest.ui.CosmicTheme;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntFunction;

/**
 * Hades II-Inspired Main Menu Screen faithful to ConsoleMain:
 * - Unnumbered main menu buttons: SOLO BATTLE, SQUAD BATTLE, LOAD LESSON, HOW TO PLAY, EXIT.
 * - One-slide-at-a-time Pilot Registration showing each pilot's Hades Portrait, 2D Pixel Sprite,
 *   and Weapon right inside the registration box.
 */
public class MainMenuScreen extends ScreenAdapter {

    private static final List<IntFunction<Entity>> ALIENS =
            List.of(VoidBrute::new, StarWeaver::new, NebulaLich::new, LunarBeast::new);

    private static final String[] PILOT_ARCHETYPES = {
        "VANGUARD BLADE-MASTER",
        "ASTRAL RAIL SHARPSHOOTER",
        "VALKYRIE SPEAR LANCER"
    };

    private static final String[] PILOT_WEAPONS = {
        "Stygian Star-Blade (Sword)",
        "Adamant Star-Gun (Plasma Pistol)",
        "Eternal Astral Spear (Runic Lance)"
    };

    private final CosmicQuestGame game;
    private final CosmicAssets assets;
    private final Stage stage;

    private float animTime = 0f;

    // Floating emerald/cyan arcane motes
    private static final int MOTE_COUNT = 38;
    private final float[] moteX = new float[MOTE_COUNT];
    private final float[] moteY = new float[MOTE_COUNT];
    private final float[] moteSpeed = new float[MOTE_COUNT];
    private final float[] moteSize = new float[MOTE_COUNT];
    private final boolean[] moteCyan = new boolean[MOTE_COUNT];

    public MainMenuScreen(CosmicQuestGame game) {
        this.game = game;
        this.assets = game.getAssets();
        this.stage = new Stage(new FitViewport(CosmicTheme.VIRTUAL_WIDTH, CosmicTheme.VIRTUAL_HEIGHT), game.getBatch());

        for (int i = 0; i < MOTE_COUNT; i++) {
            moteX[i] = MathUtils.random(0f, CosmicTheme.VIRTUAL_WIDTH);
            moteY[i] = MathUtils.random(0f, CosmicTheme.VIRTUAL_HEIGHT);
            moteSpeed[i] = MathUtils.random(12f, 38f);
            moteSize[i] = MathUtils.random(1.8f, 3.8f);
            moteCyan[i] = MathUtils.randomBoolean(0.35f);
        }

        buildHadesTitleUi();
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    private void buildHadesTitleUi() {
        Skin skin = assets.getSkin();

        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);

        // Left Column positioned directly below the COSMIC QUEST logo (no numbers on buttons)
        Table leftMenuCol = new Table();
        leftMenuCol.padLeft(85f).padTop(245f);

        TextButton soloBtn = new TextButton("S O L O   B A T T L E", skin, "hades-menu");
        soloBtn.getLabel().setAlignment(Align.center);
        soloBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                CosmicAudio.playBoonActivate();
                showSoloBattleDialog();
            }
        });

        TextButton squadBtn = new TextButton("S Q U A D   B A T T L E", skin, "hades-menu");
        squadBtn.getLabel().setAlignment(Align.center);
        squadBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                CosmicAudio.playBoonActivate();
                showSquadBattleDialog();
            }
        });

        TextButton loadBtn = new TextButton("L O A D   L E S S O N", skin, "hades-menu");
        loadBtn.getLabel().setAlignment(Align.center);
        loadBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                CosmicAudio.playClick();
                showLoadLessonDialog();
            }
        });

        TextButton helpBtn = new TextButton("H O W   T O   P L A Y", skin, "hades-menu");
        helpBtn.getLabel().setAlignment(Align.center);
        helpBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                CosmicAudio.playClick();
                showHowToPlayDialog();
            }
        });

        TextButton exitBtn = new TextButton("E X I T", skin, "hades-menu");
        exitBtn.getLabel().setAlignment(Align.center);
        exitBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.exit();
            }
        });

        leftMenuCol.add(soloBtn).width(380f).height(50f).padBottom(6f).row();
        leftMenuCol.add(squadBtn).width(380f).height(48f).padBottom(6f).row();
        leftMenuCol.add(loadBtn).width(380f).height(46f).padBottom(6f).row();
        leftMenuCol.add(helpBtn).width(380f).height(46f).padBottom(6f).row();
        leftMenuCol.add(exitBtn).width(380f).height(46f).row();

        root.add(leftMenuCol).left().expand();
    }

    private void showSoloBattleDialog() {}
    private void showSquadBattleDialog() {}
    private void showLoadLessonDialog() {}
    private void showHowToPlayDialog() {}

    public void render(float delta) {
        animTime += delta;
        ScreenUtils.clear(CosmicTheme.BG_OBSIDIAN);

        stage.getViewport().apply();
        SpriteBatch batch = game.getBatch();
        ShapeRenderer shapes = game.getShapes();
        batch.setProjectionMatrix(stage.getCamera().combined);
        shapes.setProjectionMatrix(stage.getCamera().combined);

        // 1. Fullscreen Hades II-Style Splash Key Art
        Texture splash = assets.loadTexture("backgrounds/menu_hades_splash.png", false);
        Texture logo = assets.loadTexture("ui/hades_logo.png", false);
        float logoBob = MathUtils.sin(animTime * 2.2f) * 3.5f;

        batch.begin();
        batch.setColor(Color.WHITE);
        batch.draw(splash, 0, 0, CosmicTheme.VIRTUAL_WIDTH, CosmicTheme.VIRTUAL_HEIGHT);
        batch.end();

        // 2. Subtle Animated Emerald & Cyan Arcane Dust Motes
        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        for (int i = 0; i < MOTE_COUNT; i++) {
            moteY[i] += moteSpeed[i] * delta;
            moteX[i] += MathUtils.sin(animTime * 1.6f + i) * 10f * delta;
            if (moteY[i] > CosmicTheme.VIRTUAL_HEIGHT + 10f) {
                moteY[i] = -10f;
                moteX[i] = MathUtils.random(0f, CosmicTheme.VIRTUAL_WIDTH);
            }
            if (moteCyan[i]) {
                shapes.setColor(0.13f, 0.90f, 1.0f, 0.45f);
            } else {
                shapes.setColor(0.15f, 0.96f, 0.58f, 0.50f);
            }
            shapes.circle(moteX[i], moteY[i], moteSize[i]);
        }
        shapes.end();

        // 3. Draw the COSMIC QUEST I Title Logo + ConsoleMain Status Lines (lesson & AI status)
        batch.begin();
        batch.draw(logo, 52f, 448f + logoBob, 520f, 192f);

        assets.fontSmall.setColor(0.55f, 0.78f, 0.68f, 0.85f);
        assets.fontSmall.draw(batch, "MAIN MENU  (lesson: " + game.getLessonName() + ")", 42f, 48f);
        assets.fontSmall.draw(batch, game.getAiStatusText(), 42f, 26f);
        assets.fontSmall.draw(batch, "v1.0.0  //  STARS: " + game.getTotalStarsEarned(), 1085f, 704f);
        assets.fontSmall.setColor(Color.WHITE);
        batch.end();

        // 4. Draw Menu Buttons (SOLO BATTLE, SQUAD BATTLE, LOAD LESSON, HOW TO PLAY, EXIT)
        stage.act(delta);
        stage.draw();
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
