package com.dp_ua.iksparser.dba.service;

import com.dp_ua.iksparser.dba.repo.SubscriberRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SubscriberService {
    private final SubscriberRepo repo;

    @Autowired
    public SubscriberService(SubscriberRepo repo) {
        this.repo = repo;
    }

}
