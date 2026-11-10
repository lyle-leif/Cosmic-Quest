package com.neurolink.cosmicquest.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.neurolink.cosmicquest.model.EnumerationQuestion;
import com.neurolink.cosmicquest.model.Entity;
import com.neurolink.cosmicquest.model.IdentificationQuestion;
import com.neurolink.cosmicquest.model.LunarBeast;
import com.neurolink.cosmicquest.model.NebulaLich;
import com.neurolink.cosmicquest.model.Question;
import com.neurolink.cosmicquest.model.QuestionType;
import com.neurolink.cosmicquest.model.VoidBrute;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.junit.jupiter.api.Test;

class BattleModeTest {

    private static final long SEED = 42;

    /**
     * The smallest possible battle mode, only for testing the shared code in
     * BattleMode: a right answer hits the alien, a wrong one changes nothing.
     */
    private static class TestBattle extends BattleMode {

        TestBattle(Entity entity, LessonData lesson) {
            super(entity, new QuestionGenerator(lesson, new Random(SEED)), new Random(SEED));
        }

        @Override
        public TurnResult submitAnswer(String answer) {
            requireAwaitingAnswer();
            double ratio = scoreAnswer(currentQuestion, answer);
            int damage = calculatePlayerDamage(currentQuestion, 1, ratio);
            entity.takeDamage(damage);
            state = entity.isDefeated() ? BattleState.WON
                    : ratio > 0 ? BattleState.PLAYER_ATTACK : BattleState.ENEMY_ATTACK;
            if (ratio < 1.0) {
                questionPool.recordMissedQuestion(currentQuestion);
            }
            return new TurnResult(currentQuestion, answer, ratio == 1.0, false, ratio, damage, 0, state);
        }

        @Override
        public TurnResult submitTimeout() {
            requireAwaitingAnswer();
            state = BattleState.ENEMY_ATTACK;
            questionPool.recordMissedQuestion(currentQuestion);
            return new TurnResult(currentQuestion, null, false, true, 0.0, 0, 0, state);
        }

        @Override
        protected boolean allPlayersDefeated() {
            return false;
        }
    }

    private static LessonData lesson() {
        return new LessonData(List.of(
                new Term("Class", "A blueprint that describes the fields and methods of an object."),
                new Term("Object", "An instance of a class that contains data and can perform actions."),
                new Term("Method", "A block of code that performs a specific task."),
                new Term("Field", "A variable that stores data belonging to an object.")),
                Map.of("Pillars of OOP", List.of("Encapsulation", "Inheritance", "Polymorphism", "Abstraction")));
    }

    private static TestBattle startedBattle(Entity entity) {
        TestBattle battle = new TestBattle(entity, lesson());
        battle.startBattle();
        return battle;
    }

    @Test
    void startsOnTurnOneWithAQuestion() {
        TestBattle battle = startedBattle(new VoidBrute(1));
        assertEquals(BattleState.QUESTION, battle.getState());
        assertEquals(1, battle.getCurrentTurn());
        assertNotNull(battle.getCurrentQuestion());
        assertFalse(battle.isOver());
    }

    @Test
    void everyQuestionMatchesTheAliensType() {
        TestBattle battle = startedBattle(new NebulaLich(1));
        for (int i = 0; i < 25; i++) {
            assertEquals(QuestionType.MULTIPLE_CHOICE, battle.getCurrentQuestion().getType());
            battle.submitAnswer("wrong answer");
            battle.nextTurn();
        }
    }

    @Test
    void poolRefillsSoTheBattleNeverRunsDry() {
        // Only right answers, so nothing is recycled and the pool really empties.
        // The state is set directly so the alien is not defeated along the way.
        TestBattle battle = startedBattle(new VoidBrute(3));
        int turns = BattleMode.QUESTIONS_PER_REFILL * 3;
        for (int i = 0; i < turns; i++) {
            battle.state = BattleState.PLAYER_ATTACK;
            battle.nextTurn();
            assertNotNull(battle.getCurrentQuestion());
        }
        assertEquals(turns + 1, battle.getCurrentTurn());
        assertTrue(battle.getMissedQuestions().isEmpty());
    }

    @Test
    void cannotUseBattleBeforeStart() {
        TestBattle battle = new TestBattle(new VoidBrute(1), lesson());
        assertEquals(0, battle.getCurrentTurn());
        assertThrows(IllegalStateException.class, battle::getCurrentQuestion);
        assertThrows(IllegalStateException.class, () -> battle.submitAnswer("x"));
    }

    @Test
    void cannotStartTwice() {
        TestBattle battle = startedBattle(new VoidBrute(1));
        assertThrows(IllegalStateException.class, battle::startBattle);
    }

    @Test
    void cannotAnswerTwiceInOneTurn() {
        TestBattle battle = startedBattle(new VoidBrute(1));
        battle.submitAnswer("wrong");
        assertThrows(IllegalStateException.class, () -> battle.submitAnswer("again"));
        assertThrows(IllegalStateException.class, battle::submitTimeout);
    }

    @Test
    void cannotSkipToNextTurnWithoutAnswering() {
        TestBattle battle = startedBattle(new VoidBrute(1));
        assertThrows(IllegalStateException.class, battle::nextTurn);
    }

