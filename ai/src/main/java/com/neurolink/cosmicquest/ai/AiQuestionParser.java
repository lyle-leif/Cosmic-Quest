package com.neurolink.cosmicquest.ai;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads the questions out of the text a provider wrote. Models sometimes wrap
 * JSON in extra words or ```json fences, so only the part from the first
 * {@code {} to the last {@code }} is read. Entries that are not objects are
 * skipped; checking each question's content is the game's job.
 */
final class AiQuestionParser {

    private AiQuestionParser() {
    }

    /**
     * Parses a reply.
     *
     * @param reply the text the model wrote.
     * @param kind the kind that was asked for; the model's own label is ignored.
     * @param max the most questions to keep.
     * @return the questions found, at most {@code max}.
     * @throws AiException if the text holds no {"questions": [...]} JSON.
     */
    static List<AiQuestion> parse(String reply, AiQuestionKind kind, int max) throws AiException {
        JsonArray entries = questionsArray(reply);
        List<AiQuestion> questions = new ArrayList<>();
        for (JsonElement entry : entries) {
            if (questions.size() >= max) {
                break;
            }
            if (entry.isJsonObject()) {
                JsonObject q = entry.getAsJsonObject();
                questions.add(new AiQuestion(kind, text(q, "text"), text(q, "answer"),
                        texts(q, "options"), texts(q, "items"), texts(q, "keywords")));
            }
        }
        return questions;
    }

    private static JsonArray questionsArray(String reply) throws AiException {
        if (reply == null) {
            throw new AiException("The reply was empty.");
        }
        int start = reply.indexOf('{');
        int end = reply.lastIndexOf('}');
        if (start < 0 || end <= start) {
            throw new AiException("The reply had no JSON object.");
        }
        try {
            JsonElement root = JsonParser.parseString(reply.substring(start, end + 1));
            if (root.isJsonObject() && root.getAsJsonObject().get("questions") instanceof JsonArray array) {
                return array;
            }
        } catch (JsonParseException | IllegalStateException e) {
            throw new AiException("The reply was not valid JSON.", e);
        }
        throw new AiException("The reply had no \"questions\" list.");
    }

    /** Reads a text field; null when missing or not plain text. */
    private static String text(JsonObject object, String field) {
        JsonElement value = object.get(field);
        return value != null && value.isJsonPrimitive() ? value.getAsString() : null;
    }

    /** Reads a list of texts; skips anything that is not plain text. */
    private static List<String> texts(JsonObject object, String field) {
        List<String> values = new ArrayList<>();
        if (object.get(field) instanceof JsonArray array) {
            for (JsonElement value : array) {
                if (value.isJsonPrimitive()) {
                    values.add(value.getAsString());
                }
            }
        }
        return values;
    }
}
