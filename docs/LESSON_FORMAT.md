# Lesson File Format

A lesson file is a text file that contains terms and their definitions.
Cosmic Quest reads it and turns each line into a quiz question.

## Term lines

Each term goes on its own line, written like this:

    Term :: Definition

The part before `::` is the term, and the part after it is the definition.
Spaces around `::`  are optional.

Example:

    Encapsulation :: A programming concept that bundles data and methods into a single unit.

## Comments and blank lines

A line that starts with # is a comment and the game ignores it.
A blank line is ignored.

## List lines

Some lessons have a list of items, like the pillars of OOP. Write these as:

    [ENUM] Title :: item1, item2, item3

The items are separated by commas.

Example:

    [ENUM] Pillars of OOP :: encapsulation, inheritance, polymorphism, abstraction

## Errors

A line that has no :: is invalid. The game reports it with its line number.