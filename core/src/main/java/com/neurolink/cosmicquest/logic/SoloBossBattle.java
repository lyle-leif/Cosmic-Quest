package com.neurolink.cosmicquest.logic;

import com.neurolink.cosmicquest.model.EnumerationQuestion;
import com.neurolink.cosmicquest.model.Entity;
import com.neurolink.cosmicquest.model.Player;
import com.neurolink.cosmicquest.model.Question;
import java.util.Random;

/**
 * One player against one alien.
 *
 * <ul>
 * <li><b>Right answer:</b> the streak grows first, then the player hits the
 *     alien with the new streak's bonus (decision D8). Score grows by the damage.</li>
 * <li><b>Wrong answer or timeout:</b> the streak resets, the question goes to
 *     the back of the pool, and the alien strikes, using its special move if it
 *     has one.</li>
 * <li><b>Partly right enumeration:</b> the player deals partial damage, but it
 *     still counts as a miss (the alien strikes and the question returns).</li>
 * <li><b>Order of checks:</b> if the player's hit defeats the alien, it does not
 *     strike back, so the player wins (decision D4).</li>
 * </ul>
 */
public class SoloBossBattle extends BattleMode {

    private static final double FULL_MATCH = 1.0;
    private static final double NO_MATCH = 0.0;

    private final Player player;

    /**
     * Prepares a solo battle with its own random order.
     *
     * @param player the player.
     * @param entity the alien to fight.
     * @param generator makes questions from the loaded lesson.
     * @throws IllegalArgumentException if an argument is null or the lesson
     *         cannot make questions of the alien's type.
     */
    public SoloBossBattle(Player player, Entity entity, QuestionGenerator generator) {
        this(player, entity, generator, new Random());
    }

    /**
     * Prepares a solo battle with a given source of randomness, for tests.
     *
     * @param player the player.
     * @param entity the alien to fight.
     * @param generator makes questions from the loaded lesson.
     * @param random shuffles the question pool.
     * @throws IllegalArgumentException if an argument is null or the lesson
     *         cannot make questions of the alien's type.
     */
    public SoloBossBattle(Player player, Entity entity, QuestionGenerator generator, Random random) {
        super(entity, generator, random);
        if (player == null) {
            throw new IllegalArgumentException("The player must not be null.");
        }
        this.player = player;
    }

    /** Returns the player. */
    public Player getPlayer() {
        return player;
    }

    @Override
    public TurnResult submitAnswer(String answer) {
        requireAwaitingAnswer();
        double ratio = scoreAnswer(currentQuestion, answer);
        if (ratio >= FULL_MATCH) {
            return resolveCorrect(answer);
        }
        return resolveMiss(answer, ratio, false);
    }

    @Override
    public TurnResult submitTimeout() {
        requireAwaitingAnswer();
        return resolveMiss(null, NO_MATCH, true);
    }

    /** Streak first (D8), then the hit; WON if the alien falls. */
    private TurnResult resolveCorrect(String answer) {
        player.increaseStreak();
        int dealt = hitEntity(player.getStreak(), FULL_MATCH);
        // S06 task 9: every 3rd streak grants a random power-up here.
        state = entity.isDefeated() ? BattleState.WON : BattleState.PLAYER_ATTACK;
        return new TurnResult(currentQuestion, answer, true, false, FULL_MATCH, dealt, 0, state);
    }

    /**
     * A wrong, partial or timed-out answer. A partial enumeration still deals
     * damage first; if that defeats the alien, it does not strike back (D4).
     */
    private TurnResult resolveMiss(String answer, double ratio, boolean timedOut) {
        Question question = currentQuestion;
        int dealt = ratio > NO_MATCH ? hitEntity(player.getStreak(), ratio) : 0;
        player.resetStreak();
        questionPool.recordMissedQuestion(question);
        int taken = 0;
        if (entity.isDefeated()) {
            state = BattleState.WON;
        } else {
            taken = strikePlayer(itemsMissed(question, ratio));
            state = player.isDefeated() ? BattleState.LOST : BattleState.ENEMY_ATTACK;
        }
        return new TurnResult(question, answer, false, timedOut, ratio, dealt, taken, state);
    }

    /** Deals the player's damage to the alien and adds it to the score. */
    private int hitEntity(int streak, double ratio) {
        int damage = calculatePlayerDamage(currentQuestion, streak, ratio);
        entity.takeDamage(damage);
        player.addScore(damage);
        return damage;
    }

    /**
     * The alien strikes the player. Returns the damage really taken, which is
     * 0 when a shield blocks the hit.
     */
    private int strikePlayer(int itemsMissed) {
        int hpBefore = player.getHp();
        player.takeDamage(entity.getStrikeDamage(currentTurn, itemsMissed));
        return hpBefore - player.getHp();
    }

    /** Counts the list items left out; 0 for questions that are not enumeration. */
    private static int itemsMissed(Question question, double ratio) {
        if (question instanceof EnumerationQuestion enumeration) {
            int total = enumeration.getItems().size();
            return total - (int) Math.round(ratio * total);
        }
        return 0;
    }
}