    @Test
    void nextTurnShowsANewQuestion() {
        TestBattle battle = startedBattle(new VoidBrute(1));
        battle.submitAnswer(battle.getCurrentQuestion().getCorrectAnswer());
        assertEquals(BattleState.PLAYER_ATTACK, battle.getState());
        battle.nextTurn();
        assertEquals(BattleState.QUESTION, battle.getState());
        assertEquals(2, battle.getCurrentTurn());
    }

    @Test
    void nothingIsAcceptedAfterTheBattleIsWon() {
        TestBattle battle = startedBattle(new VoidBrute(1));
        while (!battle.isOver()) {
            battle.submitAnswer(battle.getCurrentQuestion().getCorrectAnswer());
            if (!battle.isOver()) {
                battle.nextTurn();
            }
        }
        assertEquals(BattleState.WON, battle.getState());
        assertThrows(IllegalStateException.class, () -> battle.submitAnswer("x"));
        assertThrows(IllegalStateException.class, battle::nextTurn);
    }

    @Test
    void endBattleCountsAsLossButKeepsAWin() {
        TestBattle quitter = startedBattle(new VoidBrute(1));
        quitter.endBattle();
        assertEquals(BattleState.LOST, quitter.getState());
        assertTrue(quitter.isOver());

        TestBattle winner = startedBattle(new VoidBrute(1));
        while (!winner.isOver()) {
            winner.submitAnswer(winner.getCurrentQuestion().getCorrectAnswer());
            if (!winner.isOver()) {
                winner.nextTurn();
            }
        }
        winner.endBattle();
        assertEquals(BattleState.WON, winner.getState());
    }

    @Test
    void missedQuestionsAreListedForTheDebrief() {
        TestBattle battle = startedBattle(new VoidBrute(1));
        Question first = battle.getCurrentQuestion();
        battle.submitTimeout();
        assertEquals(List.of(first), battle.getMissedQuestions());
    }

    @Test
    void scoreAnswerGivesPartialCreditOnlyForEnumeration() {
        Question enumeration = new EnumerationQuestion("List them.", List.of("a", "b", "c", "d"), 2);
        Question identification = new IdentificationQuestion("Stores data.", "Field", 1);
        assertEquals(0.75, BattleMode.scoreAnswer(enumeration, "a, b, c"), 0.0001);
        assertEquals(1.0, BattleMode.scoreAnswer(identification, "field"), 0.0001);
        assertEquals(0.0, BattleMode.scoreAnswer(identification, "method"), 0.0001);
    }

    @Test
    void damageUsesTheCalculator() {
        TestBattle battle = startedBattle(new VoidBrute(1));
        Question identification = new IdentificationQuestion("Stores data.", "Field", 2);
        Question enumeration = new EnumerationQuestion("List them.", List.of("a", "b", "c", "d"), 2);
        assertEquals(DamageCalculator.playerDamage(2, 1), battle.calculatePlayerDamage(identification, 1, 1.0));
        assertEquals(0, battle.calculatePlayerDamage(identification, 1, 0.0));
        assertEquals(DamageCalculator.enumerationDamage(2, 1, 0.75),
                battle.calculatePlayerDamage(enumeration, 1, 0.75));
    }

    @Test
    void doubleDamageDoublesOneHitThenWearsOff() {
        TestBattle battle = startedBattle(new VoidBrute(1));
        Question question = new IdentificationQuestion("Stores data.", "Field", 1);
        battle.doubleDamageNext = true;
        assertEquals(0, battle.calculatePlayerDamage(question, 1, 0.0));
        assertTrue(battle.isDoubleDamageNext(), "A miss must not use up Double Damage");
        assertEquals(44, battle.calculatePlayerDamage(question, 1, 1.0));
        assertFalse(battle.isDoubleDamageNext());
        assertEquals(22, battle.calculatePlayerDamage(question, 1, 1.0));
    }

    @Test
    void rejectsAlienTheLessonCannotSupport() {
        LessonData noLists = new LessonData(lesson().getTerms(), Map.of());
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> new TestBattle(new LunarBeast(1), noLists));
        assertTrue(e.getMessage().contains("Lunar Beast"));
    }

    @Test
    void rejectsNulls() {
        QuestionGenerator generator = new QuestionGenerator(lesson());
        assertThrows(IllegalArgumentException.class,
                () -> new BattleMode(null, generator, new Random()) {
                    @Override
                    public TurnResult submitAnswer(String answer) {
                        return null;
                    }

                    @Override
                    public TurnResult submitTimeout() {
                        return null;
                    }

                    @Override
                    protected boolean allPlayersDefeated() {
                        return false;
                    }
                });
    }

    @Test
    void finishedStatesAreOnlyWonAndLost() {
        assertTrue(BattleState.WON.isFinished());
        assertTrue(BattleState.LOST.isFinished());
        assertFalse(BattleState.QUESTION.isFinished());
        assertFalse(BattleState.PLAYER_ATTACK.isFinished());
        assertFalse(BattleState.ENEMY_ATTACK.isFinished());
    }
}
