package com.neurolink.cosmicquest.ai;

import java.util.ArrayList;
import java.util.List;

/**
 * Tries providers in order and returns the first one that writes questions.
 * If every provider fails, the error lists each reason, and the game falls
 * back to local questions.
 */
public class AiChain implements AiClient {

    private final List<AiClient> clients;
    private String lastSource;

    /**
     * Creates a chain.
     *
     * @param clients the providers, first choice first.
     * @throws IllegalArgumentException if the list is null, empty or holds null.
     */
    public AiChain(List<AiClient> clients) {
        if (clients == null || clients.isEmpty()) {
            throw new IllegalArgumentException("The chain needs at least one provider.");
        }
        // A loop, not contains(null): List.of(...).contains(null) throws instead of returning false.
        for (AiClient client : clients) {
            if (client == null) {
                throw new IllegalArgumentException("The chain must not hold null providers.");
            }
        }
        this.clients = List.copyOf(clients);
    }

    /** Returns the providers' names joined, such as "Gemini, Groq". */
    @Override
    public String getName() {
        List<String> names = new ArrayList<>();
        for (AiClient client : clients) {
            names.add(client.getName());
        }
        return String.join(", ", names);
    }

    @Override
    public List<AiQuestion> generate(AiRequest request) throws AiException {
        List<String> reasons = new ArrayList<>();
        for (AiClient client : clients) {
            try {
                List<AiQuestion> questions = client.generate(request);
                if (questions != null && !questions.isEmpty()) {
                    lastSource = client.getName();
                    return questions;
                }
                reasons.add(client.getName() + " wrote no questions.");
            } catch (AiException e) {
                reasons.add(e.getMessage());
            }
        }
        lastSource = null;
        throw new AiException("No AI provider answered: " + String.join(" ", reasons));
    }

    /** Returns the name of the provider that answered last, or null if none did. */
    public String getLastSource() {
        return lastSource;
    }
}
