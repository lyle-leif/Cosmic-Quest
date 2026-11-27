package com.neurolink.cosmicquest.logic;

import com.neurolink.cosmicquest.model.Question;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Random;

/**
 * Holds a battle's questions like a deck of cards. Questions are handed out
 * from the front, one at a time. A missed question goes to the back, so the
 * player sees it again later, and it is remembered for the debrief.
 */
public class QuestionPool {

    private final Deque<Question> questions = new ArrayDeque<>();
    private final List<Question> missed = new ArrayList<>();
    private final Random random;

    /**
     * Creates a pool with its own random order.
     *
     * @param questions the questions to use; they are copied and shuffled.
     * @throws IllegalArgumentException if the list or any question in it is null.
     */
    public QuestionPool(List<Question> questions) {
        this(questions, new Random());
    }

    /**
     * Creates a pool with a given source of randomness. Tests pass
     * {@code new Random(42)} so the order is the same every run.
     *
     * @param questions the questions to use; they are copied and shuffled.
     * @param random the source of randomness for shuffling.
     * @throws IllegalArgumentException if any argument or question is null.
     */
    public QuestionPool(List<Question> questions, Random random) {
        if (random == null) {
            throw new IllegalArgumentException("Random must not be null.");
        }
        this.random = random;
        addQuestions(questions);
    }

    /**
     * Shuffles new questions and puts them behind the ones already waiting.
     * Use this to refill the pool from a {@link QuestionGenerator}.
     *
     * @param newQuestions the questions to add.
     * @throws IllegalArgumentException if the list or any question in it is null.
     */
    public void addQuestions(List<Question> newQuestions) {
        if (newQuestions == null) {
            throw new IllegalArgumentException("Questions must not be null.");
        }
        // A loop, not contains(null): lists made with List.of() throw on contains(null).
        for (Question question : newQuestions) {
            if (question == null) {
                throw new IllegalArgumentException("Questions must not be null.");
            }
        }
        List<Question> shuffled = new ArrayList<>(newQuestions);
        Collections.shuffle(shuffled, random);
        questions.addAll(shuffled);
    }

    /** Returns true while at least one question is waiting. */
    public boolean hasNext() {
        return !questions.isEmpty();
    }

    /** Returns how many questions are waiting, including recycled ones. */
    public int size() {
        return questions.size();
    }

    /**
     * Takes the next question from the front of the pool.
     *
     * @return the next question.
     * @throws IllegalStateException if the pool is empty; check {@link #hasNext()} first.
     */
    public Question getNextQuestion() {
        if (questions.isEmpty()) {
            throw new IllegalStateException("The question pool is empty.");
        }
        return questions.poll();
    }

    /**
     * Puts a missed question at the back of the pool, so it comes again
     * after every other waiting question, and remembers it for the debrief.
     *
     * @param question the question the player missed.
     * @throws IllegalArgumentException if question is null.
     */
    public void recordMissedQuestion(Question question) {
        if (question == null) {
            throw new IllegalArgumentException("The missed question must not be null.");
        }
        questions.offer(question);
        if (!missed.contains(question)) {
            missed.add(question);
        }
    }

    /**
     * Returns every question missed so far, each once, in the order first
     * missed. The list cannot be changed.
     *
     * @return the missed questions.
     */
    public List<Question> getMissedQuestions() {
        return Collections.unmodifiableList(new ArrayList<>(missed));
    }
}
