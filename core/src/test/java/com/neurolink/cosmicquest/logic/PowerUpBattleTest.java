package com.neurolink.cosmicquest.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.neurolink.cosmicquest.model.Entity;
import com.neurolink.cosmicquest.model.EnumerationQuestion;
import com.neurolink.cosmicquest.model.LunarBeast;
import com.neurolink.cosmicquest.model.MultipleChoiceQuestion;
import com.neurolink.cosmicquest.model.NebulaLich;
import com.neurolink.cosmicquest.model.Player;
import com.neurolink.cosmicquest.model.ReasoningQuestion;
import com.neurolink.cosmicquest.model.StarWeaver;
import com.neurolink.cosmicquest.model.VoidBrute;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import org.junit.jupiter.api.Test;

/** Power-ups inside a real solo battle: earning, using, hints and Thread Bind. */
class PowerUpBattleTest {

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

    // ---- Earning -------------------------------------------------------------

    @Test
    void thirdStreakEarnsAPowerUp() {
        SoloBossBattle battle = startBattle(new LunarBeast(3));
        assertNull(answerRight(battle).getPowerUpEarned());
        battle.nextTurn();
        assertNull(answerRight(battle).getPowerUpEarned());
        battle.nextTurn();
        TurnResult third = answerRight(battle);
        assertNotNull(third.getPowerUpEarned());
        assertEquals(List.of(third.getPowerUpEarned()), battle.getInventory().getPowerUps());
    }

    @Test
    void fullSlotsSkipTheReward() {
        SoloBossBattle battle = startBattle(new LunarBeast(3));
        battle.getInventory().add(new Shield());
        battle.getInventory().add(new Shield());
        battle.getInventory().add(new Shield());
        player.increaseStreak();
        player.increaseStreak();
        TurnResult third = answerRight(battle);
        assertEquals(3, player.getStreak());
        assertNull(third.getPowerUpEarned());
        assertEquals(3, battle.getInventory().size());
    }

    // ---- Using -----------------------------------------------------------------

    @Test
    void usingDoesNotUseUpTheTurn() {
        SoloBossBattle battle = startBattle(new VoidBrute(1));
        battle.getInventory().add(new Repair());
        battle.usePowerUp(0);
        assertEquals(BattleState.QUESTION, battle.getState());
        assertEquals(1, battle.getCurrentTurn());
        assertTrue(battle.getInventory().getPowerUps().isEmpty());
    }

    @Test
    void canOnlyUseWhileAQuestionIsWaiting() {
        SoloBossBattle battle = startBattle(new VoidBrute(1));
        battle.getInventory().add(new Repair());
        battle.submitAnswer(WRONG);
        assertThrows(IllegalStateException.class, () -> battle.usePowerUp(0));
    }

    @Test
    void emptySlotCannotBeUsed() {
        SoloBossBattle battle = startBattle(new VoidBrute(1));
        assertThrows(IllegalArgumentException.class, () -> battle.usePowerUp(0));
    }

    @Test
    void shieldBlocksTheNextStrike() {
        SoloBossBattle battle = startBattle(new VoidBrute(1));
        battle.getInventory().add(new Shield());
        battle.usePowerUp(0);
        assertEquals(0, battle.submitAnswer(WRONG).getDamageTaken());
        assertEquals(100, player.getHp());
    }

    @Test
    void repairHealsTwentyButNotAboveMax() {
        SoloBossBattle battle = startBattle(new VoidBrute(1));
        player.takeDamage(30);
        battle.getInventory().add(new Repair());
        battle.getInventory().add(new Repair());
        battle.usePowerUp(0);
        assertEquals(90, player.getHp());
        battle.usePowerUp(0);
        assertEquals(100, player.getHp());
    }

