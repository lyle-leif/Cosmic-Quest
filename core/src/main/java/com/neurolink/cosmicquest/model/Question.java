package com.neurolink.cosmicquest.model;

/**
 * The base of every quiz question in Cosmic Quest. Each question type decides
 * for itself how an answer is checked.
 */
public abstract class Question {

    private static final int MIN_DIFFICULTY = 1;
    private static final int MAX_DIFFICULTY = 3;

    private final String questionText;
    private final String correctAnswer;
    private final int difficulty;

    /**
     * Creates a question. Only subclasses can call this.
     *
     * @param questionText what the player is asked.
     * @param correctAnswer the expected answer.
     * @param difficulty from 1 (easy) to 3 (hard).
     * @throws IllegalArgumentException if a text is blank or the difficulty is not 1 to 3.
     */
    protected Question(String questionText, String correctAnswer, int difficulty) {
        if (questionText == null || questionText.isBlank()) {
            throw new IllegalArgumentException("The question text must not be blank.");
        }
        if (correctAnswer == null || correctAnswer.isBlank()) {
            throw new IllegalArgumentException("The correct answer must not be blank.");
        }
        if (difficulty < MIN_DIFFICULTY || difficulty > MAX_DIFFICULTY) {
            throw new IllegalArgumentException("Difficulty must be 1 to 3, got " + difficulty + ".");
        }
        this.questionText = questionText;
        this.correctAnswer = correctAnswer;
        this.difficulty = difficulty;
    }

    /** Returns what the player is asked. */
    public String getQuestionText() {
        return questionText;
    }

    /** Returns the expected answer. */
    public String getCorrectAnswer() {
        return correctAnswer;
    }

    /** Returns the difficulty, from 1 (easy) to 3 (hard). */
    public int getDifficulty() {
        return difficulty;
    }

    /**
     * Checks the player's answer. Each question type decides how.
     *
     * @param input what the player typed or picked.
     * @return true if the answer is correct.
     */
    public abstract boolean checkAnswer(String input);

    /**
     * Returns which kind of question this is.
     *
     * @return the question type.
     */
    public abstract QuestionType getType();

    /**
     * Makes two answers comparable: trims the edges, makes it lower case and
     * turns any run of spaces into one space. A null input becomes "".
     *
     * @param s the text to clean up.
     * @return the cleaned-up text.
     */
    protected static String normalize(String s) {
        if (s == null) {
            return "";
        }
        return s.trim().toLowerCase().replaceAll("\\s+", " ");
    }
}