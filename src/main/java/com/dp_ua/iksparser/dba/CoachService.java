package com.dp_ua.iksparser.dba;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CoachService {
    private final CoachRepo repo;

    @Autowired
    public CoachService(CoachRepo repo) {
        this.repo = repo;
    }
}