    @Test
    void doubleDamageDoublesTheNextHit() {
        SoloBossBattle battle = startBattle(new VoidBrute(3));
        int difficulty = battle.getCurrentQuestion().getDifficulty();
        battle.getInventory().add(new DoubleDamage());
        battle.usePowerUp(0);
        assertEquals(2 * DamageCalculator.playerDamage(difficulty, 1), answerRight(battle).getDamageDealt());
    }

    // ---- Hint ----------------------------------------------------------------

    @Test
    void hintHidesTwoWrongOptionsNeverTheRightOne() {
        for (int seed = 0; seed < 20; seed++) {
            SoloBossBattle battle = new SoloBossBattle(new Player("P"), new NebulaLich(1),
                    new QuestionGenerator(lesson(), new Random(seed)), new Random(seed));
            battle.startBattle();
            battle.getInventory(battle.getPlayer()).add(new Hint());
            battle.usePowerUp(0);
            MultipleChoiceQuestion q = (MultipleChoiceQuestion) battle.getCurrentQuestion();
            Set<Integer> hidden = battle.getHiddenOptions();
            assertEquals(2, hidden.size());
            assertFalse(hidden.contains(q.getCorrectIndex()));
            assertTrue(battle.getHint().startsWith("Not "));
        }
    }

    @Test
    void hintShowsFirstLetterForIdentification() {
        SoloBossBattle battle = startBattle(new VoidBrute(1));
        battle.getInventory().add(new Hint());
        battle.usePowerUp(0);
        String term = battle.getCurrentQuestion().getCorrectAnswer();
        assertEquals(Hint.maskAfterFirstLetter(term), battle.getHint());
        assertTrue(battle.getHiddenOptions().isEmpty());
    }

    @Test
    void hintShowsFirstLetterOfEveryListItem() {
        SoloBossBattle battle = startBattle(new LunarBeast(1));
        battle.getInventory().add(new Hint());
        battle.usePowerUp(0);
        assertTrue(battle.getCurrentQuestion() instanceof EnumerationQuestion);
        assertEquals("Starts with: E..., I..., P..., A...", battle.getHint());
    }

    @Test
    void hintSuggestsAKeywordForReasoning() {
        SoloBossBattle battle = startBattle(new StarWeaver(1));
        battle.getInventory().add(new Hint());
        battle.usePowerUp(0);
        ReasoningQuestion q = (ReasoningQuestion) battle.getCurrentQuestion();
        assertTrue(battle.getHint().contains(q.getKeywords().get(0)));
    }

    @Test
    void hintIsClearedForTheNextQuestion() {
        SoloBossBattle battle = startBattle(new NebulaLich(1));
        battle.getInventory().add(new Hint());
        battle.usePowerUp(0);
        battle.submitAnswer(WRONG);
        battle.nextTurn();
        assertNull(battle.getHint());
        assertTrue(battle.getHiddenOptions().isEmpty());
    }

    // ---- Thread Bind -----------------------------------------------------------

    @Test
    void threadBindLocksAPowerUpForTwoTurns() {
        SoloBossBattle battle = startBattle(new StarWeaver(3));
        battle.getInventory().add(new Shield());

        battle.submitAnswer(WRONG);                 // turn 1: the Star Weaver strikes and locks it
        assertTrue(battle.getInventory().isLocked(0));

        battle.nextTurn();                          // turn 2: still locked
        assertThrows(IllegalStateException.class, () -> battle.usePowerUp(0));
        answerRight(battle);

        battle.nextTurn();                          // turn 3: still locked
        assertTrue(battle.getInventory().isLocked(0));
        answerRight(battle);

        battle.nextTurn();                          // turn 4: free again
        assertFalse(battle.getInventory().isLocked(0));
        battle.usePowerUp(0);
        assertEquals(1, player.getShieldTurns());
    }

    @Test
    void otherAliensDoNotLockPowerUps() {
        SoloBossBattle battle = startBattle(new VoidBrute(1));
        battle.getInventory().add(new Shield());
        battle.submitAnswer(WRONG);
        assertFalse(battle.getInventory().isLocked(0));
    }
}
