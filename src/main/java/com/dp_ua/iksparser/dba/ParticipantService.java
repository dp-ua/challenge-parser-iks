package com.dp_ua.iksparser.dba;

import com.dp_ua.iksparser.element.ParticipantEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
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
