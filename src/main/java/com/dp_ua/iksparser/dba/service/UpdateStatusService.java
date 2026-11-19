package com.dp_ua.iksparser.dba.service;

import java.util.List;

import org.springframework.stereotype.Component;

import com.dp_ua.iksparser.dba.entity.UpdateStatusEntity;
import com.dp_ua.iksparser.dba.repo.UpdateStatusRepo;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Component
@Transactional
@RequiredArgsConstructor
public class UpdateStatusService {

    private final UpdateStatusRepo repo;


    public void save(UpdateStatusEntity message) {
        repo.save(message);
    }

    public List<UpdateStatusEntity> findAllByCompetitionIdAndStatus(long competitionId, String name) {
        return repo.findAllByCompetitionIdAndStatus(competitionId, name);
    }

    public void saveAll(List<UpdateStatusEntity> allByCompetitionId) {
        repo.saveAll(allByCompetitionId);
    }

    public void flush() {
        repo.flush();
    }

}
