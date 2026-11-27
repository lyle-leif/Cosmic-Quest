package com.neurolink.cosmicquest.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.neurolink.cosmicquest.model.Entity;
import com.neurolink.cosmicquest.model.EnumerationQuestion;
import com.neurolink.cosmicquest.model.LunarBeast;
import com.neurolink.cosmicquest.model.MultipleChoiceQuestion;
import com.neurolink.cosmicquest.model.NebulaLich;
import com.neurolink.cosmicquest.model.Player;
import com.neurolink.cosmicquest.model.Question;
import com.neurolink.cosmicquest.model.StarWeaver;
import com.neurolink.cosmicquest.model.VoidBrute;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.junit.jupiter.api.Test;

class SoloBossBattleTest {

    private static final long SEED = 42;
    private static final String WRONG = "definitely not the answer";

    private final Player player = new Player("Lyle");

    private static LessonData lesson() {
        return new LessonData(List.of(
                new Term("Class", "A blueprint that describes the fields and methods of an object."),
                new Term("Object", "An instance of a class that contains data and can perform actions."),
                new Term("Method", "A block of code that performs a specific task."),
                new Term("Field", "A variable that stores data belonging to an object.")),
                Map.of("Pillars of OOP", List.of("Encapsulation", "Inheritance", "Polymorphism", "Abstraction")));
    }

    private SoloBossBattle startBattle(Entity entity) {
        SoloBossBattle battle = new SoloBossBattle(player, entity,
                new QuestionGenerator(lesson(), new Random(SEED)), new Random(SEED));
        battle.startBattle();
        return battle;
    }

    private static TurnResult answerRight(SoloBossBattle battle) {
        return battle.submitAnswer(battle.getCurrentQuestion().getCorrectAnswer());
    }

    @Test
    void rightAnswerHitsTheAlien() {
        SoloBossBattle battle = startBattle(new VoidBrute(3));
        int difficulty = battle.getCurrentQuestion().getDifficulty();

        TurnResult result = answerRight(battle);

        int expected = DamageCalculator.playerDamage(difficulty, 1);
        assertTrue(result.isCorrect());
        assertEquals(expected, result.getDamageDealt());
        assertEquals(0, result.getDamageTaken());
        assertEquals(160 - expected, battle.getEntity().getHp());
        assertEquals(BattleState.PLAYER_ATTACK, battle.getState());
    }

    @Test
    void streakGrowsBeforeDamageIsWorkedOut() {
        // Decision D8: the first right answer already counts as streak 1.
        SoloBossBattle battle = startBattle(new VoidBrute(3));
        int difficulty = battle.getCurrentQuestion().getDifficulty();
        TurnResult result = answerRight(battle);
        assertEquals(1, player.getStreak());
        assertEquals(DamageCalculator.playerDamage(difficulty, 1), result.getDamageDealt());
    }

    @Test
    void streakBuildsAcrossTurnsAndRaisesDamage() {
        SoloBossBattle battle = startBattle(new VoidBrute(3));
        answerRight(battle);
        battle.nextTurn();
        answerRight(battle);
        battle.nextTurn();
        int difficulty = battle.getCurrentQuestion().getDifficulty();
        TurnResult third = answerRight(battle);
        assertEquals(3, player.getStreak());
        assertEquals(DamageCalculator.playerDamage(difficulty, 3), third.getDamageDealt());
    }

    @Test
    void scoreIsTotalDamageDealt() {
        SoloBossBattle battle = startBattle(new VoidBrute(3));
        int first = answerRight(battle).getDamageDealt();
        battle.nextTurn();
        int second = answerRight(battle).getDamageDealt();
        assertEquals(first + second, player.getScore());
    }

    @Test
    void wrongAnswerLetsTheAlienStrike() {
        SoloBossBattle battle = startBattle(new VoidBrute(1));
        player.increaseStreak();
        Question question = battle.getCurrentQuestion();

        TurnResult result = battle.submitAnswer(WRONG);

        assertFalse(result.isCorrect());
        assertEquals(0, result.getDamageDealt());
        assertEquals(8, result.getDamageTaken());
        assertEquals(92, player.getHp());
        assertEquals(0, player.getStreak());
        assertEquals(List.of(question), battle.getMissedQuestions());
        assertEquals(BattleState.ENEMY_ATTACK, battle.getState());
    }

