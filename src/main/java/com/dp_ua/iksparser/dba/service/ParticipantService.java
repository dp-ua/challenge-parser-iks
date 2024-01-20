package com.dp_ua.iksparser.dba.service;

import com.dp_ua.iksparser.dba.element.ParticipantEntity;
import com.dp_ua.iksparser.dba.repo.ParticipantRepo;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Transactional
public class ParticipantService {
    private final ParticipantRepo repo;

    @Autowired
    public ParticipantService(ParticipantRepo repo) {
        this.repo = repo;
    }

    public ParticipantEntity save(ParticipantEntity participant) {
        return repo.save(participant);
    }

    public ParticipantEntity saveOrUpdate(ParticipantEntity participant) {
        ParticipantEntity existingParticipant = repo.findByUrl(participant.getUrl());
        if (existingParticipant == null) {
            return repo.save(participant);
        } else {
            existingParticipant.setSurname(participant.getSurname());
            existingParticipant.setName(participant.getName());
            existingParticipant.setTeam(participant.getTeam());
            existingParticipant.setRegion(participant.getRegion());
            existingParticipant.setBorn(participant.getBorn());
            return repo.save(existingParticipant);
        }
    }
}
