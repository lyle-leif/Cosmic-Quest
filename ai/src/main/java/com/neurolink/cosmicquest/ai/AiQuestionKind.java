package com.neurolink.cosmicquest.ai;

/**
 * The kinds of question the AI can be asked for. It mirrors the game's
 * question types, but lives here so this module never depends on the game.
 */
public enum AiQuestionKind {
    /** Give the definition; the player types the term. */
    IDENTIFICATION,
    /** A question with four options, one of them right. */
    MULTIPLE_CHOICE,
    /** The player lists every item of a group. */
    ENUMERATION,
    /** The player explains in their own words; scored by keywords. */
    REASONING
}
