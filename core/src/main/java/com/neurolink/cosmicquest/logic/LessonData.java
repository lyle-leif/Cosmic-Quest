package com.neurolink.cosmicquest.logic;
 
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
 
/**
 * Everything read from one lesson file: its terms and its lists.
 * The data cannot be changed after it is created.
 */
public class LessonData {
 
    private final List<Term> terms;
    private final Map<String, List<String>> enumerations;
 
    /**
     * Creates the lesson data and keeps read-only copies of what it is given.
     *
     * @param terms the terms in file order.
     * @param enumerations each list title with its items, in file order.
     */
    public LessonData(List<Term> terms, Map<String, List<String>> enumerations) {
        this.terms = Collections.unmodifiableList(new ArrayList<>(terms));
        Map<String, List<String>> copy = new LinkedHashMap<>();
        for (Map.Entry<String, List<String>> entry : enumerations.entrySet()) {
            copy.put(entry.getKey(), Collections.unmodifiableList(new ArrayList<>(entry.getValue())));
        }
        this.enumerations = Collections.unmodifiableMap(copy);
    }
 
    /** Returns the terms, in the order they appear in the file. */
    public List<Term> getTerms() {
        return terms;
    }
 
    /** Returns each list title with its items, in the order they appear in the file. */
    public Map<String, List<String>> getEnumerations() {
        return enumerations;
    }
}