package com.neurolink.cosmicquest.ai;

import java.util.List;

/**
 * One question as the AI wrote it: plain data, not yet checked. The game
 * checks it (for example, exactly 4 options) and turns it into one of its
 * own question classes, or drops it.
 */
public final class AiQuestion {

    private final AiQuestionKind kind;
    private final String text;
    private final String answer;
    private final List<String> options;
    private final List<String> items;
    private final List<String> keywords;

    /**
     * Creates a question. Missing text becomes "", missing lists become empty.
     *
     * @param kind the kind of question.
     * @param text what the player is asked.
     * @param answer the right answer (identification, multiple choice, reasoning).
     * @param options the options (multiple choice only).
     * @param items the items to list (enumeration only).
     * @param keywords the words a good explanation uses (reasoning only).
     * @throws IllegalArgumentException if kind is null.
     */
    public AiQuestion(AiQuestionKind kind, String text, String answer,
            List<String> options, List<String> items, List<String> keywords) {
        if (kind == null) {
            throw new IllegalArgumentException("Kind must not be null.");
        }
        this.kind = kind;
        this.text = text == null ? "" : text.strip();
        this.answer = answer == null ? "" : answer.strip();
        this.options = copy(options);
        this.items = copy(items);
        this.keywords = copy(keywords);
    }

    private static List<String> copy(List<String> values) {
        if (values == null) {
            return List.of();
        }
        return values.stream().filter(v -> v != null).map(String::strip).toList();
    }

    /** Returns the kind of question. */
    public AiQuestionKind getKind() {
        return kind;
    }

    /** Returns what the player is asked; "" if missing. */
    public String getText() {
        return text;
    }

    /** Returns the right answer; "" if missing. */
    public String getAnswer() {
        return answer;
    }

    /** Returns the options; empty if none. The list cannot be changed. */
    public List<String> getOptions() {
        return options;
    }

    /** Returns the items to list; empty if none. The list cannot be changed. */
    public List<String> getItems() {
        return items;
    }

    /** Returns the keywords; empty if none. The list cannot be changed. */
    public List<String> getKeywords() {
        return keywords;
    }
}
