package com.neurolink.cosmicquest.logic;
 
/**
 * One term from a lesson file and its definition, such as
 * "Class :: A blueprint that describes the fields and methods of an object."
 */
public class Term {
 
    private final String term;
    private final String definition;
 
    /**
     * Creates a term with its definition.
     *
     * @param term the word or phrase being defined.
     * @param definition what the term means.
     */
    public Term(String term, String definition) {
        this.term = term;
        this.definition = definition;
    }
 
    /** Returns the word or phrase being defined. */
    public String getTerm() {
        return term;
    }
 
    /** Returns what the term means. */
    public String getDefinition() {
        return definition;
    }
}
