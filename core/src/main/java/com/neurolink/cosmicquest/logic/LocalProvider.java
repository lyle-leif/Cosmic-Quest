package com.neurolink.cosmicquest.logic;

import com.neurolink.cosmicquest.model.Question;
import com.neurolink.cosmicquest.model.QuestionType;
import java.util.ArrayList;
import java.util.List;

/**
 * Makes questions offline with {@link QuestionGenerator}. It always works, so
 * it is also the fallback when AI is off or fails.
 */
public class LocalProvider implements QuestionProvider {

    /** The status line for local questions. */
    public static final String STATUS = "Local questions";

    private final QuestionGenerator generator;

    /**
     * Wraps a generator.
     *
     * @param generator makes questions from the loaded lesson.
     * @throws IllegalArgumentException if generator is null.
     */
    public LocalProvider(QuestionGenerator generator) {
        if (generator == null) {
            throw new IllegalArgumentException("The generator must not be null.");
        }
        this.generator = generator;
    }

    @Override
    public boolean supports(QuestionType type) {
        return generator.supports(type);
    }

    @Override
    public List<Question> provide(QuestionType type, int count) {
        if (count < 0) {
            throw new IllegalArgumentException("Count must not be negative, got " + count + ".");
        }
        List<Question> questions = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            questions.add(generator.generate(type));
        }
        return questions;
    }

    @Override
    public String getStatus() {
        return STATUS;
    }
}
