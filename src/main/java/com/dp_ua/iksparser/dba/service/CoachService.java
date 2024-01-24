package com.dp_ua.iksparser.dba.service;

import com.dp_ua.iksparser.dba.element.CoachEntity;
import com.dp_ua.iksparser.dba.repo.CoachRepo;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Transactional
@Slf4j
public class CoachService {
    private final CoachRepo repo;

    @Autowired
    public CoachService(CoachRepo repo) {
        this.repo = repo;
    }

    @Transactional
    public CoachEntity save(CoachEntity coach) {
        return repo.save(coach);
    }

    public CoachEntity findByName(String name) {
        List<CoachEntity> coaches = repo.findAllByName(name);
        if (coaches.isEmpty()) {
            return null;
        }
        if (coaches.size() > 1) {
            log.warn("Found {} coaches with name {}", coaches.size(), name);
        }
        return coaches.get(0);
    }

    public List<CoachEntity> searchByNamePartialMatch(String partialName) {
        return repo.findByNameContaining(partialName);
    }
}
