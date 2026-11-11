package com.neurolink.cosmicquest.ai;

import java.util.List;
import java.util.Map;

/**
 * Writes the instructions sent to every provider. The prompt asks for JSON
 * only, in one fixed shape, so {@link AiQuestionParser} can read the reply.
 */
final class PromptBuilder {

    private PromptBuilder() {
    }

    /**
     * Builds the prompt for a request.
     *
     * @param request what to ask for.
     * @return the full prompt text.
     */
    static String build(AiRequest request) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("You write study questions for a quiz game. Use ONLY the lesson below; ")
                .append("do not add facts from outside it.\n")
                .append("Write exactly ").append(request.getCount()).append(' ')
                .append(label(request.getKind())).append(" questions.\n")
                .append(rules(request.getKind()))
                .append("Reply with JSON only, no other text, in this shape:\n")
                .append("{\"questions\": [").append(shape(request.getKind())).append(", ...]}\n\n")
                .append("LESSON\n");
        if (!request.getTerms().isEmpty()) {
            prompt.append("Terms:\n");
            for (Map.Entry<String, String> term : request.getTerms().entrySet()) {
                prompt.append("- ").append(term.getKey()).append(": ").append(term.getValue()).append('\n');
            }
        }
        if (!request.getLists().isEmpty()) {
            prompt.append("Lists:\n");
            for (Map.Entry<String, List<String>> list : request.getLists().entrySet()) {
                prompt.append("- ").append(list.getKey()).append(": ")
                        .append(String.join(", ", list.getValue())).append('\n');
            }
        }
        return prompt.toString();
    }

    private static String label(AiQuestionKind kind) {
        switch (kind) {
            case IDENTIFICATION:
                return "identification";
            case MULTIPLE_CHOICE:
                return "multiple choice";
            case ENUMERATION:
                return "enumeration";
            default:
                return "reasoning";
        }
    }

    private static String rules(AiQuestionKind kind) {
        switch (kind) {
            case IDENTIFICATION:
                return "Each question describes one term in new words, without using the term itself. "
                        + "The answer is the term, spelled exactly as in the lesson.\n";
            case MULTIPLE_CHOICE:
                return "Each question has exactly 4 different options. The wrong options are other terms "
                        + "from the lesson. The answer is the right option, copied exactly.\n";
            case ENUMERATION:
                return "Each question asks the player to name every item of one list. "
                        + "The items are copied exactly from that list.\n";
            default:
                return "Each question asks why or how, about one term. The answer is a model answer of "
                        + "1 to 2 sentences. Keywords are 2 to 5 single words from that answer that a "
                        + "good explanation must use.\n";
        }
    }

    private static String shape(AiQuestionKind kind) {
        switch (kind) {
            case IDENTIFICATION:
                return "{\"text\": \"...\", \"answer\": \"...\"}";
            case MULTIPLE_CHOICE:
                return "{\"text\": \"...\", \"options\": [\"...\", \"...\", \"...\", \"...\"], \"answer\": \"...\"}";
            case ENUMERATION:
                return "{\"text\": \"...\", \"items\": [\"...\", \"...\"]}";
            default:
                return "{\"text\": \"...\", \"answer\": \"...\", \"keywords\": [\"...\", \"...\"]}";
        }
    }
}
