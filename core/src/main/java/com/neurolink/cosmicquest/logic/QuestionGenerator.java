package com.neurolink.cosmicquest.logic;

import com.neurolink.cosmicquest.model.EnumerationQuestion;
import com.neurolink.cosmicquest.model.IdentificationQuestion;
import com.neurolink.cosmicquest.model.MultipleChoiceQuestion;
import com.neurolink.cosmicquest.model.Question;
import com.neurolink.cosmicquest.model.QuestionType;
import com.neurolink.cosmicquest.model.ReasoningQuestion;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Turns a lesson into quiz questions of all four types, with no internet.
 * Terms and lists are drawn from shuffled decks, so the same term does not
 * come back until every other term has been used. A question type the lesson
 * cannot support is skipped (no lists means no enumeration; fewer than four
 * terms means no multiple choice).
 */
public class QuestionGenerator {

    private static final int MULTIPLE_CHOICE_OPTIONS = 4;
    private static final int MIN_KEYWORDS = 2;
    private static final int MAX_KEYWORDS = 5;
    private static final int MIN_KEYWORD_LENGTH = 4;

    /** Difficulty of identification questions; AI questions use the same values. */
    public static final int IDENTIFICATION_DIFFICULTY = 2;
    /** Difficulty of multiple choice questions. */
    public static final int MULTIPLE_CHOICE_DIFFICULTY = 1;
    /** Difficulty of enumeration questions. */
    public static final int ENUMERATION_DIFFICULTY = 2;
    /** Difficulty of reasoning questions. */
    public static final int REASONING_DIFFICULTY = 3;

    private static final String NOT_LETTER = "[^a-z]+";
    private static final Set<String> COMMON_WORDS = Set.of(
            "about", "also", "another", "been", "being", "cannot", "does", "each", "from",
            "have", "into", "make", "makes", "more", "most", "must", "only", "other",
            "same", "some", "such", "than",
            "that", "their", "them", "then", "there", "these", "they", "this", "those",
            "used", "uses", "what", "when", "where", "which", "while", "will", "with",
            "without", "would", "your");

    private final LessonData data;
    private final Random random;
    private final List<String> distinctTermNames;
    private final List<Term> reasoningTerms;
    private final List<String> enumerationTitles;
    private final Deque<Term> termDeck = new ArrayDeque<>();
    private final Deque<Term> reasoningDeck = new ArrayDeque<>();
    private final Deque<String> enumerationDeck = new ArrayDeque<>();

    /**
     * Creates a generator with its own random order.
     *
     * @param data the lesson to make questions from.
     * @throws IllegalArgumentException if data is null.
     */
    public QuestionGenerator(LessonData data) {
        this(data, new Random());
    }

    /**
     * Creates a generator with a given source of randomness. Tests pass
     * {@code new Random(42)} so the questions come out the same every run.
     *
     * @param data the lesson to make questions from.
     * @param random the source of randomness.
     * @throws IllegalArgumentException if data or random is null.
     */
    public QuestionGenerator(LessonData data, Random random) {
        if (data == null || random == null) {
            throw new IllegalArgumentException("Lesson data and random must not be null.");
        }
        this.data = data;
        this.random = random;
        this.distinctTermNames = findDistinctTermNames(data.getTerms());
        this.reasoningTerms = findReasoningTerms(data.getTerms());
        this.enumerationTitles = new ArrayList<>(data.getEnumerations().keySet());
    }

    /**
     * Tells whether this lesson has enough material for a question type.
     *
     * @param type the question type to check.
     * @return true if questions of that type can be made.
     */
    public boolean supports(QuestionType type) {
        switch (type) {
            case IDENTIFICATION:
                return !data.getTerms().isEmpty();
            case MULTIPLE_CHOICE:
                return distinctTermNames.size() >= MULTIPLE_CHOICE_OPTIONS;
            case ENUMERATION:
                return !enumerationTitles.isEmpty();
            case REASONING:
                return !reasoningTerms.isEmpty();
            default:
                return false;
        }
    }

    /**
     * Returns every question type this lesson can support, in enum order.
     *
     * @return the supported types; empty if the lesson has no terms or lists.
     */
    public List<QuestionType> getSupportedTypes() {
        List<QuestionType> types = new ArrayList<>();
        for (QuestionType type : QuestionType.values()) {
            if (supports(type)) {
                types.add(type);
            }
        }
        return types;
    }

