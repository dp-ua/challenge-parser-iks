package com.dp_ua.iksparser.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CompetitionUpdateQueueTest {

    /** Collects tasks instead of running them, so a test decides when "the worker thread" runs them. */
    private final Queue<Runnable> tasks = new ArrayDeque<>();
    private final DataUpdateService updater = mock(DataUpdateService.class);
    private CompetitionUpdateQueue queue;

    @BeforeEach
    void setUp() {
        Executor executor = tasks::add;
        queue = new CompetitionUpdateQueue(executor, updater);
    }

    private void runAllTasks() {
        Runnable task;
        while ((task = tasks.poll()) != null) {
            task.run();
        }
    }

    @Test
    void request_runsUpdateOnce() {
        assertTrue(queue.requestUpdate(1L));

        verify(updater, times(0)).update(anyLong());
        runAllTasks();
        verify(updater, times(1)).update(1L);
    }

    @Test
    void request_sameCompetitionWhilePending_isIgnored() {
        assertTrue(queue.requestUpdate(1L));
        assertFalse(queue.requestUpdate(1L));
        assertFalse(queue.requestUpdate(1L));

        assertEquals(1, tasks.size());
        runAllTasks();
        verify(updater, times(1)).update(1L);
    }

    @Test
    void request_differentCompetitionsAreAllQueued() {
        assertTrue(queue.requestUpdate(1L));
        assertTrue(queue.requestUpdate(2L));

        runAllTasks();
        verify(updater).update(1L);
        verify(updater).update(2L);
    }

    @Test
    void request_afterFinished_isAcceptedAgain() {
        queue.requestUpdate(1L);
        runAllTasks();

        assertTrue(queue.requestUpdate(1L));
        runAllTasks();
        verify(updater, times(2)).update(1L);
    }

    @Test
    void request_afterFailedUpdate_isAcceptedAgain() {
        doThrow(new IllegalStateException("boom")).when(updater).update(1L);
        queue.requestUpdate(1L);
        try {
            runAllTasks();
        } catch (IllegalStateException ignored) {
            // the exception must not leave the competition stuck as "pending"
        }

        assertTrue(queue.requestUpdate(1L));
    }

    @Test
    void request_whenExecutorRejects_isAcceptedAgainLater() {
        Executor rejecting = task -> {
            throw new RejectedExecutionException("full");
        };
        CompetitionUpdateQueue rejectingQueue = new CompetitionUpdateQueue(rejecting, updater);

        assertFalse(rejectingQueue.requestUpdate(1L));
        assertFalse(rejectingQueue.requestUpdate(1L));
    }
}
