package com.neurolink.cosmicquest.logic;

import com.neurolink.cosmicquest.model.EnumerationQuestion;
import com.neurolink.cosmicquest.model.Entity;
import com.neurolink.cosmicquest.model.Question;
import com.neurolink.cosmicquest.model.QuestionType;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * The shared skeleton of every battle. It owns the alien, the question pool
 * and the turn cycle; each mode (Solo, Squad) decides what an answer does by
 * overriding {@link #submitAnswer(String)} and {@link #submitTimeout()}.
 *
 * <p>Every question in a battle matches the alien's question type: a Void
 * Brute asks only identification questions. When the pool runs out, it is
 * refilled from the generator, so a battle never runs dry.
 *
 * <p>Turn cycle: {@link #startBattle()}, then repeat
 * {@link #getCurrentQuestion()}, {@code submitAnswer} (or {@code submitTimeout}),
 * {@link #nextTurn()} until {@link #isOver()}. See {@link BattleState}.
 */
public abstract class BattleMode {

    /** Questions added to the pool each time it is filled. */
    public static final int QUESTIONS_PER_REFILL = 10;

    private static final int DOUBLE_DAMAGE_FACTOR = 2;
    private static final double FULL_MATCH = 1.0;
    private static final double NO_MATCH = 0.0;

    protected final Entity entity;
    protected final QuestionGenerator generator;
    protected final QuestionPool questionPool;
    protected BattleState state = BattleState.QUESTION;
    protected int currentTurn;
    protected Question currentQuestion;
    protected boolean doubleDamageNext;

    /**
     * Prepares a battle against one alien. The battle starts with
     * {@link #startBattle()}.
     *
     * @param entity the alien to fight.
     * @param generator makes questions from the loaded lesson.
     * @param random shuffles the question pool; tests pass a fixed seed.
     * @throws IllegalArgumentException if an argument is null, or the lesson
     *         cannot make questions of the alien's type.
     */
    protected BattleMode(Entity entity, QuestionGenerator generator, Random random) {
        if (entity == null || generator == null || random == null) {
            throw new IllegalArgumentException("Entity, generator and random must not be null.");
        }
        if (!generator.supports(entity.getQuestionType())) {
            throw new IllegalArgumentException("This lesson cannot make " + entity.getQuestionType()
                    + " questions for " + entity.getName() + ".");
        }
        this.entity = entity;
        this.generator = generator;
        this.questionPool = new QuestionPool(makeQuestions(), random);
    }

    /**
     * Answers the current question. Each mode decides what a right or wrong
     * answer does, then moves the state to PLAYER_ATTACK, ENEMY_ATTACK, WON or LOST.
     *
     * @param answer what the player typed.
     * @return what happened this turn.
     * @throws IllegalStateException if no question is waiting for an answer.
     */
    public abstract TurnResult submitAnswer(String answer);

    /**
     * Tells the battle the player ran out of time. It counts as a wrong answer.
     *
     * @return what happened this turn.
     * @throws IllegalStateException if no question is waiting for an answer.
     */
    public abstract TurnResult submitTimeout();

    /**
     * Starts the battle: turn 1 and the first question.
     *
     * @throws IllegalStateException if the battle has already started.
     */
    public void startBattle() {
        if (currentQuestion != null) {
            throw new IllegalStateException("The battle has already started.");
        }
        currentTurn = 1;
        currentQuestion = drawQuestion();
        state = BattleState.QUESTION;
    }

    /**
     * Moves on after a turn's result has been shown: next turn, next question.
     *
     * @throws IllegalStateException if no answer was given yet this turn, or
     *         the battle is over.
     */
    public void nextTurn() {
        if (state != BattleState.PLAYER_ATTACK && state != BattleState.ENEMY_ATTACK) {
            throw new IllegalStateException("Cannot move on from state " + state + ".");
        }
        currentTurn++;
        currentQuestion = drawQuestion();
        state = BattleState.QUESTION;
    }

    /** Ends the battle early, for example when the player quits. It counts as a loss. */
    public void endBattle() {
        if (!state.isFinished()) {
            state = BattleState.LOST;
        }
    }

    /**
     * Returns the question waiting for an answer.
     *
     * @return the current question.
     * @throws IllegalStateException if the battle has not started.
     */
    public Question getCurrentQuestion() {
        if (currentQuestion == null) {
            throw new IllegalStateException("The battle has not started; call startBattle() first.");
        }
        return currentQuestion;
    }

    /** Returns where the battle is in its turn cycle. */
    public BattleState getState() {
        return state;
    }

    /** Returns true once the battle is WON or LOST. */
    public boolean isOver() {
        return state.isFinished();
    }

    /** Returns the alien being fought. */
    public Entity getEntity() {
        return entity;
    }

    /** Returns the turn number, starting at 1; 0 before the battle starts. */
    public int getCurrentTurn() {
        return currentTurn;
    }

    /** Returns true if the next hit will deal double damage. */
    public boolean isDoubleDamageNext() {
        return doubleDamageNext;
    }

    /** Returns every question missed so far, each once, for the debrief. */
    public List<Question> getMissedQuestions() {
        return questionPool.getMissedQuestions();
    }

    /**
     * Throws unless a question is waiting for an answer. Call this first in
     * {@code submitAnswer} and {@code submitTimeout}.
     *
     * @throws IllegalStateException if the battle has not started, the answer
     *         was already given, or the battle is over.
     */
    protected void requireAwaitingAnswer() {
        if (currentQuestion == null) {
            throw new IllegalStateException("The battle has not started; call startBattle() first.");
        }
        if (state != BattleState.QUESTION) {
            throw new IllegalStateException("Not waiting for an answer; the state is " + state + ".");
        }
    }

    /**
     * Scores an answer from 0.0 to 1.0. Enumeration questions can score in
     * between; every other type scores 1.0 or 0.0.
     *
     * @param question the question answered.
     * @param answer what the player typed.
     * @return the share of the answer that was right.
     */
    protected static double scoreAnswer(Question question, String answer) {
        if (question instanceof EnumerationQuestion enumeration) {
            return enumeration.getMatchRatio(answer);
        }
        return question.checkAnswer(answer) ? FULL_MATCH : NO_MATCH;
    }

    /**
     * Works out the player's damage for an answer, using
     * {@link DamageCalculator}. If Double Damage is active it doubles this hit
     * and is used up.
     *
     * @param question the question answered.
     * @param streak correct answers in a row, counting this one (decision D8).
     * @param ratio the answer's score from {@link #scoreAnswer}.
     * @return the damage to deal; 0 when the ratio is 0.
     */
    protected int calculatePlayerDamage(Question question, int streak, double ratio) {
        int damage;
        if (question.getType() == QuestionType.ENUMERATION) {
            damage = DamageCalculator.enumerationDamage(question.getDifficulty(), streak, ratio);
        } else {
            damage = ratio > NO_MATCH ? DamageCalculator.playerDamage(question.getDifficulty(), streak) : 0;
        }
        if (doubleDamageNext && damage > 0) {
            damage *= DOUBLE_DAMAGE_FACTOR;
            doubleDamageNext = false;
        }
        return damage;
    }

    /** Takes the next question, refilling the pool first if it is empty. */
    private Question drawQuestion() {
        if (!questionPool.hasNext()) {
            questionPool.addQuestions(makeQuestions());
        }
        return questionPool.getNextQuestion();
    }

    /** Makes a batch of questions of the alien's type. */
    private List<Question> makeQuestions() {
        List<Question> questions = new ArrayList<>(QUESTIONS_PER_REFILL);
        for (int i = 0; i < QUESTIONS_PER_REFILL; i++) {
            questions.add(generator.generate(entity.getQuestionType()));
        }
        return questions;
    }
}
