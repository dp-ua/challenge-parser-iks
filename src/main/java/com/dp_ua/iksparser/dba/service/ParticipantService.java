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

    // find by this fields
//    private String surname;
//    private String name;
//    private String team;
//    private String region;
//    private String born;
    public ParticipantEntity findBySurnameAndNameAndTeamAndRegionAndBorn(
            String surname,
            String name,
            String team,
            String region,
            String born
    ) {
        return repo.findBySurnameAndNameAndTeamAndRegionAndBorn(
                surname,
                name,
                team,
                region,
                born
        );
    }
//    public ParticipantEntity findByUrl(String url) {
//        return repo.findByUrl(url);
//    }
}
