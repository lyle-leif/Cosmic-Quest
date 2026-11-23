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
import com.neurolink.cosmicquest.logic.SquadBossBattle;
import com.neurolink.cosmicquest.model.Entity;
import com.neurolink.cosmicquest.model.Player;
import com.neurolink.cosmicquest.model.Team;
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

    private static final List<IntFunction<Entity>> ALIENS = CosmicQuestGame.ALIENS;

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
        CosmicAudio.playMusic(CosmicAudio.MENU_THEME);
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

        TextButton prologueBtn = new TextButton("P R O L O G U E", skin, "hades-menu");
        prologueBtn.getLabel().setAlignment(Align.center);
        prologueBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                CosmicAudio.playClick();
                game.transitionTo(new PrologueScreen(game));
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
        leftMenuCol.add(prologueBtn).width(380f).height(46f).padBottom(6f).row();
        leftMenuCol.add(exitBtn).width(380f).height(46f).row();

        root.add(leftMenuCol).left().expand();
    }

    /**
     * Builds a visual Pilot Registration Card for a single pilot (pilotIndex = 0, 1, or 2),
     * showing ONLY their high-detail Hades Bust Portrait anchored flush to the bottom border
     * alongside their clean, non-overlapping Name Input Form!
     */
    private Table buildSinglePilotEntryCard(
            int pilotIndex,
            String defaultName,
            String currentValue,
            String promptText,
            String buttonLabel,
            java.util.function.Consumer<String> onConfirm) {

        Skin skin = assets.getSkin();
        Table card = new Table();
        card.setBackground(skin.getDrawable("panel-dark"));
        // Bottom padding = 2f so the portrait sits directly on top of the 2px gold bottom border!
        card.pad(14f, 16f, 2f, 26f);

        // Left: High-Detail Hades Art Portrait anchored flush to the bottom border of the card
        Image portraitImg = new Image(assets.getCadetPortrait(pilotIndex));
        portraitImg.setScaling(Scaling.fit);
        portraitImg.setAlign(Align.bottom);

        // Right Input Form: Archetype, Weapon, TextField, and Confirm Button
        Table formCol = new Table();
        formCol.padBottom(14f);

        Label roleLabel = new Label(PILOT_ARCHETYPES[pilotIndex % 3], skin, "gold");
        Label wepLabel = new Label("Weapon: " + PILOT_WEAPONS[pilotIndex % 3], skin, "small");
        wepLabel.setColor(CosmicTheme.ASTRAL_CYAN);
        wepLabel.setWrap(true);
        Label promptLbl = new Label(promptText, skin, "small");

        final TextField nameField = new TextField(currentValue.equals(defaultName) ? "" : currentValue, skin);
        nameField.setMessageText(defaultName);

        TextButton confirmBtn = new TextButton(buttonLabel, skin, "boon");
        confirmBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                String typed = nameField.getText().strip();
                String finalName = typed.isEmpty() ? defaultName : typed;
                onConfirm.accept(finalName);
            }
        });

        formCol.add(roleLabel).width(380f).left().padBottom(4f).row();
        formCol.add(wepLabel).width(380f).left().padBottom(14f).row();
        formCol.add(promptLbl).width(380f).left().padBottom(6f).row();
        formCol.add(nameField).width(380f).height(42f).left().padBottom(14f).row();
        formCol.add(confirmBtn).width(380f).height(48f).left().row();

        card.add(portraitImg).size(210f, 210f).bottom().padRight(24f);
        card.add(formCol).width(385f).center().expandX();

        stage.setKeyboardFocus(nameField);
        return card;
    }

    /**
     * Faithful to ConsoleMain.playSolo():
     * Slide 1: Pilot Registration (with Pilot 1 Portrait + 2D Pixel Sprite & Sword)
     * Slide 2: Choose an Alien
     * Slide 3: Choose a Level (Level 1, Level 2, Level 3 Boss)
     */
    private void showSoloBattleDialog() {
        Skin skin = assets.getSkin();
        final Dialog dialog = new Dialog("", skin);
        dialog.setBackground(skin.getDrawable("panel-dialogue"));
        dialog.pad(26f, 34f, 24f, 34f);

        final Table content = dialog.getContentTable();
        content.pad(6f);

        final int[] step = {1};
        final String[] pilotName = {"Pilot"};
        final int[] chosenAlienIdx = {0};

        final Label badgeLabel = new Label("", skin, "cyan");
        final Label titleLabel = new Label("", skin, "title");
        final Label subLabel = new Label("", skin, "muted");

        content.add(badgeLabel).left().padBottom(2f).row();
        content.add(titleLabel).left().padBottom(4f).row();
        content.add(subLabel).left().padBottom(14f).row();

        final Table body = new Table();
        content.add(body).width(668f).minHeight(205f).center().row();

        final TextButton backBtn = new TextButton("<< BACK", skin, "utility");
        final TextButton cancelBtn = new TextButton("CANCEL", skin, "utility");

        final Runnable[] renderStep = new Runnable[1];
        renderStep[0] = () -> {
            body.clearChildren();

            if (step[0] == 1) {
                badgeLabel.setText("SOLO BATTLE   //   STEP 1 OF 3");
                titleLabel.setText("ENTER PILOT NAME");
                subLabel.setText("Register your pilot before choosing an alien opponent.");
                backBtn.setVisible(false);

                Table pilotCard = buildSinglePilotEntryCard(
                        0,
                        "Pilot",
                        pilotName[0],
                        "Pilot name (blank for Pilot):",
                        "CONFIRM PILOT  >>",
                        chosenName -> {
                            CosmicAudio.playClick();
                            pilotName[0] = chosenName;
                            step[0] = 2;
                            renderStep[0].run();
                        });
                body.add(pilotCard).width(656f).center().row();

            } else if (step[0] == 2) {
                badgeLabel.setText("SOLO BATTLE   //   STEP 2 OF 3   (Pilot: " + pilotName[0] + ")");
                titleLabel.setText("CHOOSE AN ALIEN");
                subLabel.setText("Select which alien to fight (lesson: " + game.getLessonName() + "):");
                backBtn.setVisible(true);

                QuestionGenerator gen = game.createGenerator();
                List<Integer> availableIndices = new ArrayList<>();
                for (int i = 0; i < ALIENS.size(); i++) {
                    Entity sample = ALIENS.get(i).apply(1);
                    if (gen.supports(sample.getQuestionType())) {
                        availableIndices.add(i);
                    }
                }

                Table grid = new Table();
                for (int listPos = 0; listPos < availableIndices.size(); listPos++) {
                    final int alienIdx = availableIndices.get(listPos);
                    Entity sample = ALIENS.get(alienIdx).apply(1);
                    String labelText = sample.getName().toUpperCase() + "\n" + sample.getSpecialMove();
                    TextButton card = new TextButton(labelText, skin, "boon");
                    card.getLabel().setWrap(true);
                    card.pad(10f, 14f, 10f, 14f);
                    card.addListener(new ClickListener() {
                        @Override
                        public void clicked(InputEvent event, float x, float y) {
                            CosmicAudio.playClick();
                            chosenAlienIdx[0] = alienIdx;
                            step[0] = 3;
                            renderStep[0].run();
                        }
                    });
                    grid.add(card).width(318f).height(84f).pad(6f);
                    if (listPos % 2 == 1) {
                        grid.row();
                    }
                }
                body.add(grid).center().row();

            } else {
                Entity sample = ALIENS.get(chosenAlienIdx[0]).apply(1);
                badgeLabel.setText("SOLO BATTLE   //   STEP 3 OF 3   (" + pilotName[0] + " vs " + sample.getName() + ")");
                titleLabel.setText("CHOOSE A LEVEL");
                subLabel.setText(sample.getName() + " -- " + sample.getSpecialMove());
                backBtn.setVisible(true);

                String[] levelLabels = {"LEVEL 1\nInitiate", "LEVEL 2\nVeteran", "LEVEL 3\nBoss"};
                Table lvlRow = new Table();
                for (int l = 0; l < 3; l++) {
                    final int levelVal = l + 1;
                    Entity lvlEntity = ALIENS.get(chosenAlienIdx[0]).apply(levelVal);
                    String btnText = levelLabels[l] + "\n(" + lvlEntity.getMaxHp() + " Hull  |  " + lvlEntity.getAttackPower() + " ATK)";
                    TextButton lvlBtn = new TextButton(btnText, skin, "boon");
                    lvlBtn.pad(12f, 14f, 12f, 14f);
                    lvlBtn.addListener(new ClickListener() {
                        @Override
                        public void clicked(InputEvent event, float x, float y) {
                            CosmicAudio.playBoonActivate();
                            dialog.hide();
                            launchSoloBattle(pilotName[0], chosenAlienIdx[0], levelVal);
                        }
                    });
                    lvlRow.add(lvlBtn).width(206f).height(98f).pad(8f);
                }
                body.add(lvlRow).center().row();
            }

            dialog.pack();
            dialog.setPosition(
                    Math.round((CosmicTheme.VIRTUAL_WIDTH - dialog.getWidth()) / 2f),
                    Math.round((CosmicTheme.VIRTUAL_HEIGHT - dialog.getHeight()) / 2f));
        };

        backBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                CosmicAudio.playClick();
                if (step[0] > 1) {
                    step[0]--;
                    renderStep[0].run();
                }
            }
        });

        cancelBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                CosmicAudio.playClick();
                dialog.hide();
            }
        });

        Table footer = new Table();
        footer.add(backBtn).width(180f).height(42f).left().padRight(260f);
        footer.add(cancelBtn).width(180f).height(42f).right();
        dialog.getButtonTable().add(footer).padTop(10f);

        dialog.show(stage);
        renderStep[0].run();
    }

    /**
     * Faithful to ConsoleMain.playSquad() with ONE-SLIDE-AT-A-TIME Pilot Registration:
     * - Slide 0: "HOW MANY PILOTS?" (2 Pilots or 3 Pilots)
     * - Slide 1: Pilot 1 Name + Portrait & Sword Sprite
     * - Slide 2: Pilot 2 Name + Portrait & Star-Gun Sprite
     * - Slide 3 (if 3 pilots): Pilot 3 Name + Portrait & Astral Spear Sprite
     * - Final Slide: "CHOOSE AN ALIEN" (Level 3 Boss)
     */
    private void showSquadBattleDialog() {
        Skin skin = assets.getSkin();
        final Dialog dialog = new Dialog("", skin);
        dialog.setBackground(skin.getDrawable("panel-dialogue"));
        dialog.pad(26f, 34f, 24f, 34f);

        final Table content = dialog.getContentTable();
        content.pad(6f);

        // slideIndex: 0 = How Many Pilots, 1..pilotCount = Pilot i Registration, pilotCount + 1 = Choose Alien
        final int[] slideIndex = {0};
        final int[] pilotCount = {2};
        final String[] pilotNames = {"Pilot 1", "Pilot 2", "Pilot 3"};

        final Label badgeLabel = new Label("", skin, "cyan");
        final Label titleLabel = new Label("", skin, "title");
        final Label subLabel = new Label("", skin, "muted");
        final Label errorLabel = new Label("", skin, "crimson");

        content.add(badgeLabel).left().padBottom(2f).row();
        content.add(titleLabel).left().padBottom(4f).row();
        content.add(subLabel).left().padBottom(6f).row();
        content.add(errorLabel).left().padBottom(6f).row();

        final Table body = new Table();
        content.add(body).width(668f).minHeight(205f).center().row();

        final TextButton backBtn = new TextButton("<< BACK", skin, "utility");
        final TextButton cancelBtn = new TextButton("CANCEL", skin, "utility");

        final Runnable[] renderSlide = new Runnable[1];
        renderSlide[0] = () -> {
            body.clearChildren();
            errorLabel.setText("");
            int totalSlides = pilotCount[0] + 2;

            if (slideIndex[0] == 0) {
                badgeLabel.setText("SQUAD BATTLE   //   SQUAD SIZE");
                titleLabel.setText("HOW MANY PILOTS?");
                subLabel.setText("2 or 3 pilots take turns against a Level 3 boss, switching character & weapon on each turn.");
                backBtn.setVisible(false);

                Table sizeRow = new Table();

                TextButton twoBtn = new TextButton("2 PILOTS\nSword  +  Star-Gun", skin, "boon");
                twoBtn.pad(14f, 18f, 14f, 18f);
                twoBtn.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        CosmicAudio.playClick();
                        pilotCount[0] = 2;
                        slideIndex[0] = 1;
                        renderSlide[0].run();
                    }
                });

                TextButton threeBtn = new TextButton("3 PILOTS\nSword  +  Star-Gun  +  Spear", skin, "boon");
                threeBtn.pad(14f, 18f, 14f, 18f);
                threeBtn.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        CosmicAudio.playClick();
                        pilotCount[0] = 3;
                        slideIndex[0] = 1;
                        renderSlide[0].run();
                    }
                });

                sizeRow.add(twoBtn).width(314f).height(98f).pad(10f);
                sizeRow.add(threeBtn).width(314f).height(98f).pad(10f);
                body.add(sizeRow).center().row();

            } else if (slideIndex[0] >= 1 && slideIndex[0] <= pilotCount[0]) {
                final int pIdx = slideIndex[0] - 1; // 0, 1, or 2
                final int pilotNum = slideIndex[0];
                String defName = "Pilot " + pilotNum;

                badgeLabel.setText("SQUAD BATTLE   //   PILOT " + pilotNum + " OF " + pilotCount[0]);
                titleLabel.setText("REGISTER PILOT " + pilotNum);
                subLabel.setText("Enter a unique callsign for Pilot " + pilotNum + " (or leave blank for " + defName + ").");
                backBtn.setVisible(true);

                String nextLabel = (pilotNum < pilotCount[0])
                        ? "NEXT: PILOT " + (pilotNum + 1) + "  >>"
                        : "NEXT: CHOOSE AN ALIEN  >>";

                Table pilotCard = buildSinglePilotEntryCard(
                        pIdx,
                        defName,
                        pilotNames[pIdx],
                        "Pilot " + pilotNum + " name (blank for " + defName + "):",
                        nextLabel,
                        chosenName -> {
                            // Validate with Team.addPlayer up through pilotNum so duplicates are caught immediately!
                            Team checkTeam = new Team("Squad");
                            for (int prev = 0; prev < pIdx; prev++) {
                                checkTeam.addPlayer(new Player(pilotNames[prev]));
                            }
                            try {
                                checkTeam.addPlayer(new Player(chosenName));
                                pilotNames[pIdx] = chosenName;
                                CosmicAudio.playClick();
                                slideIndex[0]++;
                                renderSlide[0].run();
                            } catch (IllegalArgumentException ex) {
                                CosmicAudio.playBossHit();
                                errorLabel.setText("! " + ex.getMessage() + " Pick another name.");
                            }
                        });
                body.add(pilotCard).width(656f).center().row();

            } else {
                StringBuilder rosterSummary = new StringBuilder();
                for (int i = 0; i < pilotCount[0]; i++) {
                    if (i > 0) {
                        rosterSummary.append("  |  ");
                    }
                    rosterSummary.append(pilotNames[i]);
                }
                badgeLabel.setText("SQUAD BATTLE   //   FINAL STEP   (Squad: " + rosterSummary + ")");
                titleLabel.setText("CHOOSE AN ALIEN  (LEVEL 3 BOSS)");
                subLabel.setText("Select which Level 3 Boss your squad will fight:");
                backBtn.setVisible(true);

                QuestionGenerator gen = game.createGenerator();
                List<Integer> availableIndices = new ArrayList<>();
                for (int i = 0; i < ALIENS.size(); i++) {
                    Entity sample = ALIENS.get(i).apply(1);
                    if (gen.supports(sample.getQuestionType())) {
                        availableIndices.add(i);
                    }
                }

                Table grid = new Table();
                for (int listPos = 0; listPos < availableIndices.size(); listPos++) {
                    final int alienIdx = availableIndices.get(listPos);
                    Entity sample = ALIENS.get(alienIdx).apply(SquadBossBattle.BOSS_LEVEL);
                    String labelText = sample.getName().toUpperCase() + " (Lv.3 - " + sample.getMaxHp() + " Hull)\n" + sample.getSpecialMove();
                    TextButton card = new TextButton(labelText, skin, "boon");
                    card.getLabel().setWrap(true);
                    card.pad(10f, 14f, 10f, 14f);
                    card.addListener(new ClickListener() {
                        @Override
                        public void clicked(InputEvent event, float x, float y) {
                            CosmicAudio.playBoonActivate();
                            dialog.hide();
                            launchSquadBattle(pilotCount[0], pilotNames, alienIdx);
                        }
                    });
                    grid.add(card).width(318f).height(86f).pad(6f);
                    if (listPos % 2 == 1) {
                        grid.row();
                    }
                }
                body.add(grid).center().row();
            }

            dialog.pack();
            dialog.setPosition(
                    Math.round((CosmicTheme.VIRTUAL_WIDTH - dialog.getWidth()) / 2f),
                    Math.round((CosmicTheme.VIRTUAL_HEIGHT - dialog.getHeight()) / 2f));
        };

        backBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                CosmicAudio.playClick();
                if (slideIndex[0] > 0) {
                    slideIndex[0]--;
                    renderSlide[0].run();
                }
            }
        });

        cancelBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                CosmicAudio.playClick();
                dialog.hide();
            }
        });

        Table footer = new Table();
        footer.add(backBtn).width(180f).height(42f).left().padRight(260f);
        footer.add(cancelBtn).width(180f).height(42f).right();
        dialog.getButtonTable().add(footer).padTop(10f);

        dialog.show(stage);
        renderSlide[0].run();
    }

    /**
     * Faithful to ConsoleMain.promptLesson():
     * "Type the path to a lesson .txt file (blank to cancel):"
     */
    private void showLoadLessonDialog() {
        Skin skin = assets.getSkin();
        final Dialog dialog = new Dialog("", skin);
        dialog.setBackground(skin.getDrawable("panel-dialogue"));
        dialog.pad(26f, 34f, 24f, 34f);

        Table content = dialog.getContentTable();
        content.pad(8f);

        content.add(new Label("LOAD LESSON", skin, "title")).left().padBottom(6f).row();
        content.add(new Label("Current lesson: " + game.getLessonName()
                + "  (" + game.getLessonData().getTerms().size() + " terms, "
                + game.getLessonData().getEnumerations().size() + " lists)", skin, "cyan")).left().padBottom(12f).row();
        content.add(new Label("Type the path to a lesson .txt file (blank to cancel):", skin, "default")).left().padBottom(8f).row();

        final TextField pathField = new TextField("", skin);
        pathField.setMessageText("assets/lessons/OOP_Fundamentals.txt");
        content.add(pathField).width(560f).height(42f).left().padBottom(10f).row();

        final Label feedbackLabel = new Label("", skin, "small");
        feedbackLabel.setWrap(true);
        content.add(feedbackLabel).width(560f).left().padBottom(10f).row();

        Table btnRow = new Table();
        TextButton loadBtn = new TextButton("LOAD LESSON", skin, "boon");
        loadBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                String typed = pathField.getText().strip().replace("\"", "");
                if (typed.isEmpty()) {
                    CosmicAudio.playClick();
                    dialog.hide();
                    return;
                }
                String err = game.loadLessonWithError(typed);
                if (err == null) {
                    CosmicAudio.playBoonActivate();
                    feedbackLabel.setColor(CosmicTheme.EMERALD_HEAL);
                    feedbackLabel.setText("Loaded lesson: " + game.getLessonName());
                } else {
                    CosmicAudio.playBossHit();
                    feedbackLabel.setColor(CosmicTheme.BLOOD_RED);
                    feedbackLabel.setText("! " + err);
                }
            }
        });

        TextButton closeBtn = new TextButton("DONE / CANCEL", skin, "utility");
        closeBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                CosmicAudio.playClick();
                dialog.hide();
            }
        });

        btnRow.add(loadBtn).width(240f).height(46f).padRight(16f);
        btnRow.add(closeBtn).width(200f).height(46f);
        dialog.getButtonTable().add(btnRow);

        dialog.show(stage);
        stage.setKeyboardFocus(pathField);
    }

    /**
     * Faithful to ConsoleRenderer.showHowToPlay(30) + quick buttons for Prologue, Codex, and Audio.
     */
    private void showHowToPlayDialog() {
        Skin skin = assets.getSkin();
        final Dialog dialog = new Dialog("", skin);
        dialog.setBackground(skin.getDrawable("panel-dialogue"));
        dialog.pad(24f, 32f, 22f, 32f);

        Table content = dialog.getContentTable();
        content.pad(6f);

        content.add(new Label("HOW TO PLAY", skin, "title")).left().padBottom(10f).row();

        String[] rules = {
            "- Pick an alien and a level. Each alien asks one type of question.",
            "- A right answer hits the alien. Answers in a row add up to +50% damage.",
            "- A wrong answer lets the alien strike, and the question comes back later.",
            "- Answer within " + ConsoleMain.ANSWER_TIME_LIMIT_SECONDS + " seconds, or it counts as a miss.",
            "- Every 3 right answers in a row earn a power-up (up to 3).",
            "- Squad: 2 or 3 pilots take turns against a level 3 boss (Pilot 1 uses Sword, Pilot 2 uses Gun, Pilot 3 uses Spear). A pilot at 0 hull is skipped; the squad loses when every pilot is down."
        };
        for (String r : rules) {
            Label rl = new Label(r, skin, "default");
            rl.setWrap(true);
            content.add(rl).width(660f).left().padBottom(5f).row();
        }

        content.add(new Label("Battle commands & controls:", skin, "gold")).left().padTop(8f).padBottom(4f).row();
        Label cmdLbl = new Label(
                "  Power-up slots 1) 2) 3) (top of the question box) -- click one before you answer\n"
                + "  RETREAT (top left) -- give up the battle; it counts as a loss\n"
                + "  Enumeration: pick only the right items; each wrong item cancels a right one",
                skin, "small");
        cmdLbl.setColor(CosmicTheme.ASTRAL_CYAN);
        content.add(cmdLbl).width(660f).left().padBottom(14f).row();

        Table extraRow = new Table();
        TextButton prologueBtn = new TextButton("WATCH PROLOGUE", skin, "boon");
        prologueBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                CosmicAudio.playClick();
                dialog.hide();
                game.transitionTo(new PrologueScreen(game));
            }
        });

        TextButton codexBtn = new TextButton("CHARACTER CODEX", skin, "boon");
        codexBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                CosmicAudio.playClick();
                dialog.hide();
                game.transitionTo(new CodexScreen(game));
            }
        });

        final TextButton audioBtn = new TextButton(
                CosmicAudio.isMuted() ? "AUDIO: MUTED" : "AUDIO: ON", skin, "utility");
        audioBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                CosmicAudio.toggleMute();
                CosmicAudio.playClick();
                audioBtn.setText(CosmicAudio.isMuted() ? "AUDIO: MUTED" : "AUDIO: ON");
            }
        });

        TextButton closeBtn = new TextButton("CLOSE", skin, "utility");
        closeBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                CosmicAudio.playClick();
                dialog.hide();
            }
        });

        extraRow.add(prologueBtn).width(185f).height(44f).padRight(10f);
        extraRow.add(codexBtn).width(185f).height(44f).padRight(10f);
        extraRow.add(audioBtn).width(145f).height(44f).padRight(10f);
        extraRow.add(closeBtn).width(120f).height(44f);
        dialog.getButtonTable().add(extraRow);

        dialog.show(stage);
    }

    private void launchSoloBattle(String pilotName, int alienIdx, int level) {
        game.startSoloBattle(stage, pilotName, alienIdx, level,
                message -> showErrorDialog("Cannot Start Solo Battle", message));
    }

    private void launchSquadBattle(int pilotCount, String[] pilotNames, int alienIdx) {
        try {
            Team team = new Team("Squad");
            for (int i = 0; i < pilotCount; i++) {
                team.addPlayer(new Player(pilotNames[i]));
            }
            Entity boss = ALIENS.get(alienIdx).apply(SquadBossBattle.BOSS_LEVEL);
            SquadBossBattle battle = new SquadBossBattle(team, boss, game.createQuestionProvider(), game.getRandom());
            game.launchBattle(stage, battle,
                    () -> new BattleScreen(game, battle, alienIdx, SquadBossBattle.BOSS_LEVEL, true),
                    message -> showErrorDialog("Cannot Start Squad Battle", message));
        } catch (Exception ex) {
            showErrorDialog("Cannot Start Squad Battle", ex.getMessage());
        }
    }

    private void showErrorDialog(String titleText, String message) {
        Skin skin = assets.getSkin();
        Dialog dialog = new Dialog("", skin);
        dialog.setBackground(skin.getDrawable("panel-crimson"));
        dialog.pad(20f);
        dialog.getContentTable().add(new Label(titleText, skin, "gold")).left().padBottom(8f).row();
        Label msg = new Label(message == null ? "Unknown error" : message, skin, "default");
        msg.setWrap(true);
        dialog.getContentTable().add(msg).width(420f).padBottom(12f).row();
        dialog.button("OK", true);
        dialog.show(stage);
    }

    @Override
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
        assets.fontSmall.draw(batch, "PREVIEW  //  STARS: " + game.getTotalStarsEarned(), 1085f, 704f);
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
