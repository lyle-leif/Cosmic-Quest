package com.neurolink.cosmicquest.ai;

import java.util.List;

/**
 * Anything that can write questions from a lesson: one online provider, or a
 * chain of them. The game only talks to this interface.
 */
public interface AiClient {

    /** Returns a short name for messages, such as "Gemini". */
    String getName();

    /**
     * Asks for questions.
     *
     * @param request what to ask for.
     * @return the questions written; never empty.
     * @throws AiException if no usable questions came back, for any reason.
     */
    List<AiQuestion> generate(AiRequest request) throws AiException;
}
