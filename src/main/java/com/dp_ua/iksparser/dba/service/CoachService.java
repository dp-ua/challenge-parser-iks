package com.dp_ua.iksparser.dba.service;

import com.dp_ua.iksparser.dba.element.CoachEntity;
import com.dp_ua.iksparser.dba.repo.CoachRepo;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Transactional
public class CoachService {
    private final CoachRepo repo;

    @Autowired
    public CoachService(CoachRepo repo) {
        this.repo = repo;
    }

    public CoachEntity save(CoachEntity coach) {
        return repo.save(coach);
    }

    public CoachEntity saveOrUpdate(String name) {
        CoachEntity existingCoach = repo.findByName(name);
        if (existingCoach == null) {
            CoachEntity coach = new CoachEntity();
            coach.setName(name);
            return repo.save(coach);
        } else {
            return existingCoach;
        }
    }
}
