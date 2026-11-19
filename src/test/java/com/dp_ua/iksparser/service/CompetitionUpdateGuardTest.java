package com.dp_ua.iksparser.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

class CompetitionUpdateGuardTest {

    private final CompetitionUpdateGuard guard = new CompetitionUpdateGuard();

    @Test
    void tryStart_secondAttemptForSameCompetitionIsRejected() {
        assertTrue(guard.tryStart(1L));
        assertFalse(guard.tryStart(1L));
    }

    @Test
    void tryStart_differentCompetitionsDoNotInterfere() {
        assertTrue(guard.tryStart(1L));
        assertTrue(guard.tryStart(2L));
    }

    @Test
    void finish_allowsStartingAgain() {
        guard.tryStart(1L);
        guard.finish(1L);
        assertTrue(guard.tryStart(1L));
    }

    @Test
    void finish_forUnknownCompetitionIsHarmless() {
        guard.finish(42L);
        assertTrue(guard.tryStart(42L));
    }

    @Test
    void tryStart_isAtomicUnderConcurrency() throws Exception {
        int threads = 16;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch ready = new CountDownLatch(threads);
        CountDownLatch go = new CountDownLatch(1);
        AtomicInteger winners = new AtomicInteger();
        for (int i = 0; i < threads; i++) {
            pool.submit(() -> {
                ready.countDown();
                go.await();
                if (guard.tryStart(7L)) {
                    winners.incrementAndGet();
                }
                return null;
            });
        }
        ready.await();
        go.countDown();
        pool.shutdown();
        assertTrue(pool.awaitTermination(5, TimeUnit.SECONDS));
        assertEquals(1, winners.get());
    }

}
