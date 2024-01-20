package com.dp_ua.iksparser.dba.service;

import com.dp_ua.iksparser.dba.repo.UpdateStatusRepo;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Transactional
public class UpdateStatusService {
    private final UpdateStatusRepo repo;

    @Autowired
    public UpdateStatusService(UpdateStatusRepo repo) {
        this.repo = repo;
    }
}
