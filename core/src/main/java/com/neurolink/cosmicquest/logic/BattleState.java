package com.neurolink.cosmicquest.logic;

/**
 * Where a battle is in its turn cycle.
 *
 * <pre>
 * startBattle()               -> QUESTION
 * QUESTION      right answer  -> PLAYER_ATTACK   (or WON if the alien reaches 0 HP)
 * QUESTION      wrong/timeout -> ENEMY_ATTACK    (or LOST if the player reaches 0 HP)
 * PLAYER_ATTACK nextTurn()    -> QUESTION
 * ENEMY_ATTACK  nextTurn()    -> QUESTION
 * any state     endBattle()   -> LOST
 * </pre>
 *
 * PLAYER_ATTACK and ENEMY_ATTACK are the moment the result is shown (and, in
 * the GUI, animated). WON and LOST are final: no more answers or turns are
 * accepted.
 */
public enum BattleState {

    /** A question is shown and the battle waits for an answer. */
    QUESTION,

    /** The player answered right and hit the alien; call nextTurn() to continue. */
    PLAYER_ATTACK,

    /** The player answered wrong or ran out of time and the alien struck; call nextTurn() to continue. */
    ENEMY_ATTACK,

    /** The alien was defeated. */
    WON,

    /** The player (or the whole team) was defeated, or the battle was ended early. */
    LOST;

    /**
     * Tells whether the battle has ended.
     *
     * @return true for WON and LOST.
     */
    public boolean isFinished() {
        return this == WON || this == LOST;
    }
}
