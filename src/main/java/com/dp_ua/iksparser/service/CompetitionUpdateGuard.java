package com.dp_ua.iksparser.service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

/**
 * Prevents running (or queueing) several updates of the same competition at once.
 */
@Component
public class CompetitionUpdateGuard {

    private final Set<Long> updating = ConcurrentHashMap.newKeySet();

    /**
     * @return true if the update was registered, false if this competition is already being updated
     */
    public boolean tryStart(long competitionId) {
        return updating.add(competitionId);
    }

    public void finish(long competitionId) {
        updating.remove(competitionId);
    }

}