    /**
     * Makes a mixed list of questions. Supported types take turns, so every
     * supported type appears once {@code count} is at least the number of
     * supported types. The list is shuffled at the end.
     *
     * @param count how many questions to make.
     * @return the questions, in random order.
     * @throws IllegalArgumentException if count is negative.
     * @throws IllegalStateException if count is above 0 and the lesson supports no type.
     */
    public List<Question> generateQuestions(int count) {
        if (count < 0) {
            throw new IllegalArgumentException("Count must not be negative, got " + count + ".");
        }
        List<QuestionType> types = getSupportedTypes();
        if (count > 0 && types.isEmpty()) {
            throw new IllegalStateException("The lesson has no terms or lists to make questions from.");
        }
        Collections.shuffle(types, random);
        List<Question> questions = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            questions.add(generate(types.get(i % types.size())));
        }
        Collections.shuffle(questions, random);
        return questions;
    }

    /**
     * Makes one question of the given type.
     *
     * @param type the question type to make.
     * @return the new question.
     * @throws IllegalStateException if the lesson does not support that type.
     */
    public Question generate(QuestionType type) {
        switch (type) {
            case IDENTIFICATION:
                return generateIdentification();
            case MULTIPLE_CHOICE:
                return generateMultipleChoice();
            case ENUMERATION:
                return generateEnumeration();
            case REASONING:
                return generateReasoning();
            default:
                throw new IllegalStateException("Unknown question type: " + type);
        }
    }

    /**
     * Shows a definition; the player types the term.
     *
     * @return the new question.
     * @throws IllegalStateException if the lesson has no terms.
     */
    public IdentificationQuestion generateIdentification() {
        requireSupport(QuestionType.IDENTIFICATION);
        Term term = draw(termDeck, data.getTerms());
        return new IdentificationQuestion(term.getDefinition(), term.getTerm(), IDENTIFICATION_DIFFICULTY);
    }

    /**
     * Shows a definition with four terms to pick from: the right one and three
     * different ones, shuffled.
     *
     * @return the new question.
     * @throws IllegalStateException if the lesson has fewer than four different terms.
     */
    public MultipleChoiceQuestion generateMultipleChoice() {
        requireSupport(QuestionType.MULTIPLE_CHOICE);
        Term term = draw(termDeck, data.getTerms());
        List<String> others = new ArrayList<>();
        for (String name : distinctTermNames) {
            if (!name.equalsIgnoreCase(term.getTerm())) {
                others.add(name);
            }
        }
        Collections.shuffle(others, random);
        List<String> options = new ArrayList<>(others.subList(0, MULTIPLE_CHOICE_OPTIONS - 1));
        options.add(term.getTerm());
        Collections.shuffle(options, random);
        return new MultipleChoiceQuestion(term.getDefinition(), options,
                options.indexOf(term.getTerm()), MULTIPLE_CHOICE_DIFFICULTY);
    }

    /**
     * Asks the player to name every item of one [ENUM] list.
     *
     * @return the new question.
     * @throws IllegalStateException if the lesson has no lists.
     */
    public EnumerationQuestion generateEnumeration() {
        requireSupport(QuestionType.ENUMERATION);
        String title = draw(enumerationDeck, enumerationTitles);
        return new EnumerationQuestion("List the " + title + ".",
                data.getEnumerations().get(title), ENUMERATION_DIFFICULTY);
    }

    /**
     * Asks the player to explain a term; keywords come from its definition.
     *
     * @return the new question.
     * @throws IllegalStateException if no definition has at least two keywords.
     */
    public ReasoningQuestion generateReasoning() {
        requireSupport(QuestionType.REASONING);
        Term term = draw(reasoningDeck, reasoningTerms);
        return new ReasoningQuestion("Explain " + term.getTerm() + " in your own words.",
                term.getDefinition(), extractKeywords(term), REASONING_DIFFICULTY);
    }

    /**
     * Picks keywords from a term's definition: words of at least four letters
     * that are not common words and not part of the term itself, in order,
     * without repeats, at most five.
     *
     * @param term the term whose definition to read.
     * @return the keywords; may hold fewer than two for a short definition.
     */
    static List<String> extractKeywords(Term term) {
        Set<String> termWords = Set.of(term.getTerm().toLowerCase().split(NOT_LETTER));
        Set<String> keywords = new LinkedHashSet<>();
        for (String word : term.getDefinition().toLowerCase().split(NOT_LETTER)) {
            if (word.length() >= MIN_KEYWORD_LENGTH && !COMMON_WORDS.contains(word)
                    && !termWords.contains(word)) {
                keywords.add(word);
            }
            if (keywords.size() == MAX_KEYWORDS) {
                break;
            }
        }
        return new ArrayList<>(keywords);
    }

    /**
     * Takes the next item from a shuffled deck, refilling and reshuffling it
     * from the source when it runs out.
     */
    private <T> T draw(Deque<T> deck, List<T> source) {
        if (deck.isEmpty()) {
            List<T> shuffled = new ArrayList<>(source);
            Collections.shuffle(shuffled, random);
            deck.addAll(shuffled);
        }
        return deck.poll();
    }

    /** Throws if the lesson cannot make questions of this type. */
    private void requireSupport(QuestionType type) {
        if (!supports(type)) {
            throw new IllegalStateException("This lesson cannot make " + type + " questions.");
        }
    }

    /** Returns each term name once, ignoring case, in file order. */
    private static List<String> findDistinctTermNames(List<Term> terms) {
        List<String> names = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (Term term : terms) {
            if (seen.add(term.getTerm().toLowerCase())) {
                names.add(term.getTerm());
            }
        }
        return names;
    }

    /** Returns the terms whose definitions give at least two keywords. */
    private static List<Term> findReasoningTerms(List<Term> terms) {
        List<Term> usable = new ArrayList<>();
        for (Term term : terms) {
            if (extractKeywords(term).size() >= MIN_KEYWORDS) {
                usable.add(term);
            }
        }
        return usable;
    }
}
