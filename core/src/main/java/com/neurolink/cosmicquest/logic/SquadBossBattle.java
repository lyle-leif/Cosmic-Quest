package com.neurolink.cosmicquest.logic;

import com.neurolink.cosmicquest.model.Entity;
import com.neurolink.cosmicquest.model.Player;
import com.neurolink.cosmicquest.model.Team;
import java.util.Random;

/**
 * A team of 2 to 3 players against one level 3 boss, taking turns on one
 * computer.
 *
 * <ul>
 * <li>Each turn belongs to one player: they answer, and on a miss the boss
 *     strikes them. Turns rotate in team order after every answer.</li>
 * <li>Each player keeps their own streak, score and power-up slots.</li>
 * <li>A defeated player is skipped. The team loses only when every player is
 *     at 0 HP.</li>
 * </ul>
 * The turn rules themselves are shared with Solo and live in {@link BattleMode}.
 */
public class SquadBossBattle extends BattleMode {

    /** The only level a Squad boss can be. */
    public static final int BOSS_LEVEL = 3;

    private final Team team;
    private Player currentPlayer;

    /**
     * Prepares a Squad battle with its own random order.
     *
     * @param team the players, in turn order.
     * @param boss the level 3 alien to fight.
     * @param generator makes questions from the loaded lesson.
     * @throws IllegalArgumentException if an argument is null, the team has
     *         fewer than 2 players, the boss is not level 3, or the lesson
     *         cannot make questions of the boss's type.
     */
    public SquadBossBattle(Team team, Entity boss, QuestionGenerator generator) {
        this(team, boss, generator, new Random());
    }

    /**
     * Prepares a Squad battle with a given source of randomness, for tests.
     *
     * @param team the players, in turn order.
     * @param boss the level 3 alien to fight.
     * @param generator makes questions from the loaded lesson.
     * @param random shuffles the question pool.
     * @throws IllegalArgumentException if an argument is null, the team has
     *         fewer than 2 players, the boss is not level 3, or the lesson
     *         cannot make questions of the boss's type.
     */
    public SquadBossBattle(Team team, Entity boss, QuestionGenerator generator, Random random) {
        super(boss, generator, random);
        if (team == null) {
            throw new IllegalArgumentException("The team must not be null.");
        }
        if (team.size() < Team.MIN_PLAYERS) {
            throw new IllegalArgumentException("A Squad battle needs at least " + Team.MIN_PLAYERS
                    + " players, got " + team.size() + ".");
        }
        if (boss.getLevel() != BOSS_LEVEL) {
            throw new IllegalArgumentException("A Squad boss must be level " + BOSS_LEVEL
                    + ", got " + boss.getLevel() + ".");
        }
        this.team = team;
    }

    /** Starts the battle with the first player still fighting. */
    @Override
    public void startBattle() {
        super.startBattle();
        currentPlayer = team.getNextAlivePlayer();
    }

    /** Moves on to the next question and the next player still fighting. */
    @Override
    public void nextTurn() {
        super.nextTurn();
        currentPlayer = team.getNextAlivePlayer();
    }

    @Override
    public TurnResult submitAnswer(String answer) {
        return answerFor(requireCurrentPlayer(), answer);
    }

    @Override
    public TurnResult submitTimeout() {
        return timeoutFor(requireCurrentPlayer());
    }

    /** Returns the team. */
    public Team getTeam() {
        return team;
    }

    /** Returns the player whose turn it is, or null before the battle starts. */
    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    /** Returns the power-up slots of the player whose turn it is. */
    public PowerUpInventory getInventory() {
        return getInventory(requireCurrentPlayer());
    }

    /**
     * Uses one of the current player's power-ups before they answer.
     *
     * @param slot the slot, from 0.
     * @return the power-up that was used.
     * @throws IllegalStateException if no question is waiting, or the slot is locked.
     * @throws IllegalArgumentException if the slot is empty.
     */
    public PowerUp usePowerUp(int slot) {
        return usePowerUp(requireCurrentPlayer(), slot);
    }

    /**
     * Returns the stars earned, using the whole team's hull: 0 until the
     * battle is won, then 1 to 3 (see {@link MatchStats#getStars}).
     *
     * @return 0 to 3.
     */
    public int getStars() {
        return stats.getStars(state == BattleState.WON, team.getTotalHp(), team.getTotalMaxHp());
    }

    /** The battle is lost only when every player on the team is at 0 HP. */
    @Override
    protected boolean allPlayersDefeated() {
        return team.isDefeated();
    }

    private Player requireCurrentPlayer() {
        if (currentPlayer == null) {
            throw new IllegalStateException("The battle has not started; call startBattle() first.");
        }
        return currentPlayer;
    }
}
