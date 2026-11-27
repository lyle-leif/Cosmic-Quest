package com.neurolink.cosmicquest.logic;

import com.neurolink.cosmicquest.ai.AiChain;
import com.neurolink.cosmicquest.ai.AiClient;
import com.neurolink.cosmicquest.ai.AiException;
import com.neurolink.cosmicquest.ai.AiQuestion;
import com.neurolink.cosmicquest.ai.AiQuestionKind;
import com.neurolink.cosmicquest.ai.AiRequest;
import com.neurolink.cosmicquest.model.EnumerationQuestion;
import com.neurolink.cosmicquest.model.IdentificationQuestion;
import com.neurolink.cosmicquest.model.MultipleChoiceQuestion;
import com.neurolink.cosmicquest.model.Question;
import com.neurolink.cosmicquest.model.QuestionType;
import com.neurolink.cosmicquest.model.ReasoningQuestion;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Asks the AI module for questions, checks every one, and turns the good ones
 * into the game's own question classes.
 *
 * <ul>
 * <li>A question that breaks the game's rules (for example, not exactly 4
 *     options, or the answer written inside the clue) is dropped.</li>
 * <li>Any shortfall is filled with local questions, so a battle always gets
 *     the number it asked for.</li>
 * <li>If the AI fails for any reason (no internet, timeout, HTTP 429, bad
 *     JSON), every question is local and the status becomes
 *     {@value #OFFLINE_STATUS}.</li>
 * </ul>
 */
public class AiQuestionProvider implements QuestionProvider {

    /** The status line when no AI question could be used. */
    public static final String OFFLINE_STATUS = "Offline: using local questions";

    private static final int OPTION_COUNT = 4;
    private static final int MIN_ITEMS = 2;
    private static final int MIN_KEYWORDS = 2;
    private static final int MAX_KEYWORDS = 5;

    private final AiClient client;
    private final Map<String, String> terms = new LinkedHashMap<>();
    private final Map<String, List<String>> lists;
    private final LocalProvider local;
    private String status;
    private String lastProblem;

    /**
     * Prepares the provider.
     *
     * @param client the AI to ask, usually an {@link AiChain}.
     * @param lesson the loaded lesson, sent to the AI as its only source.
     * @param local the fallback for anything the AI does not supply.
     * @throws IllegalArgumentException if an argument is null.
     */
    public AiQuestionProvider(AiClient client, LessonData lesson, LocalProvider local) {
        if (client == null || lesson == null || local == null) {
            throw new IllegalArgumentException("Client, lesson and local provider must not be null.");
        }
        this.client = client;
        for (Term term : lesson.getTerms()) {
            terms.put(term.getTerm(), term.getDefinition());
        }
        this.lists = lesson.getEnumerations();
        this.local = local;
        this.status = "AI questions on (" + client.getName() + ")";
    }

    /** Supports the same types as the lesson does locally, so the fallback always works. */
    @Override
    public boolean supports(QuestionType type) {
        return local.supports(type);
    }

    @Override
    public List<Question> provide(QuestionType type, int count) {
        if (count < 0) {
            throw new IllegalArgumentException("Count must not be negative, got " + count + ".");
        }
        if (!supports(type)) {
            throw new IllegalStateException("This lesson cannot make " + type + " questions.");
        }
        List<Question> questions = new ArrayList<>(count);
        if (count > 0) {
            questions.addAll(askAi(type, count));
        }
        int fromAi = questions.size();
        questions.addAll(local.provide(type, count - fromAi));
        if (count > 0) {
            status = fromAi == 0 ? OFFLINE_STATUS
                    : "AI questions from " + source() + (fromAi < count ? " (topped up with local questions)" : "");
        }
        return questions;
    }

    @Override
    public String getStatus() {
        return status;
    }

    /** Returns why the last AI request gave nothing usable, or null if it worked. */
    public String getLastProblem() {
        return lastProblem;
    }

    /** Asks the AI and keeps only the questions that pass the game's checks. */
    private List<Question> askAi(QuestionType type, int count) {
        List<Question> accepted = new ArrayList<>();
        try {
            AiRequest request = new AiRequest(terms, lists, kind(type), Math.min(count, AiRequest.MAX_COUNT));
            for (AiQuestion written : client.generate(request)) {
                Question question = convert(written, type);
                if (question != null && accepted.size() < count) {
                    accepted.add(question);
                }
            }
            lastProblem = accepted.isEmpty() ? "The AI's questions did not pass the game's checks." : null;
        } catch (AiException | RuntimeException e) {
            lastProblem = e.getMessage();
        }
        return accepted;
    }

    private String source() {
        if (client instanceof AiChain chain && chain.getLastSource() != null) {
            return chain.getLastSource();
        }
        return client.getName();
    }

    private static AiQuestionKind kind(QuestionType type) {
        switch (type) {
            case IDENTIFICATION:
                return AiQuestionKind.IDENTIFICATION;
            case MULTIPLE_CHOICE:
                return AiQuestionKind.MULTIPLE_CHOICE;
            case ENUMERATION:
                return AiQuestionKind.ENUMERATION;
            default:
                return AiQuestionKind.REASONING;
        }
    }

    /**
     * Checks one AI question and builds the game's version of it.
     *
     * @return the question, or null if it breaks a rule.
     */
    static Question convert(AiQuestion written, QuestionType type) {
        String text = written.getText();
        String answer = written.getAnswer();
        if (text.isBlank()) {
            return null;
        }
        try {
            switch (type) {
                case IDENTIFICATION:
                    if (answer.isBlank() || lower(text).contains(lower(answer))) {
                        return null; // An empty answer, or a clue that gives the answer away.
                    }
                    return new IdentificationQuestion(text, answer, QuestionGenerator.IDENTIFICATION_DIFFICULTY);
                case MULTIPLE_CHOICE:
                    return multipleChoice(text, answer, written.getOptions());
                case ENUMERATION:
                    List<String> items = distinct(written.getItems());
                    return items.size() < MIN_ITEMS ? null
                            : new EnumerationQuestion(text, items, QuestionGenerator.ENUMERATION_DIFFICULTY);
                default:
                    List<String> keywords = singleWords(written.getKeywords());
                    if (answer.isBlank() || keywords.size() < MIN_KEYWORDS) {
                        return null;
                    }
                    return new ReasoningQuestion(text, answer, keywords, QuestionGenerator.REASONING_DIFFICULTY);
            }
        } catch (IllegalArgumentException e) {
            return null; // The question class itself refused it.
        }
    }

    /** Exactly 4 different options, and the answer must be one of them. */
    private static Question multipleChoice(String text, String answer, List<String> options) {
        List<String> distinct = distinct(options);
        if (distinct.size() != OPTION_COUNT || options.size() != OPTION_COUNT) {
            return null;
        }
        for (int i = 0; i < OPTION_COUNT; i++) {
            if (distinct.get(i).equalsIgnoreCase(answer)) {
                return new MultipleChoiceQuestion(text, distinct, i, QuestionGenerator.MULTIPLE_CHOICE_DIFFICULTY);
            }
        }
        return null;
    }

    /** Non-blank values, each once (ignoring case), in their first order. */
    private static List<String> distinct(List<String> values) {
        Set<String> seen = new LinkedHashSet<>();
        List<String> kept = new ArrayList<>();
        for (String value : values) {
            if (!value.isBlank() && seen.add(lower(value))) {
                kept.add(value);
            }
        }
        return kept;
    }

    /** Keywords are matched as whole words, so only single words are kept (at most 5). */
    private static List<String> singleWords(List<String> keywords) {
        List<String> kept = new ArrayList<>();
        for (String keyword : distinct(keywords)) {
            if (keyword.matches("[\\p{L}\\p{N}-]+") && kept.size() < MAX_KEYWORDS) {
                kept.add(lower(keyword));
            }
        }
        return kept;
    }

    private static String lower(String value) {
        return value.toLowerCase(Locale.ROOT);
    }
}
