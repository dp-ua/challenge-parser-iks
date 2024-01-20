package com.dp_ua.iksparser.dba.service;

import com.dp_ua.iksparser.dba.element.UpdateStatusEntity;
import com.dp_ua.iksparser.dba.repo.UpdateStatusRepo;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Transactional
public class UpdateStatusService {
    private final UpdateStatusRepo repo;

    @Autowired
    public UpdateStatusService(UpdateStatusRepo repo) {
        this.repo = repo;
    }

    public void save(UpdateStatusEntity message) {
        repo.save(message);
    }

    public List<UpdateStatusEntity> findAllByCompetitionIdAndStatus(long competitionId, String name) {
        return repo.findAllByCompetitionIdAndStatus(competitionId, name);
    }
}
