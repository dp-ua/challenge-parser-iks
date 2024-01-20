package com.dp_ua.iksparser.dba.service;

import com.dp_ua.iksparser.dba.element.EventEntity;
import com.dp_ua.iksparser.dba.repo.EventRepo;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Transactional
public class EventService {
    private final EventRepo repo;

    @Autowired
    public EventService(EventRepo repo) {
        this.repo = repo;
    }

    public EventEntity save(EventEntity event) {
        return repo.save(event);
    }
}