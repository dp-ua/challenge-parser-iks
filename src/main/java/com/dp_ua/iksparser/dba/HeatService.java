package com.dp_ua.iksparser.dba;

import com.dp_ua.iksparser.element.HeatEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class HeatService {
    private final HeatRepo repo;

    @Autowired
    public HeatService(HeatRepo repo) {
        this.repo = repo;
    }

    public HeatEntity save(HeatEntity heat) {
        return repo.save(heat);
    }
}