    @Test
    void timeoutCountsAsAMiss() {
        SoloBossBattle battle = startBattle(new StarWeaver(1));
        TurnResult result = battle.submitTimeout();
        assertTrue(result.isTimedOut());
        assertEquals("", result.getPlayerAnswer());
        assertEquals(10, result.getDamageTaken());
        assertEquals(1, battle.getMissedQuestions().size());
    }

    @Test
    void groundSlamLandsOnTheThirdTurn() {
        SoloBossBattle battle = startBattle(new VoidBrute(1));
        assertEquals(8, battle.submitAnswer(WRONG).getDamageTaken());
        battle.nextTurn();
        assertEquals(8, battle.submitAnswer(WRONG).getDamageTaken());
        battle.nextTurn();
        assertEquals(12, battle.submitAnswer(WRONG).getDamageTaken());
    }

    @Test
    void partialListDealsDamageButStillCountsAsAMiss() {
        SoloBossBattle battle = startBattle(new LunarBeast(1));
        player.increaseStreak();
        List<String> items = ((EnumerationQuestion) battle.getCurrentQuestion()).getItems();

        TurnResult result = battle.submitAnswer(String.join(", ", items.subList(0, 3)));

        assertFalse(result.isCorrect());
        assertEquals(0.75, result.getMatchRatio(), 0.0001);
        assertTrue(result.getDamageDealt() > 0);
        assertEquals(11 + 2, result.getDamageTaken(), "Moon Pounce: +2 for the 1 item missed");
        assertEquals(0, player.getStreak());
        assertEquals(1, battle.getMissedQuestions().size());
        assertEquals(BattleState.ENEMY_ATTACK, battle.getState());
    }

    @Test
    void moonPounceOnATimeoutCountsEveryItem() {
        SoloBossBattle battle = startBattle(new LunarBeast(1));
        assertEquals(11 + 4 * 2, battle.submitTimeout().getDamageTaken());
    }

    @Test
    void finishingBlowStopsTheAlienFromStrikingBack() {
        // Decision D4: a partial hit that defeats the alien wins outright.
        SoloBossBattle battle = startBattle(new LunarBeast(1));
        battle.getEntity().takeDamage(79);
        List<String> items = ((EnumerationQuestion) battle.getCurrentQuestion()).getItems();

        TurnResult result = battle.submitAnswer(items.get(0));

        assertEquals(BattleState.WON, result.getNewState());
        assertEquals(0, result.getDamageTaken());
        assertEquals(100, player.getHp());
    }

    @Test
    void defeatingTheAlienWins() {
        SoloBossBattle battle = startBattle(new VoidBrute(1));
        while (!battle.isOver()) {
            answerRight(battle);
            if (!battle.isOver()) {
                battle.nextTurn();
            }
        }
        assertEquals(BattleState.WON, battle.getState());
        assertTrue(battle.getEntity().isDefeated());
        assertEquals(100, player.getHp());
        assertTrue(battle.getMissedQuestions().isEmpty());
    }

    @Test
    void reachingZeroHullLoses() {
        SoloBossBattle battle = startBattle(new VoidBrute(1));
        player.takeDamage(95);
        TurnResult result = battle.submitAnswer(WRONG);
        assertEquals(BattleState.LOST, result.getNewState());
        assertTrue(battle.isOver());
        assertThrows(IllegalStateException.class, battle::nextTurn);
    }

    @Test
    void shieldBlocksTheStrike() {
        SoloBossBattle battle = startBattle(new VoidBrute(1));
        player.activateShield(1);
        TurnResult result = battle.submitAnswer(WRONG);
        assertEquals(0, result.getDamageTaken());
        assertEquals(100, player.getHp());
    }

    @Test
    void doubleDamageAppliesToTheNextHit() {
        SoloBossBattle battle = startBattle(new VoidBrute(3));
        int difficulty = battle.getCurrentQuestion().getDifficulty();
        battle.doubleDamageNext = true;
        TurnResult result = answerRight(battle);
        assertEquals(2 * DamageCalculator.playerDamage(difficulty, 1), result.getDamageDealt());
        assertFalse(battle.isDoubleDamageNext());
    }

