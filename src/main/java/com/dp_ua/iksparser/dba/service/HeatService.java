package com.dp_ua.iksparser.dba.service;

import com.dp_ua.iksparser.dba.element.HeatEntity;
import com.dp_ua.iksparser.dba.repo.HeatRepo;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Transactional
public class HeatService {
    private final HeatRepo repo;

    @Autowired
    public HeatService(HeatRepo repo) {
        this.repo = repo;
    }

    public HeatEntity save(HeatEntity heat) {
        return repo.save(heat);
    }

    public void delete(HeatEntity heat) {
        repo.delete(heat);
    }
}
