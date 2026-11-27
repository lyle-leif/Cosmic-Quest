package com.neurolink.cosmicquest.logic;

import com.neurolink.cosmicquest.model.Entity;
import com.neurolink.cosmicquest.model.Player;
import java.util.Random;

/**
 * One player against one alien. Every answer is the same player's, so a
 * single defeat loses the battle. The turn rules themselves are shared with
 * Squad and live in {@link BattleMode}.
 */
public class SoloBossBattle extends BattleMode {

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
        this.player = checkPlayer(player);
    }

    /**
     * Prepares a solo battle whose questions come from a provider, such as
     * AI with a local fallback.
     *
     * @param player the player.
     * @param entity the alien to fight.
     * @param provider supplies the questions.
     * @param random shuffles the question pool.
     * @throws IllegalArgumentException if an argument is null or the lesson
     *         cannot make questions of the alien's type.
     */
    public SoloBossBattle(Player player, Entity entity, QuestionProvider provider, Random random) {
        super(entity, provider, random);
        this.player = checkPlayer(player);
    }

    private static Player checkPlayer(Player player) {
        if (player == null) {
            throw new IllegalArgumentException("The player must not be null.");
        }
        return player;
    }

    /** Returns the player. */
    public Player getPlayer() {
        return player;
    }

    /** Returns the player's power-up slots. */
    public PowerUpInventory getInventory() {
        return getInventory(player);
    }

    /**
     * Uses one of the player's power-ups before answering.
     *
     * @param slot the slot, from 0.
     * @return the power-up that was used.
     * @throws IllegalStateException if no question is waiting, or the slot is locked.
     * @throws IllegalArgumentException if the slot is empty.
     */
    public PowerUp usePowerUp(int slot) {
        return usePowerUp(player, slot);
    }

    @Override
    public TurnResult submitAnswer(String answer) {
        return answerFor(player, answer);
    }

    @Override
    public TurnResult submitTimeout() {
        return timeoutFor(player);
    }

    /**
     * Returns the stars earned: 0 until the battle is won, then 1 to 3
     * (see {@link MatchStats#getStars}).
     *
     * @return 0 to 3.
     */
    public int getStars() {
        return stats.getStars(state == BattleState.WON, player.getHp(), player.getMaxHp());
    }

    /** The battle is lost when the one player is at 0 HP. */
    @Override
    protected boolean allPlayersDefeated() {
        return player.isDefeated();
    }
}