    @Test
    void missedQuestionComesBackLater() {
        SoloBossBattle battle = startBattle(new VoidBrute(3));
        Question missed = battle.getCurrentQuestion();
        battle.submitAnswer(WRONG);
        boolean cameBack = false;
        for (int turn = 0; turn < BattleMode.QUESTIONS_PER_REFILL + 1 && !cameBack; turn++) {
            battle.nextTurn();
            cameBack = battle.getCurrentQuestion() == missed;
            if (!cameBack) {
                player.heal(100);
                battle.submitTimeout();
            }
        }
        assertTrue(cameBack);
    }

    @Test
    void rejectsNullPlayer() {
        QuestionGenerator generator = new QuestionGenerator(lesson());
        assertThrows(IllegalArgumentException.class,
                () -> new SoloBossBattle(null, new VoidBrute(1), generator));
    }

    @Test
    void perfectWinIsTrackedAndEarnsThreeStars() {
        SoloBossBattle battle = startBattle(new VoidBrute(3));
        int turns = 0;
        while (true) {
            answerRight(battle);
            turns++;
            if (battle.isOver()) {
                break;
            }
            battle.nextTurn();
        }
        MatchStats stats = battle.getStats();
        assertEquals(BattleState.WON, battle.getState());
        assertEquals(turns, stats.getTotalAnswers());
        assertEquals(turns, stats.getCorrectAnswers());
        assertEquals(player.getScore(), stats.getDamageDealt(), "Score is total damage dealt");
        assertEquals(0, stats.getDamageTaken());
        assertTrue(stats.getMisses().isEmpty());
        assertEquals(3, battle.getStars());
    }

    @Test
    void lossIsTrackedAndEarnsNoStars() {
        SoloBossBattle battle = startBattle(new VoidBrute(3));
        Question first = battle.getCurrentQuestion();
        while (true) {
            battle.submitAnswer(WRONG);
            if (battle.isOver()) {
                break;
            }
            battle.nextTurn();
        }
        MatchStats stats = battle.getStats();
        assertEquals(BattleState.LOST, battle.getState());
        assertEquals(0, stats.getCorrectAnswers());
        assertEquals(player.getMaxHp(), stats.getDamageTaken(), "Every hull point lost is counted");
        assertEquals(first, stats.getMisses().get(0).getQuestion());
        assertEquals(WRONG, stats.getMisses().get(0).getPlayerAnswer());
        assertEquals(0, battle.getStars());
    }

    @Test
    void timeoutIsRecordedAsAMiss() {
        SoloBossBattle battle = startBattle(new VoidBrute(3));
        battle.submitTimeout();
        assertEquals(1, battle.getStats().getTotalAnswers());
        assertEquals("", battle.getStats().getMisses().get(0).getPlayerAnswer());
    }

    @Test
    void noStarsWhileTheBattleIsStillGoing() {
        SoloBossBattle battle = startBattle(new VoidBrute(3));
        answerRight(battle);
        assertEquals(0, battle.getStars());
    }

    @Test
    void hexBoltReshufflesAMissedQuestionAtLevelThree() {
        SoloBossBattle battle = startBattle(new NebulaLich(3));
        boolean reshuffled = false;
        for (int turn = 0; turn < 8; turn++) {
            MultipleChoiceQuestion question = (MultipleChoiceQuestion) battle.getCurrentQuestion();
            List<String> before = question.getOptions();
            String right = question.getCorrectAnswer();
            player.heal(100);
            battle.submitAnswer(WRONG);
            reshuffled |= !before.equals(question.getOptions());
            assertEquals(right, question.getOptions().get(question.getCorrectIndex()));
            battle.nextTurn();
        }
        assertTrue(reshuffled);
    }

    @Test
    void noHexBoltBelowLevelThree() {
        SoloBossBattle battle = startBattle(new NebulaLich(2));
        for (int turn = 0; turn < 8; turn++) {
            MultipleChoiceQuestion question = (MultipleChoiceQuestion) battle.getCurrentQuestion();
            List<String> before = question.getOptions();
            player.heal(100);
            battle.submitAnswer(WRONG);
            assertEquals(before, question.getOptions());
            battle.nextTurn();
        }
    }
}
