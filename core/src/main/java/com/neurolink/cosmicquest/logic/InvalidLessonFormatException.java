package com.neurolink.cosmicquest.logic;
 
/**
 * Thrown when a line in a lesson file does not follow the lesson format.
 * The message says which line is wrong, for example "Line 4: missing ::".
 */
public class InvalidLessonFormatException extends Exception {
 
    private static final long serialVersionUID = 1L;
 
    private final int lineNumber;
 
    /**
     * Creates the error for one line of the file.
     *
     * @param lineNumber the line that is wrong, counting from 1.
     * @param message what is wrong with the line.
     */
    public InvalidLessonFormatException(int lineNumber, String message) {
        super("Line " + lineNumber + ": " + message);
        this.lineNumber = lineNumber;
    }
 
    /** Returns the line that is wrong, counting from 1. */
    public int getLineNumber() {
        return lineNumber;
    }
}
