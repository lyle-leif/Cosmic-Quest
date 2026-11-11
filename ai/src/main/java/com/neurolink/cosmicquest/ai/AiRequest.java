package com.neurolink.cosmicquest.ai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * What the game asks the AI for: questions of one kind, written only from
 * the lesson's own terms and lists.
 */
public final class AiRequest {

    /** Most questions one request may ask for, to keep replies small. */
    public static final int MAX_COUNT = 20;

    private final Map<String, String> terms;
    private final Map<String, List<String>> lists;
    private final AiQuestionKind kind;
    private final int count;

    /**
     * Creates a request.
     *
     * @param terms the lesson's terms and definitions, in lesson order.
     * @param lists the lesson's enumeration lists, by title.
     * @param kind the kind of question wanted.
     * @param count how many questions, 1 to {@link #MAX_COUNT}.
     * @throws IllegalArgumentException if an argument is null, the lesson is
     *         empty, or the count is out of range.
     */
    public AiRequest(Map<String, String> terms, Map<String, List<String>> lists, AiQuestionKind kind, int count) {
        if (terms == null || lists == null || kind == null) {
            throw new IllegalArgumentException("Terms, lists and kind must not be null.");
        }
        if (terms.isEmpty() && lists.isEmpty()) {
            throw new IllegalArgumentException("The lesson has no terms or lists.");
        }
        if (count < 1 || count > MAX_COUNT) {
            throw new IllegalArgumentException("Count must be 1 to " + MAX_COUNT + ", got " + count + ".");
        }
        this.terms = Collections.unmodifiableMap(new LinkedHashMap<>(terms));
        Map<String, List<String>> copy = new LinkedHashMap<>();
        for (Map.Entry<String, List<String>> list : lists.entrySet()) {
            copy.put(list.getKey(), List.copyOf(new ArrayList<>(list.getValue())));
        }
        this.lists = Collections.unmodifiableMap(copy);
        this.kind = kind;
        this.count = count;
    }

    /** Returns the terms and definitions. The map cannot be changed. */
    public Map<String, String> getTerms() {
        return terms;
    }

    /** Returns the enumeration lists by title. The map cannot be changed. */
    public Map<String, List<String>> getLists() {
        return lists;
    }

    /** Returns the kind of question wanted. */
    public AiQuestionKind getKind() {
        return kind;
    }

    /** Returns how many questions are wanted. */
    public int getCount() {
        return count;
    }
}
