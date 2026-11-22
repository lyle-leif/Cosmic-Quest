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
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.neurolink.cosmicquest.CosmicQuestGame;
import com.neurolink.cosmicquest.ui.CosmicAssets;
import com.neurolink.cosmicquest.ui.CosmicAudio;
import com.neurolink.cosmicquest.ui.CosmicTheme;

/**
 * Interactive 6-Scene Prologue Cutscene Screen ("The Exodus of Terra & Fall of Nova Elysium").
 */
public class PrologueScreen extends ScreenAdapter {

    private static final String[] SCENE_TITLES = {
        "SCENE I  //  YEAR 2194: THE WITHERED CRADLE (DYSTOPIAN EARTH)",
        "SCENE II  //  VOYAGE OF THE ARK-SHIP AEGIS-IX",
        "SCENE III  //  NEW EDEN: THE COLONY OF NOVA ELYSIUM",
        "SCENE IV  //  THE ECLIPSE INVASION: FOUR COSMIC TITANS",
        "SCENE V  //  THE COLONIAL GOVERNMENT'S MANDATE",
        "SCENE VI  //  PROTOCOL OLYMPUS: THE TRIAL BEGINS"
    };

    private static final String[] SCENE_SPEAKERS = {
        "CHRONICLE ARCHIVE  //  TERRA FINAL LOG",
        "COMMANDER OF ARK AEGIS-IX",
        "NOVA ELYSIUM COLONIAL CHRONICLE",
        "PLANETARY DEFENSE ALERT",
        "HIGH CHANCELLOR OF NOVA ELYSIUM",
        "THE CADET  //  VANGUARD OF HUMANITY"
    };

    private static final String[] SCENE_TEXTS = {
        "\"Earth did not end in fire... it ended in hunger. By 2194, the last harvests turned to ash, the oceans dried to salt, and our cradle became a starving dystopia with no resources left to sustain human life.\"",
        "\"With nothing left to harvest on Earth, the last 50,000 survivors boarded the Colony Ark AEGIS-IX -- entrusting their lives to a lone astral vanguard: The Cadet, piloting into the uncharted deep void.\"",
        "\"Beyond the Andromeda Drift, we discovered a lush sanctuary planet: NOVA ELYSIUM. For seven golden years, humanity planted new fields, raised crystalline dome-cities, and believed we were finally safe.\"",
        "\"We were wrong. Drawn by our new planet's living core, four ancient Cosmic Entities from rival worlds tore open the sky -- the Void Brute, Star Weaver, Nebula Lich, and Lunar Beast -- invading our sanctuary!\"",
        "\"Cadet! The Colonial Government commissions you under Protocol Olympus. Sync your Stygian Star-Blade with the NeuroLink Codex, strike down the four Cosmic Invaders, and free Nova Elysium from their grasp!\"",
        "\"Earth fell once. Nova Elysium will not fall on my watch. Every correct decree fuels my blade -- let the Cosmic Invaders come!\""
    };

    private static final String[] SCENE_BACKDROPS = {
        "backgrounds/env_wreckage_drift.png",
        "backgrounds/env_deep_field.png",
        "backgrounds/env_andromeda_drift.png",
        "backgrounds/env_event_horizon.png",
        "backgrounds/env_crimson_necropolis.png",
        "backgrounds/env_supernova_bloom.png"
    };

    private final CosmicQuestGame game;
    private final CosmicAssets assets;
    private final Stage stage;
    private int sceneIndex = 0;
    private float animTime = 0f;

    private Label sceneHeaderLabel;
    private Label speakerLabel;
    private Label storyTextLabel;
    private TextButton nextBtn;

