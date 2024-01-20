package com.dp_ua.iksparser.dba;

import com.dp_ua.iksparser.dba.element.CoachEntity;
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
}
