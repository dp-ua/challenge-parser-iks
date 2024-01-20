package com.dp_ua.iksparser.dba;

import com.dp_ua.iksparser.dba.element.ParticipantEntity;
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
}
