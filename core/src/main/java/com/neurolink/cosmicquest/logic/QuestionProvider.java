package com.neurolink.cosmicquest.logic;

import com.neurolink.cosmicquest.model.Question;
import com.neurolink.cosmicquest.model.QuestionType;
import java.util.List;

/**
 * Where a battle gets its questions. {@link LocalProvider} makes them from the
 * lesson offline; {@link AiQuestionProvider} asks an AI first and falls back
 * to local questions. A battle only knows this interface, so it works the
 * same with either.
 */
public interface QuestionProvider {

    /**
     * Returns true if this provider can make questions of a type.
     *
     * @param type the question type.
     * @return true if questions of that type can be made.
     */
    boolean supports(QuestionType type);

    /**
     * Makes questions of one type.
     *
     * @param type the question type; must be supported.
     * @param count how many questions, 0 or more.
     * @return exactly {@code count} questions.
     * @throws IllegalArgumentException if count is negative.
     * @throws IllegalStateException if the type is not supported.
     */
    List<Question> provide(QuestionType type, int count);

    /**
     * Returns a short line about where the last questions came from, such as
     * "AI questions from Gemini" or "Offline: using local questions".
     *
     * @return the status line.
     */
    String getStatus();
}
