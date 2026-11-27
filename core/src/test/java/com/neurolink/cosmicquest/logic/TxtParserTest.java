package com.neurolink.cosmicquest.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Lessons are written to a temporary folder in each test, because tests run
 * with core/ as the working folder and cannot see assets/.
 */
class TxtParserTest {

    @TempDir
    Path tempDir;

    private final TxtParser parser = new TxtParser();

    private Path writeLesson(String text) throws IOException {
        return Files.writeString(tempDir.resolve("lesson.txt"), text);
    }

    @Test
    void readsTermLine() throws Exception {
        LessonData data = parser.parseFile(writeLesson("Class :: A blueprint for objects.\n"));
        assertEquals(1, data.getTerms().size());
        assertEquals("Class", data.getTerms().get(0).getTerm());
        assertEquals("A blueprint for objects.", data.getTerms().get(0).getDefinition());
    }

    @Test
    void readsEnumLine() throws Exception {
        LessonData data = parser.parseFile(writeLesson("[ENUM] Pillars :: Encapsulation , Inheritance,Abstraction\n"));
        assertEquals(List.of("Encapsulation", "Inheritance", "Abstraction"),
                data.getEnumerations().get("Pillars"));
        assertTrue(data.getTerms().isEmpty());
    }

    @Test
    void skipsCommentsAndBlankLines() throws Exception {
        LessonData data = parser.parseFile(writeLesson("# a comment\n\n   \nField :: Stores data.\n"));
        assertEquals(1, data.getTerms().size());
    }

    @Test
    void splitsAtFirstDelimiterOnly() throws Exception {
        LessonData data = parser.parseFile(writeLesson("Scope :: Written as A :: B in notes.\n"));
        assertEquals("Written as A :: B in notes.", data.getTerms().get(0).getDefinition());
    }

    @Test
    void keepsFileOrder() throws Exception {
        LessonData data = parser.parseFile(writeLesson("B :: second letter\nA :: first letter\n"));
        assertEquals("B", data.getTerms().get(0).getTerm());
        assertEquals("A", data.getTerms().get(1).getTerm());
    }

    @Test
    void missingDelimiterReportsLineNumber() throws Exception {
        Path file = writeLesson("# comment\nClass :: fine\nThis line is broken\n");
        InvalidLessonFormatException e =
                assertThrows(InvalidLessonFormatException.class, () -> parser.parseFile(file));
        assertEquals(3, e.getLineNumber());
        assertTrue(e.getMessage().startsWith("Line 3: "));
    }

    @Test
    void rejectsEmptyTermOrDefinition() throws Exception {
        Path noTerm = writeLesson(" :: a definition\n");
        assertThrows(InvalidLessonFormatException.class, () -> parser.parseFile(noTerm));
        Path noDefinition = writeLesson("Class ::   \n");
        assertThrows(InvalidLessonFormatException.class, () -> parser.parseFile(noDefinition));
    }

    @Test
    void rejectsEmptyListItem() throws Exception {
        Path file = writeLesson("[ENUM] Pillars :: a, , c\n");
        InvalidLessonFormatException e =
                assertThrows(InvalidLessonFormatException.class, () -> parser.parseFile(file));
        assertEquals(1, e.getLineNumber());
    }

    @Test
    void missingFileThrowsIoException() {
        assertThrows(NoSuchFileException.class, () -> parser.parseFile(tempDir.resolve("nope.txt")));
    }

    @Test
    void lessonDataCannotBeChanged() throws Exception {
        LessonData data = parser.parseFile(writeLesson("Class :: A blueprint.\n[ENUM] L :: a, b\n"));
        assertThrows(UnsupportedOperationException.class, () -> data.getTerms().clear());
        assertThrows(UnsupportedOperationException.class, () -> data.getEnumerations().get("L").add("c"));
    }
}
