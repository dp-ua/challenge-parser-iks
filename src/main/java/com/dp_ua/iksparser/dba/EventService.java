package com.dp_ua.iksparser.dba;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EventService {
    private final EventRepo repo;

    @Autowired
    public EventService(EventRepo repo) {
        this.repo = repo;
    }
}
