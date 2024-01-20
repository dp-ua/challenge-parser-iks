package com.dp_ua.iksparser.dba.service;

import com.dp_ua.iksparser.dba.element.HeatLineEntity;
import com.dp_ua.iksparser.dba.repo.HeatLineRepo;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Transactional
public class HeatLineService {
    private final HeatLineRepo repo;

    @Autowired
    public HeatLineService(HeatLineRepo repo) {
        this.repo = repo;
    }

    public HeatLineEntity save(HeatLineEntity heatLineEntity) {
        return repo.save(heatLineEntity);
    }
}
