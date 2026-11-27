package com.neurolink.cosmicquest.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.neurolink.cosmicquest.model.IdentificationQuestion;
import com.neurolink.cosmicquest.model.Player;
import com.neurolink.cosmicquest.model.Question;
import com.neurolink.cosmicquest.model.QuestionType;
import com.neurolink.cosmicquest.model.Team;
import com.neurolink.cosmicquest.model.VoidBrute;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class BattleStartupTest {

    private static BattleMode createBattle(boolean squad, QuestionProvider provider) {
        if (!squad) {
            return new SoloBossBattle(new Player("Pilot"), new VoidBrute(3), provider, new Random(42));
        }
        Team team = new Team("Squad");
        team.addPlayer(new Player("Pilot A"));
        team.addPlayer(new Player("Pilot B"));
        return new SquadBossBattle(team, new VoidBrute(3), provider, new Random(42));
    }

    private static class WaitingProvider implements QuestionProvider {
        final AtomicInteger requests = new AtomicInteger();
        final CountDownLatch requested = new CountDownLatch(1);
        final CountDownLatch release = new CountDownLatch(1);
        boolean fail;

        @Override
        public boolean supports(QuestionType type) {
            return type == QuestionType.IDENTIFICATION;
        }

        @Override
        public List<Question> provide(QuestionType type, int count) {
            requests.incrementAndGet();
            requested.countDown();
            try {
                if (!release.await(5, TimeUnit.SECONDS)) {
                    throw new IllegalStateException("Provider was not released.");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException(e);
            }
            if (fail) {
                throw new IllegalStateException("Question loading failed.");
            }
            return List.of(new IdentificationQuestion("An object blueprint", "Class", 2));
        }

        @Override
        public String getStatus() {
            return "Test provider";
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void firstRequestWaitsForBackgroundStartup(boolean squad) throws Exception {
        WaitingProvider provider = new WaitingProvider();
        BattleMode battle = createBattle(squad, provider);
        assertEquals(0, provider.requests.get(), "Constructing a battle must not fetch questions");

        try (var worker = Executors.newSingleThreadExecutor()) {
            var started = worker.submit(battle::startBattle);
            try {
                assertTrue(provider.requested.await(5, TimeUnit.SECONDS));
                assertFalse(started.isDone(), "The provider is still waiting on the worker");
            } finally {
                provider.release.countDown();
            }
            started.get(5, TimeUnit.SECONDS);
        }

        assertEquals(1, provider.requests.get());
        assertEquals(1, battle.getCurrentTurn());
        assertNotNull(battle.getCurrentQuestion());
        if (battle instanceof SquadBossBattle teamBattle) {
            assertNotNull(teamBattle.getCurrentPlayer());
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void loadingFailureIsReportedAtStartup(boolean squad) {
        WaitingProvider provider = new WaitingProvider();
        provider.fail = true;
        provider.release.countDown();
        BattleMode battle = createBattle(squad, provider);
        assertEquals(0, provider.requests.get());
        var failure = assertThrows(IllegalStateException.class, battle::startBattle);
        assertEquals("Question loading failed.", failure.getMessage());
    }
}
