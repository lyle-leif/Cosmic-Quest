package com.neurolink.cosmicquest.ai;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.List;
import java.util.Map;

/** Shared lesson and replies for the ai tests. */
final class AiTestData {

    static final String QUESTIONS_JSON =
            "{\"questions\": [{\"text\": \"A blueprint for objects.\", \"answer\": \"Class\"},"
            + " {\"text\": \"An instance of a class.\", \"answer\": \"Object\"}]}";

    private AiTestData() {
    }

    static AiRequest request(AiQuestionKind kind, int count) {
        return new AiRequest(
                Map.of("Class", "A blueprint that describes fields and methods."),
                Map.of("Pillars of OOP", List.of("Encapsulation", "Inheritance", "Polymorphism", "Abstraction")),
                kind, count);
    }

    /** A Gemini reply whose text is the given model output. */
    static String geminiReply(String modelText) {
        JsonObject part = new JsonObject();
        part.addProperty("text", modelText);
        JsonArray parts = new JsonArray();
        parts.add(part);
        JsonObject content = new JsonObject();
        content.add("parts", parts);
        JsonObject candidate = new JsonObject();
        candidate.add("content", content);
        JsonArray candidates = new JsonArray();
        candidates.add(candidate);
        JsonObject reply = new JsonObject();
        reply.add("candidates", candidates);
        return reply.toString();
    }

    /** An OpenAI-style reply whose message content is the given model output. */
    static String chatReply(String modelText) {
        JsonObject message = new JsonObject();
        message.addProperty("role", "assistant");
        message.addProperty("content", modelText);
        JsonObject choice = new JsonObject();
        choice.add("message", message);
        JsonArray choices = new JsonArray();
        choices.add(choice);
        JsonObject reply = new JsonObject();
        reply.add("choices", choices);
        return reply.toString();
    }
}
