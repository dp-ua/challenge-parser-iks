package com.dp_ua.iksparser.dba;

import com.dp_ua.iksparser.element.DayEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DayService {
    private final DayRepo repo;

    @Autowired
    public DayService(DayRepo repo) {
        this.repo = repo;
    }

    public DayEntity save(DayEntity day) {
        return repo.save(day);
    }
}
