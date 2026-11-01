package com.neurolink.cosmicquest.logic;
 
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
 
/**
 * Reads a lesson file and turns it into {@link LessonData}.
 * The rules it follows are in docs/LESSON_FORMAT.md.
 */
public class TxtParser {
 
    private static final String DELIMITER = "::";
    private static final String ENUM_TAG = "[ENUM]";
    private static final String COMMENT = "#";
    private static final String ITEM_SEPARATOR = ",";
 
    /**
     * Reads a lesson file line by line.
     * Blank lines and lines starting with # are skipped.
     *
     * @param path the lesson file to read.
     * @return the terms and lists found in the file.
     * @throws IOException if the file cannot be read.
     * @throws InvalidLessonFormatException if a line breaks the lesson format.
     */
    public LessonData parseFile(Path path) throws IOException, InvalidLessonFormatException {
        List<String> lines = Files.readAllLines(path);
        List<Term> terms = new ArrayList<>();
        Map<String, List<String>> enumerations = new LinkedHashMap<>();
 
        for (int i = 0; i < lines.size(); i++) {
            int lineNumber = i + 1;
            String line = lines.get(i).trim();
 
            if (line.isEmpty() || line.startsWith(COMMENT)) {
                continue;
            }
 
            int at = line.indexOf(DELIMITER);
            if (at == -1) {
                throw new InvalidLessonFormatException(lineNumber, "missing ::");
            }
 
            String left = line.substring(0, at).trim();
            String right = line.substring(at + DELIMITER.length()).trim();
 
            if (left.startsWith(ENUM_TAG)) {
                String title = left.substring(ENUM_TAG.length()).trim();
                enumerations.put(title, parseItems(title, right, lineNumber));
            } else {
                terms.add(parseTerm(left, right, lineNumber));
            }
        }
        return new LessonData(terms, enumerations);
    }
 
    /**
     * Builds one term, rejecting an empty term or definition.
     *
     * @param term the text before ::.
     * @param definition the text after ::.
     * @param lineNumber the line being read, for the error message.
     * @return the new term.
     * @throws InvalidLessonFormatException if the term or definition is empty.
     */
    private Term parseTerm(String term, String definition, int lineNumber)
            throws InvalidLessonFormatException {
        if (term.isEmpty() || definition.isEmpty()) {
            throw new InvalidLessonFormatException(lineNumber, "empty term or definition");
        }
        return new Term(term, definition);
    }
 
    /**
     * Splits a list's items on commas, rejecting an empty title or item.
     *
     * @param title the list title, after [ENUM].
     * @param itemText the text after ::.
     * @param lineNumber the line being read, for the error message.
     * @return the trimmed items, in order.
     * @throws InvalidLessonFormatException if the title or any item is empty.
     */
    private List<String> parseItems(String title, String itemText, int lineNumber)
            throws InvalidLessonFormatException {
        if (title.isEmpty() || itemText.isEmpty()) {
            throw new InvalidLessonFormatException(lineNumber, "empty list title or items");
        }
        List<String> items = new ArrayList<>();
        for (String part : itemText.split(ITEM_SEPARATOR)) {
            String item = part.trim();
            if (item.isEmpty()) {
                throw new InvalidLessonFormatException(lineNumber, "empty list item");
            }
            items.add(item);
        }
        return items;
    }
}