    public PrologueScreen(CosmicQuestGame game) {
        this.game = game;
        this.assets = game.getAssets();
        this.stage = new Stage(new FitViewport(CosmicTheme.VIRTUAL_WIDTH, CosmicTheme.VIRTUAL_HEIGHT), game.getBatch());

        buildUi();
        refreshScene();
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    private void buildUi() {
        Skin skin = assets.getSkin();

        Table root = new Table();
        root.setFillParent(true);
        root.pad(18f, 24f, 18f, 24f);
        stage.addActor(root);

        // Top Bar
        Table topBar = new Table();
        topBar.setBackground(skin.getDrawable("panel-dark"));
        topBar.pad(10f, 18f, 10f, 18f);

        sceneHeaderLabel = new Label("", skin, "gold");
        topBar.add(sceneHeaderLabel).left().expandX();

        TextButton skipBtn = new TextButton("SKIP PROLOGUE", skin, "utility");
        skipBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                CosmicAudio.playClick();
                game.setScreen(new MainMenuScreen(game));
            }
        });
        topBar.add(skipBtn).width(170f).height(36f).right();

        root.add(topBar).growX().height(60f).top().row();
        root.add().expandY().row();

        // Bottom Hades Narrative Box
        Table box = new Table();
        box.setBackground(skin.getDrawable("panel-dialogue"));
        box.pad(20f, 26f, 20f, 26f);

        speakerLabel = new Label("", skin, "cyan");
        box.add(speakerLabel).width(1150f).left().colspan(2).padBottom(8f).row();

        storyTextLabel = new Label("", skin, "medium");
        storyTextLabel.setColor(CosmicTheme.TEXT_WARM);
        storyTextLabel.setWrap(true);
        box.add(storyTextLabel).width(1150f).left().colspan(2).padBottom(18f).row();

        TextButton prevBtn = new TextButton("PREVIOUS SCENE", skin, "utility");
        prevBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (sceneIndex > 0) {
                    CosmicAudio.playClick();
                    sceneIndex--;
                    refreshScene();
                }
            }
        });

        nextBtn = new TextButton("NEXT SCENE  >>", skin);
        nextBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                CosmicAudio.playBoonActivate();
                if (sceneIndex < SCENE_TITLES.length - 1) {
                    sceneIndex++;
                    refreshScene();
                } else {
                    game.setScreen(new MainMenuScreen(game));
                }
            }
        });

        box.add(prevBtn).width(210f).height(48f).left();
        box.add(nextBtn).width(280f).height(48f).right();

        root.add(box).width(1220f).height(250f).bottom();
    }

    private void refreshScene() {
        sceneHeaderLabel.setText(SCENE_TITLES[sceneIndex] + "   (" + (sceneIndex + 1) + " / " + SCENE_TITLES.length + ")");
        speakerLabel.setText(SCENE_SPEAKERS[sceneIndex]);
        storyTextLabel.setText(SCENE_TEXTS[sceneIndex]);
        if (sceneIndex == SCENE_TITLES.length - 1) {
            nextBtn.setText("ENTER PANTHEON  >>");
        } else {
            nextBtn.setText("NEXT SCENE  >>");
        }
    }

    @Override
    public void render(float delta) {
        animTime += delta;
        ScreenUtils.clear(CosmicTheme.BG_OBSIDIAN);

        stage.getViewport().apply();
        SpriteBatch batch = game.getBatch();
        batch.setProjectionMatrix(stage.getCamera().combined);

        Texture bg = assets.loadTexture(SCENE_BACKDROPS[sceneIndex], false);
        float bob = MathUtils.sin(animTime * 2.8f) * 6f;

        batch.begin();
        batch.draw(bg, 0, 0, CosmicTheme.VIRTUAL_WIDTH, CosmicTheme.VIRTUAL_HEIGHT);

        // Draw 8-bit pixel sprites corresponding to each Prologue beat
        if (sceneIndex == 1 || sceneIndex == 2 || sceneIndex == 4 || sceneIndex == 5) {
            batch.draw(assets.getCadetPixelSprite(false), 180f, 295f + bob, 212f, 192f);
        }
        if (sceneIndex == 3 || sceneIndex == 4) {
            batch.draw(assets.loadTexture("sprites/enemy_void_brute.png", true), 520f, 305f - bob * 0.6f, 165f, 165f);
            batch.draw(assets.loadTexture("sprites/enemy_star_weaver.png", true), 700f, 315f + bob * 0.6f, 165f, 165f);
            batch.draw(assets.loadTexture("sprites/enemy_nebula_lich.png", true), 880f, 305f - bob * 0.6f, 165f, 165f);
            batch.draw(assets.loadTexture("sprites/enemy_lunar_beast.png", true), 1060f, 315f + bob * 0.6f, 165f, 165f);
        }
        batch.end();

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
