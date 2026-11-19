package com.dp_ua.iksparser.service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * Entry point for requesting a competition update (cron, admin REST).
 * Updates are executed one by one by the single-threaded {@code updateExecutor}.
 * A competition that is already waiting in the queue or being updated is not queued again.
 */
@Slf4j
@Component
public class CompetitionUpdateQueue {

    private final Executor executor;
    private final DataUpdateService updater;
    private final Set<Long> pending = ConcurrentHashMap.newKeySet();

    public CompetitionUpdateQueue(@Qualifier("updateExecutor") Executor executor, DataUpdateService updater) {
        this.executor = executor;
        this.updater = updater;
    }

    /**
     * @return true if the update was queued, false if it was skipped (already pending or queue is full)
     */
    public boolean requestUpdate(long competitionId) {
        if (!pending.add(competitionId)) {
            log.debug("[{}] Update already queued or running, skip", competitionId);
            return false;
        }
        try {
            executor.execute(() -> {
                try {
                    updater.update(competitionId);
                } finally {
                    pending.remove(competitionId);
                }
            });
            return true;
        } catch (RejectedExecutionException e) {
            pending.remove(competitionId);
            log.error("[{}] Update queue is full, update skipped", competitionId, e);
            return false;
        }
    }

}
