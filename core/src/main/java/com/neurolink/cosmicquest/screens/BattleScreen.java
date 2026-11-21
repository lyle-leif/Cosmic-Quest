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

        buildDialogueUi();
        preparePreFightIntro();
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

    private void buildDialogueUi() {
        Skin skin = assets.getSkin();

        Table screenRoot = new Table();
        screenRoot.setFillParent(true);
        stage.addActor(screenRoot);

        // Top Bar (Retreat / Audio toggles)
        Table topBar = new Table();
        topBar.pad(10f, 18f, 0f, 18f);

        TextButton retreatBtn = new TextButton("RETREAT TO PANTHEON", skin, "utility");
        retreatBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                CosmicAudio.playClick();
                game.setScreen(new MainMenuScreen(game));
            }
        });
        topBar.add(retreatBtn).width(210f).height(34f).left();
        topBar.add().expandX();

        final TextButton muteBtn = new TextButton(CosmicAudio.isMuted() ? "AUDIO: MUTED" : "AUDIO: ON", skin, "utility");
        muteBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                CosmicAudio.toggleMute();
                muteBtn.setText(CosmicAudio.isMuted() ? "AUDIO: MUTED" : "AUDIO: ON");
                CosmicAudio.playClick();
            }
        });
        topBar.add(muteBtn).width(140f).height(34f).right();

        screenRoot.add(topBar).growX().top().row();
        screenRoot.add().expandY().row();

        // Bottom Ornate Hades Dialogue & Question Container
        dialogueRoot = new Table();
        dialogueRoot.setBackground(skin.getDrawable("panel-dialogue"));
        dialogueRoot.pad(14f, 26f, 14f, 26f);

        // Row 1: Speaker Nameplate + Active Pilot Turn Badge
        Table headerRow = new Table();
        speakerTitleLabel = new Label("", skin, "gold");
        headerRow.add(speakerTitleLabel).left().padRight(14f);

        turnBadgeLabel = new Label("", skin, "small");
        turnBadgeLabel.setColor(CosmicTheme.ASTRAL_CYAN);
        headerRow.add(turnBadgeLabel).left().expandX();
        dialogueRoot.add(headerRow).width(1148f).padBottom(6f).row();

        // Row 2: Boss Hades Dialogue Quote + Question Prompt (Explicitly constrained to 1148f so text wraps cleanly!)
        bossFlavorLabel = new Label("", skin, "muted");
        bossFlavorLabel.setWrap(true);
        dialogueRoot.add(bossFlavorLabel).width(1148f).left().padBottom(4f).row();

        questionTextLabel = new Label("", skin, "medium");
        questionTextLabel.setWrap(true);
        questionTextLabel.setColor(CosmicTheme.TEXT_WARM);
        dialogueRoot.add(questionTextLabel).width(1148f).left().padBottom(4f).row();

        hintBannerLabel = new Label("", skin, "small");
        hintBannerLabel.setColor(CosmicTheme.EMERALD_HEAL);
        hintBannerLabel.setWrap(true);
        dialogueRoot.add(hintBannerLabel).width(1148f).left().padBottom(6f).row();

        // Row 3: Interactive Clickable Boon Answer Cards / Rune Chips
        optionsTable = new Table();
        dialogueRoot.add(optionsTable).width(1148f).height(142f);

        screenRoot.add(dialogueRoot).width(1204f).height(328f).bottom().padBottom(12f);
    }

    private void preparePreFightIntro() {
        phase = Phase.PRE_FIGHT_INTRO;
        dialogueRoot.setTouchable(Touchable.enabled);
        dialogueRoot.setVisible(true);

        Entity boss = battle.getEntity();
        speakerTitleLabel.setText(boss.getName().toUpperCase() + "  //  INVADER");
        turnBadgeLabel.setText("PRE-BATTLE ENCOUNTER  |  LV." + boss.getLevel());
        refreshHintAndBoons();

        String bossTaunt;
        String cadetReply;
        if (boss instanceof VoidBrute) {
            bossTaunt = "VOID BRUTE: \"The end is nigh, little earthborn! Your fragile colony shall be crushed into dust beneath my gravity!\"";
            cadetReply = "THE CADET: \"Earth fell once. Nova Elysium stands -- let's see if your obsidian armor survives the Codex!\"";
        } else if (boss instanceof StarWeaver) {
            bossTaunt = "STAR WEAVER: \"Struggle all you wish, mortal... Every thread of humanity's fate is already spun, and tonight I cut the cord!\"";
            cadetReply = "THE CADET: \"We didn't cross a galaxy just to get tangled in your web, Oracle!\"";
        } else if (boss instanceof NebulaLich) {
            bossTaunt = "NEBULA LICH: \"The end is nigh! Two worlds could not save your kind from extinction -- kneel before the Undying Sovereign!\"";
            cadetReply = "THE CADET: \"Keep your phylacteries ready, Lich. Nova Elysium belongs to the living!\"";
        } else {
            bossTaunt = "LUNAR BEAST: \"Hear the howl of the final eclipse! Your dome cities will shatter and your bones will feed the crater!\"";
            cadetReply = "THE CADET: \"All bark and cosmic lightning. Bite down on the Stygian Star-Blade!\"";
        }

        bossFlavorLabel.setText(bossTaunt);
        questionTextLabel.setText(cadetReply);
        hintBannerLabel.setColor(CosmicTheme.ASTRAL_CYAN);
        hintBannerLabel.setText("GOVERNMENT MANDATE: Answer the Guardian's trials to channel the NeuroLink Codex into your blade!");

        optionsTable.clearChildren();
        TextButton beginBtn = new TextButton("DRAW STYGIAN STAR-BLADE  --  BEGIN Q&A TRIAL  >>", assets.getSkin());
        beginBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                CosmicAudio.playBoonActivate();
                prepareCurrentTurnQuestion();
            }
        });
        optionsTable.add(beginBtn).width(580f).height(58f).center();
    }

    private void prepareCurrentTurnQuestion() {
        phase = Phase.HADES_DIALOGUE;
        dialogueRoot.setTouchable(Touchable.enabled);
        dialogueRoot.setVisible(true);
        selectedEnumItems.clear();

        Entity boss = battle.getEntity();
        Player current = getActivePlayer();
        Question q = battle.getCurrentQuestion();

        // Apply Void Brute's Gravity Slam penalty if triggered on previous turn
        maxTurnTime = gravitySlamActiveNextTurn
                ? Math.max(8f, BASE_TURN_SECONDS - GRAVITY_SLAM_PENALTY)
                : BASE_TURN_SECONDS;
        remainingTime = maxTurnTime;
        gravitySlamActiveNextTurn = false;

        speakerTitleLabel.setText(boss.getName().toUpperCase());
        int dialogueHeroIdx = getDialogueHeroIndex();
        String weaponTag = getHeroWeaponName(dialogueHeroIdx);
        String playerTag = current != null
                ? current.getName().toUpperCase() + " [" + weaponTag + "]  (STREAK x" + current.getStreak()
                        + (battle.isDoubleDamageNext() ? " | 2X DMG" : "") + ")"
                : "CADET";
        turnBadgeLabel.setText("TURN " + battle.getCurrentTurn() + "  |  " + playerTag);
        bossFlavorLabel.setText(assets.getBossPromptIntro(boss, battle.getCurrentTurn()));

        String promptPrefix;
        if (q instanceof IdentificationQuestion) {
            promptPrefix = "[IDENTIFY THE TERM]  ";
        } else if (q instanceof EnumerationQuestion) {
            promptPrefix = "[SELECT ALL MATCHING RUNES]  ";
        } else if (q instanceof MultipleChoiceQuestion) {
            promptPrefix = "[CHOOSE ONE DECREE]  ";
        } else {
            promptPrefix = "[CHOOSE THE VALID DOCTRINE]  ";
        }
        questionTextLabel.setText(promptPrefix + q.getQuestionText());

        refreshHintAndBoons();
        populateClickableOptions(q);
    }

    private void refreshHintAndBoons() {
        String activeHint = battle.getHint();
        if (activeHint != null && !activeHint.isBlank()) {
            hintBannerLabel.setText("HINT: " + activeHint);
        } else {
            hintBannerLabel.setText("");
        }
    }

    private void activatePowerUpSlot(int slot) {
        Player current = getActivePlayer();
        if (current == null) {
            return;
        }
        try {
            PowerUp used = battle.usePowerUp(current, slot);
            CosmicAudio.playBoonActivate();
            displayedHeroHp = getActiveHeroHp();
            hintBannerLabel.setColor(CosmicTheme.GOLD_BRIGHT);
            hintBannerLabel.setText("BOON INVOKED: " + used.getName().toUpperCase() + " -- " + used.getDescription()
                    + (battle.getHint() != null ? "  |  " + battle.getHint() : ""));
            refreshHintAndBoons();
            // Rebuild options in case Hint hid two Multiple Choice options
            if (battle.getCurrentQuestion() instanceof MultipleChoiceQuestion) {
                populateClickableOptions(battle.getCurrentQuestion());
            }
        } catch (Exception ex) {
            hintBannerLabel.setColor(CosmicTheme.BLOOD_RED);
            hintBannerLabel.setText(ex.getMessage());
        }
    }

    /**
     * Populates 100% clickable Hades Boon Answer Cards inside the dialogue box
     * for whichever question type the current Guardian Boss asks.
     */
    private void populateClickableOptions(Question q) {
        optionsTable.clearChildren();
        Skin skin = assets.getSkin();

        if (q instanceof MultipleChoiceQuestion mc) {
            List<String> opts = mc.getOptions();
            Set<Integer> hidden = battle.getHiddenOptions();
            char[] letters = {'A', 'B', 'C', 'D'};

            for (int i = 0; i < opts.size(); i++) {
                final String letterChoice = String.valueOf(letters[i]);
                String labelText = "[" + letters[i] + "]  " + truncateOption(opts.get(i), 78);
                TextButton card = new TextButton(labelText, skin, "boon");
                card.getLabel().setAlignment(Align.left);
                card.getLabel().setWrap(true);
                card.pad(6f, 14f, 6f, 14f);

                if (hidden.contains(i)) {
                    card.setText("[" + letters[i] + "]  (Eliminated by Oracle Hint)");
                    card.setDisabled(true);
                } else {
                    card.addListener(new ClickListener() {
                        @Override
                        public void clicked(InputEvent event, float x, float y) {
                            if (phase == Phase.HADES_DIALOGUE) {
                                submitPlayerAnswer(letterChoice);
                            }
                        }
                    });
                }
                optionsTable.add(card).width(564f).height(62f).pad(4f);
                if (i % 2 == 1) {
                    optionsTable.row();
                }
            }
        } else if (q instanceof IdentificationQuestion idQ) {
            // Build 4 clickable Term Boon Cards (1 correct term + 3 distractor terms from LessonData)
            List<String> choices = buildIdentificationChoices(idQ.getCorrectAnswer());
            String[] badges = {"I", "II", "III", "IV"};
            for (int i = 0; i < choices.size(); i++) {
                final String chosenTerm = choices.get(i);
                TextButton card = new TextButton("[" + badges[i] + "]  " + chosenTerm, skin, "boon");
                card.getLabel().setAlignment(Align.left);
                card.getLabel().setWrap(true);
                card.pad(8f, 16f, 8f, 16f);
                card.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        if (phase == Phase.HADES_DIALOGUE) {
                            submitPlayerAnswer(chosenTerm);
                        }
                    }
                });
                optionsTable.add(card).width(564f).height(62f).pad(4f);
                if (i % 2 == 1) {
                    optionsTable.row();
                }
            }
        } else if (q instanceof ReasoningQuestion rq) {
            // Build 4 clickable Doctrine Explanation Cards (1 with the full keyword doctrine + 3 distractors)
            List<String> choices = buildReasoningChoices(rq.getCorrectAnswer());
            char[] badges = {'A', 'B', 'C', 'D'};
            for (int i = 0; i < choices.size(); i++) {
                final String chosenExplanation = choices.get(i);
                String display = "[" + badges[i] + "]  " + truncateOption(chosenExplanation, 85);
                TextButton card = new TextButton(display, skin, "boon");
                card.getLabel().setAlignment(Align.left);
                card.getLabel().setWrap(true);
                card.pad(6f, 14f, 6f, 14f);
                card.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        if (phase == Phase.HADES_DIALOGUE) {
                            submitPlayerAnswer(chosenExplanation);
                        }
                    }
                });
                optionsTable.add(card).width(564f).height(62f).pad(4f);
                if (i % 2 == 1) {
                    optionsTable.row();
                }
            }
        } else if (q instanceof EnumerationQuestion eq) {
            // Build clickable Rune Toggle Chips + [WEAVE & STRIKE] submit button!
            List<String> chips = buildEnumerationChips(eq.getItems());
            Table chipsGrid = new Table();
            int cols = 4;
            for (int i = 0; i < chips.size(); i++) {
                final String itemText = chips.get(i);
                final TextButton chipBtn = new TextButton(itemText, skin, "boon");
                chipBtn.pad(4f, 8f, 4f, 8f);
                chipBtn.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        CosmicAudio.playClick();
                        if (selectedEnumItems.contains(itemText)) {
                            selectedEnumItems.remove(itemText);
                            chipBtn.setChecked(false);
                        } else {
                            selectedEnumItems.add(itemText);
                            chipBtn.setChecked(true);
                        }
                    }
                });
                chipsGrid.add(chipBtn).width(228f).height(42f).pad(3f);
                if ((i + 1) % cols == 0) {
                    chipsGrid.row();
                }
            }
            optionsTable.add(chipsGrid).width(950f).padRight(10f);

            TextButton strikeBtn = new TextButton("WEAVE &\nSTRIKE!", skin);
            strikeBtn.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    if (phase == Phase.HADES_DIALOGUE) {
                        String joined = String.join(", ", selectedEnumItems);
                        submitPlayerAnswer(joined);
                    }
                }
            });
            optionsTable.add(strikeBtn).width(175f).height(90f).pad(3f);
        }
    }

    private List<String> buildIdentificationChoices(String correctTerm) {
        Set<String> pool = new LinkedHashSet<>();
        pool.add(correctTerm);

        List<Term> allTerms = new ArrayList<>(game.getLessonData().getTerms());
        Collections.shuffle(allTerms, uiRandom);
        for (Term t : allTerms) {
            if (pool.size() >= 4) {
                break;
            }
            if (!t.getTerm().equalsIgnoreCase(correctTerm)) {
                pool.add(t.getTerm());
            }
        }
        String[] fallbacks = {"Encapsulation", "Polymorphism", "Inheritance", "Abstraction", "Constructor"};
        for (String fb : fallbacks) {
            if (pool.size() >= 4) {
                break;
            }
            if (!fb.equalsIgnoreCase(correctTerm)) {
                pool.add(fb);
            }
        }
        List<String> list = new ArrayList<>(pool);
        Collections.shuffle(list, uiRandom);
        return list;
    }

    private List<String> buildReasoningChoices(String correctDefinition) {
        Set<String> pool = new LinkedHashSet<>();
        pool.add(correctDefinition);

        List<Term> allTerms = new ArrayList<>(game.getLessonData().getTerms());
        Collections.shuffle(allTerms, uiRandom);
        for (Term t : allTerms) {
            if (pool.size() >= 4) {
                break;
            }
            if (!t.getDefinition().equalsIgnoreCase(correctDefinition)) {
                pool.add(t.getDefinition());
            }
        }
        String[] fallbacks = {
            "Converts source code directly into machine-level bytecode without creating objects.",
            "Allocates static memory addresses at compile time while forbidding dynamic dispatch.",
            "Terminates all running threads immediately whenever a constructor is invoked."
        };
        for (String fb : fallbacks) {
            if (pool.size() >= 4) {
                break;
            }
            pool.add(fb);
        }
        List<String> list = new ArrayList<>(pool);
        Collections.shuffle(list, uiRandom);
        return list;
    }

    private List<String> buildEnumerationChips(List<String> requiredItems) {
        Set<String> pool = new LinkedHashSet<>(requiredItems);
        int targetCount = Math.max(8, requiredItems.size() + 3);

        for (List<String> list : game.getLessonData().getEnumerations().values()) {
            for (String item : list) {
                if (pool.size() >= targetCount) {
                    break;
                }
                pool.add(item);
            }
        }
        for (Term t : game.getLessonData().getTerms()) {
            if (pool.size() >= targetCount) {
                break;
            }
            pool.add(t.getTerm());
        }
        List<String> result = new ArrayList<>(pool);
        Collections.shuffle(result, uiRandom);
        return result;
    }

    private String truncateOption(String text, int maxLen) {
        if (text == null || text.length() <= maxLen) {
            return text;
        }
        return text.substring(0, maxLen - 3) + "...";
    }

    private void submitPlayerAnswer(String answer) {
        if (phase != Phase.HADES_DIALOGUE) {
            return;
        }
        this.actingHeroIndex = getDialogueHeroIndex();
        TurnResult result = battle.submitAnswer(answer);
        beginPixelCombatCutscene(result);
    }

    private void triggerTimeout() {
        if (phase != Phase.HADES_DIALOGUE) {
            return;
        }
        this.actingHeroIndex = getDialogueHeroIndex();
        TurnResult result = battle.submitTimeout();
        beginPixelCombatCutscene(result);
    }

    /**
     * Switches from the Detailed Hades Dialogue View to the 2D Pixel Battle Stage
     * and orchestrates the Cadet and Boss pixel attack animations!
     */
    private void beginPixelCombatCutscene(TurnResult result) {
        this.lastTurnResult = result;
        this.phase = Phase.PIXEL_CUTSCENE;
        this.cutsceneTimer = 0f;
        this.playerStrikeSoundPlayed = false;
        this.bossStrikeSoundPlayed = false;

        // If partial enumeration dealt damage AND missed, play both strikes sequentially
        boolean bothAct = result.getDamageDealt() > 0 && !result.isCorrect();
        this.cutsceneDuration = bothAct ? 2.35f : 1.70f;

        dialogueRoot.setTouchable(Touchable.disabled);

        // Remember if Void Brute used Gravity Slam on a miss so next turn's timer shrinks
        if (!result.isCorrect() && battle.getEntity() instanceof VoidBrute) {
            gravitySlamActiveNextTurn = true;
        }
    }

    private void spawnSparkBurst(float cx, float cy, Color mainColor, Color altColor, int count) {
        for (int i = 0; i < count; i++) {
            Spark s = new Spark();
            float angle = MathUtils.random(0f, MathUtils.PI2);
            float speed = MathUtils.random(70f, 340f);
            s.x = cx + MathUtils.random(-18f, 18f);
            s.y = cy + MathUtils.random(-18f, 18f);
            s.vx = MathUtils.cos(angle) * speed;
            s.vy = MathUtils.sin(angle) * speed;
            s.size = MathUtils.random(3.5f, 8.5f);
            s.maxLife = MathUtils.random(0.35f, 0.75f);
            s.life = s.maxLife;
            s.color = (i % 2 == 0) ? mainColor : altColor;
            sparks.add(s);
        }
    }

    private void addFloatingText(String text, float x, float y, Color color, boolean large) {
        FloatingText ft = new FloatingText();
        ft.text = text;
        ft.x = x;
        ft.y = y;
        ft.vy = large ? 58f : 42f;
        ft.maxLife = 1.25f;
        ft.life = ft.maxLife;
        ft.color = color;
        ft.large = large;
        floatingTexts.add(ft);
    }

    @Override
    public void render(float delta) {
        animTime += delta;
        updateBattleLogic(delta);

        ScreenUtils.clear(CosmicTheme.BG_OBSIDIAN);
        stage.getViewport().apply();

        SpriteBatch batch = game.getBatch();
        ShapeRenderer shapes = game.getShapes();

        // Screen shake offset during impacts
        float shakeX = 0f;
        float shakeY = 0f;
        if (screenShakeTime > 0f) {
            screenShakeTime -= delta;
            shakeX = MathUtils.random(-screenShakeIntensity, screenShakeIntensity);
            shakeY = MathUtils.random(-screenShakeIntensity, screenShakeIntensity);
        }

        stage.getCamera().position.set(
            CosmicTheme.VIRTUAL_WIDTH * 0.5f + shakeX,
            CosmicTheme.VIRTUAL_HEIGHT * 0.5f + shakeY,
            0f
        );
        stage.getCamera().update();
        batch.setProjectionMatrix(stage.getCamera().combined);
        shapes.setProjectionMatrix(stage.getCamera().combined);

        // 1. Always render the 2D Pixel Battle Stage (Pokemon-style arena + 24-bit pixel characters + HUD)
        render2DPixelBattleArena(batch, shapes, delta);

        // 2. Render the Detailed Hades Dialogue Layer (Bust Portraits + Ornate Q&A Box) based on dialogueSlide
        renderHadesDialogueOverlay(batch, shapes);

        // Reset camera center for UI Stage
        stage.getCamera().position.set(CosmicTheme.VIRTUAL_WIDTH * 0.5f, CosmicTheme.VIRTUAL_HEIGHT * 0.5f, 0f);
        stage.getCamera().update();

        dialogueRoot.setColor(1f, 1f, 1f, dialogueSlide);
        dialogueRoot.setVisible(dialogueSlide > 0.02f);

        stage.act(delta);
        stage.draw();
    }

    private void updateBattleLogic(float delta) {
        // Smoothly interpolate displayed HP bars
        displayedHeroHp = MathUtils.lerp(displayedHeroHp, getActiveHeroHp(), Math.min(1f, delta * 8f));
        displayedBossHp = MathUtils.lerp(displayedBossHp, battle.getEntity().getHp(), Math.min(1f, delta * 8f));

        // Update particles & floating combat callouts
        for (int i = sparks.size() - 1; i >= 0; i--) {
            Spark s = sparks.get(i);
            s.x += s.vx * delta;
            s.y += s.vy * delta;
            s.vy -= 220f * delta;
            s.life -= delta;
            if (s.life <= 0f) {
                sparks.remove(i);
            }
        }
        for (int i = floatingTexts.size() - 1; i >= 0; i--) {
            FloatingText ft = floatingTexts.get(i);
            ft.y += ft.vy * delta;
            ft.life -= delta;
            if (ft.life <= 0f) {
                floatingTexts.remove(i);
            }
        }

        if (phase == Phase.PRE_FIGHT_INTRO) {
            dialogueSlide = MathUtils.lerp(dialogueSlide, 1f, Math.min(1f, delta * 10f));
        } else if (phase == Phase.HADES_DIALOGUE) {
            // Slide Hades portraits & dialogue box IN
            dialogueSlide = MathUtils.lerp(dialogueSlide, 1f, Math.min(1f, delta * 10f));

            remainingTime -= delta;
            if (remainingTime <= 0f) {
                remainingTime = 0f;
                triggerTimeout();
            }
        } else {
            // Slide Hades portraits & dialogue box OUT so the 2D Pixel Battle Stage takes center stage
            dialogueSlide = MathUtils.lerp(dialogueSlide, 0f, Math.min(1f, delta * 12f));
            cutsceneTimer += delta;

            triggerCutsceneImpactEvents();

            if (cutsceneTimer >= cutsceneDuration) {
                finishCutsceneAndContinue();
            }
        }
    }

    private void triggerCutsceneImpactEvents() {
        if (lastTurnResult == null) {
            return;
        }
        Entity boss = battle.getEntity();
        boolean hasPlayerAttack = lastTurnResult.getDamageDealt() > 0;
        boolean hasBossAttack = !lastTurnResult.isCorrect();

        // Player Strike Impact at t = 0.42s
        if (hasPlayerAttack && !playerStrikeSoundPlayed && cutsceneTimer >= 0.42f) {
            playerStrikeSoundPlayed = true;
            CosmicAudio.playPlayerSlash();
            screenShakeTime = 0.28f;
            screenShakeIntensity = 9f;

            spawnSparkBurst(BOSS_BASE_X + 95f, BOSS_BASE_Y + 95f,
                    CosmicTheme.ASTRAL_CYAN, CosmicTheme.GOLD_BRIGHT, 36);
            addFloatingText("-" + lastTurnResult.getDamageDealt() + " DMG!",
                    BOSS_BASE_X + 35f, BOSS_BASE_Y + 185f, CosmicTheme.GOLD_BRIGHT, true);
        }

        // Boss Strike Impact (at t = 0.45s on pure miss, or t = 1.25s after partial enumeration hit)
        float bossImpactTime = hasPlayerAttack ? 1.25f : 0.45f;
        if (hasBossAttack && !bossStrikeSoundPlayed && cutsceneTimer >= bossImpactTime) {
            bossStrikeSoundPlayed = true;
            CosmicAudio.playBossHit();
            screenShakeTime = 0.36f;
            screenShakeIntensity = 13f;

            Color bossCol = assets.getBossAccentColor(boss);
            spawnSparkBurst(HERO_BASE_X + 80f, HERO_BASE_Y + 80f,
                    bossCol, CosmicTheme.ZAGREUS_CRIMSON, 38);

            if (lastTurnResult.getDamageTaken() > 0) {
                String tag = lastTurnResult.isTimedOut() ? "TIME OUT! -" : "-";
                addFloatingText(tag + lastTurnResult.getDamageTaken() + " HULL!",
                        HERO_BASE_X + 10f, HERO_BASE_Y + 180f, CosmicTheme.BLOOD_RED, true);
            } else {
                addFloatingText("AEGIS SHIELD BLOCKED!",
                        HERO_BASE_X - 10f, HERO_BASE_Y + 180f, CosmicTheme.ASTRAL_CYAN, true);
            }

            addFloatingText(boss.getSpecialMove().toUpperCase() + "!",
                    BOSS_BASE_X - 10f, BOSS_BASE_Y + 205f, bossCol, false);
        }
    }

    private void finishCutsceneAndContinue() {
        if (!battle.isOver()) { battle.nextTurn(); prepareCurrentTurnQuestion(); }
    }

    private void render2DPixelBattleArena(SpriteBatch batch, ShapeRenderer shapes, float delta) {
        Entity boss = battle.getEntity();
        Texture backdrop = assets.getBossBackdrop(boss);
        Color bossAccent = assets.getBossAccentColor(boss);

        // Compute character offsets for idle breathing + 2D pixel attack animations
        float idleBob = MathUtils.sin(animTime * 3.4f) * 5f;
        float heroOffsetX = 0f;
        float heroOffsetY = idleBob;
        float bossOffsetX = 0f;
        float bossOffsetY = -idleBob * 0.8f;
        boolean heroHurtSprite = false;
        boolean heroAttackingPose = false;
        boolean bossAttackingPose = false;
        boolean heroFlashCrimson = false;
        boolean bossFlashWhite = false;
        float slashProgress = -1f;
        float bossBeamProgress = -1f;

        int heroIdx = getDisplayedHeroIndex();

        if (phase == Phase.PIXEL_CUTSCENE && lastTurnResult != null) {
            boolean hasPlayerAttack = lastTurnResult.getDamageDealt() > 0;
            boolean hasBossAttack = !lastTurnResult.isCorrect();

            if (hasPlayerAttack && cutsceneTimer < 1.05f) {
                float t = MathUtils.clamp(cutsceneTimer / 0.95f, 0f, 1f);
                heroAttackingPose = (cutsceneTimer >= 0.06f && cutsceneTimer <= 0.88f);
                if (heroIdx == 1) {
                    // Gunner (Star-Gun): Steps into sharpshooter firing stance and recoils as emerald plasma rounds fire!
                    if (t < 0.35f) {
                        float p = Interpolation.pow2Out.apply(t / 0.35f);
                        heroOffsetX = p * 95f;
                        heroOffsetY = p * 14f;
                    } else {
                        float p = Interpolation.pow2In.apply((t - 0.35f) / 0.65f);
                        float recoil = MathUtils.sin(t * 28f) * 12f;
                        heroOffsetX = (1f - p) * 95f - Math.abs(recoil);
                        heroOffsetY = (1f - p) * 14f;
                    }
                } else if (heroIdx == 2) {
                    // Lancer (Astral Spear): High valkyrie leap and long-reach spear thrust!
                    if (t < 0.45f) {
                        float p = Interpolation.pow2Out.apply(t / 0.45f);
                        heroOffsetX = p * 455f;
                        heroOffsetY = p * 82f + MathUtils.sin(p * MathUtils.PI) * 58f;
                    } else {
                        float p = Interpolation.pow2In.apply((t - 0.45f) / 0.55f);
                        heroOffsetX = (1f - p) * 455f;
                        heroOffsetY = (1f - p) * 82f;
                    }
                } else {
                    // Vanguard (Stygian Sword): Dash lunge toward Boss platform and slash!
                    if (t < 0.45f) {
                        float p = Interpolation.pow2Out.apply(t / 0.45f);
                        heroOffsetX = p * 440f;
                        heroOffsetY = p * 70f + MathUtils.sin(p * MathUtils.PI) * 38f;
                    } else {
                        float p = Interpolation.pow2In.apply((t - 0.45f) / 0.55f);
                        heroOffsetX = (1f - p) * 440f;
                        heroOffsetY = (1f - p) * 70f;
                    }
                }
                if (cutsceneTimer >= 0.22f && cutsceneTimer <= 0.80f) {
                    slashProgress = (cutsceneTimer - 0.22f) / 0.58f;
                }
                if (cutsceneTimer >= 0.40f && cutsceneTimer <= 0.76f) {
                    bossFlashWhite = true;
                    bossOffsetX = MathUtils.sin((cutsceneTimer - 0.40f) * 38f) * 26f + 22f;
                }
            }

            float bossStart = hasPlayerAttack ? 1.0f : 0.05f;
            if (hasBossAttack && cutsceneTimer >= bossStart) {
                float local = cutsceneTimer - bossStart;
                float t = MathUtils.clamp(local / 0.95f, 0f, 1f);
                bossAttackingPose = (local >= 0.06f && local <= 0.88f);
                if (t < 0.42f) {
                    float p = Interpolation.pow2Out.apply(t / 0.42f);
                    bossOffsetX = -p * 420f;
                    bossOffsetY = -p * 65f;
                } else {
                    float p = Interpolation.pow2In.apply((t - 0.42f) / 0.58f);
                    bossOffsetX = -(1f - p) * 420f;
                    bossOffsetY = -(1f - p) * 65f;
                }
                if (local >= 0.25f && local <= 0.78f) {
                    bossBeamProgress = (local - 0.25f) / 0.53f;
                }
                if (local >= 0.38f && local <= 0.85f) {
                    heroHurtSprite = lastTurnResult.getDamageTaken() > 0;
                    heroFlashCrimson = lastTurnResult.getDamageTaken() > 0;
                    heroOffsetX = -MathUtils.sin((local - 0.38f) * 35f) * 22f - 18f;
                }
            }
        }

        // Draw Cosmic Backdrop
        batch.begin();
        batch.setColor(Color.WHITE);
        batch.draw(backdrop, 0, 0, CosmicTheme.VIRTUAL_WIDTH, CosmicTheme.VIRTUAL_HEIGHT);
        batch.end();

        // Draw Pokemon-style 2D Arena Platforms
        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapes.begin(ShapeRenderer.ShapeType.Filled);

        // Cadet Arena Pedestal (Bottom-Left)
        shapes.setColor(0.06f, 0.05f, 0.12f, 0.82f);
        shapes.ellipse(HERO_BASE_X - 35f, HERO_BASE_Y - 28f, 250f, 68f);
        shapes.setColor(CosmicTheme.ASTRAL_CYAN.r, CosmicTheme.ASTRAL_CYAN.g, CosmicTheme.ASTRAL_CYAN.b, 0.38f);
        shapes.ellipse(HERO_BASE_X - 20f, HERO_BASE_Y - 20f, 220f, 52f);

        // Boss Arena Pedestal (Top-Right)
        shapes.setColor(0.08f, 0.04f, 0.12f, 0.84f);
        shapes.ellipse(BOSS_BASE_X - 30f, BOSS_BASE_Y - 26f, 265f, 72f);
        shapes.setColor(bossAccent.r, bossAccent.g, bossAccent.b, 0.42f);
        shapes.ellipse(BOSS_BASE_X - 15f, BOSS_BASE_Y - 18f, 235f, 54f);
        shapes.end();

        // Draw 16-Bit Full-Body Pixel Characters (Switches between Idle Stance and Dynamic Attack Stance!)
        batch.begin();
        Texture cadetTex = assets.getCadetPixelSprite(heroIdx, heroHurtSprite, heroAttackingPose);
        Texture bossTex = assets.getBossPixelSprite(boss, bossAttackingPose);

        float hx = HERO_BASE_X + heroOffsetX;
        float hy = HERO_BASE_Y + heroOffsetY;
        float bx = BOSS_BASE_X + bossOffsetX;
        float by = BOSS_BASE_Y + bossOffsetY;

        if (heroFlashCrimson) {
            batch.setColor(1f, 0.35f, 0.45f, 1f);
        } else {
            batch.setColor(Color.WHITE);
        }
        batch.draw(cadetTex, hx, hy, 206f, 184f);

        if (bossFlashWhite) {
            batch.setColor(1f, 0.45f, 0.45f, 1f);
        } else {
            batch.setColor(Color.WHITE);
        }
        batch.draw(bossTex, bx, by, 210f, 200f);
        batch.setColor(Color.WHITE);
        batch.end();

        // Draw Combat Projectiles, Beams, and Particle Sparks ON TOP of Character Sprites
        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapes.begin(ShapeRenderer.ShapeType.Filled);

        // Player Attack Effect (Sword Slash / Gun Plasma Volley / Spear Lance Thrust)
        if (slashProgress >= 0f) {
            float sx = bx + 95f;
            float sy = by + 115f;
            // Exact pixel coordinate of the Star-Gun muzzle in hero_cadet_gunner_attack.png (pixel 84,33 on 116x100 -> +149f, +123.3f on 206x184)
            float muzzleX = hx + 149f;
            float muzzleY = hy + 123.3f;

            if (heroIdx == 1) {
                // Star-Gun: Muzzle glow + Emerald-Cyan Rail Beam + 3 Traveling Plasma Rounds aligned straight from the gun barrel!
                shapes.setColor(0.85f, 1f, 0.95f, 0.95f * (1f - slashProgress * 0.4f));
                shapes.circle(muzzleX, muzzleY, 10f);
                shapes.setColor(0.18f, 0.98f, 0.65f, 0.90f * (1f - slashProgress * 0.5f));
                shapes.rectLine(muzzleX, muzzleY, sx, sy, 10f);
                shapes.setColor(0.85f, 1f, 0.95f, 1f - slashProgress * 0.5f);
                shapes.rectLine(muzzleX, muzzleY, sx, sy, 4f);
                for (int b = 0; b < 3; b++) {
                    float bt = MathUtils.clamp((slashProgress * 1.45f) - b * 0.20f, 0f, 1f);
                    float px = MathUtils.lerp(muzzleX, sx, bt);
                    float py = MathUtils.lerp(muzzleY, sy, bt);
                    shapes.setColor(0.20f, 1f, 0.70f, 1f);
                    shapes.circle(px, py, 11f);
                    shapes.setColor(0.90f, 1f, 0.96f, 1f);
                    shapes.circle(px, py, 5.5f);
                }
                shapes.setColor(0.25f, 1f, 0.72f, 1f - slashProgress);
                shapes.circle(sx, sy, 42f + slashProgress * 68f);
            } else if (heroIdx == 2) {
                // Astral Spear: Violet-Gold Piercing Lance Thrust aligned with spearhead (hx + 178f, hy + 98f)
                float spearTipX = hx + 178f;
                float spearTipY = hy + 98f;
                shapes.setColor(0.86f, 0.36f, 1f, 0.92f * (1f - slashProgress * 0.6f));
                shapes.rectLine(spearTipX - 35f, spearTipY, sx + 45f, sy, 15f);
                shapes.setColor(CosmicTheme.GOLD_BRIGHT.r, CosmicTheme.GOLD_BRIGHT.g, CosmicTheme.GOLD_BRIGHT.b, 1f - slashProgress);
                shapes.circle(sx, sy, 55f + slashProgress * 85f);
            } else {
                // Stygian Sword: Cyan-Gold Crescent Slash aligned with sword blade (hx + 160f, hy + 96f)
                float radius = 55f + slashProgress * 85f;
                shapes.setColor(CosmicTheme.ASTRAL_CYAN.r, CosmicTheme.ASTRAL_CYAN.g, CosmicTheme.ASTRAL_CYAN.b, 1f - slashProgress);
                shapes.circle(sx, sy, radius);
                shapes.setColor(CosmicTheme.GOLD_BRIGHT.r, CosmicTheme.GOLD_BRIGHT.g, CosmicTheme.GOLD_BRIGHT.b, 1f - slashProgress * 0.8f);
                shapes.rectLine(hx + 145f, hy + 96f, sx + 40f, sy + 30f, 10f);
            }
        }

        // Boss Special Attack Shockwave / Beam Effect
        if (bossBeamProgress >= 0f) {
            float bossMuzzleX = bx + 45f;
            float bossMuzzleY = by + 95f;
            float heroHitX = hx + 85f;
            float heroHitY = hy + 92f;
            shapes.setColor(bossAccent.r, bossAccent.g, bossAccent.b, 0.85f * (1f - bossBeamProgress * 0.6f));
            shapes.rectLine(bossMuzzleX, bossMuzzleY, heroHitX, heroHitY, 16f);
            shapes.setColor(1f, 0.9f, 0.95f, 0.9f * (1f - bossBeamProgress));
            shapes.circle(heroHitX, heroHitY, 45f + bossBeamProgress * 70f);
        }

        // Particle Sparks
        for (Spark s : sparks) {
            float alpha = MathUtils.clamp(s.life / s.maxLife, 0f, 1f);
            shapes.setColor(s.color.r, s.color.g, s.color.b, alpha);
            shapes.rect(s.x - s.size * 0.5f, s.y - s.size * 0.5f, s.size, s.size);
        }
        shapes.end();

        // If Cadet has active Shield turns, draw glowing Aegis bubble around the pixel Cadet
        Player activePlayer = getActivePlayer();
        if (activePlayer != null && activePlayer.getShieldTurns() > 0) {
            Gdx.gl.glEnable(GL20.GL_BLEND);
            shapes.begin(ShapeRenderer.ShapeType.Line);
            shapes.setColor(CosmicTheme.ASTRAL_CYAN);
            shapes.circle(hx + 86f, hy + 86f, 96f + MathUtils.sin(animTime * 6f) * 4f);
            shapes.end();
        }

        // Draw Floating Combat Callout Numbers
        batch.begin();
        for (FloatingText ft : floatingTexts) {
            float alpha = MathUtils.clamp(ft.life / ft.maxLife, 0f, 1f);
            if (ft.large) {
                assets.fontLarge.setColor(ft.color.r, ft.color.g, ft.color.b, alpha);
                assets.fontLarge.draw(batch, ft.text, ft.x, ft.y);
            } else {
                assets.fontMedium.setColor(ft.color.r, ft.color.g, ft.color.b, alpha);
                assets.fontMedium.draw(batch, ft.text, ft.x, ft.y);
            }
        }
        assets.fontLarge.setColor(Color.WHITE);
        assets.fontMedium.setColor(Color.WHITE);
        batch.end();

        // Draw Pokemon-style Top HUD Status Cards (Cadet HP Card on Left, Boss HP Card on Right, Timer in Center)
        renderTopBattleHud(batch, shapes);
    }
    private void renderTopBattleHud(SpriteBatch batch, ShapeRenderer shapes) {}
    private void renderHadesDialogueOverlay(SpriteBatch batch, ShapeRenderer shapes) {}

    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}
