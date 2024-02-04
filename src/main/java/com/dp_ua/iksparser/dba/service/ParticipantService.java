package com.dp_ua.iksparser.dba.service;

import com.dp_ua.iksparser.dba.element.ParticipantEntity;
import com.dp_ua.iksparser.dba.repo.ParticipantRepo;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@Transactional
@Slf4j
public class ParticipantService {
    private final ParticipantRepo repo;

    @Autowired
    public ParticipantService(ParticipantRepo repo) {
        this.repo = repo;
    }

    @Transactional
    public ParticipantEntity save(ParticipantEntity participant) {
        return repo.save(participant);
    }

    public ParticipantEntity findBySurnameAndNameAndTeamAndRegionAndBorn(
            String surname,
            String name,
            String team,
            String region,
            String born
    ) {
        List<ParticipantEntity> participants = repo.findAllBySurnameAndNameAndTeamAndRegionAndBorn(
                surname,
                name,
                team,
                region,
                born
        );
        if (participants.isEmpty()) {
            return null;
        }
        if (participants.size() > 1) {
            log.warn("Found " + participants.size() + " participants with surname " + surname + " name " + name + " team " + team + " region " + region + " born " + born);
        }
        return participants.get(0);
    }

    public Optional<ParticipantEntity> findById(long commandArgument) {
        return repo.findById(commandArgument);
    }
}
