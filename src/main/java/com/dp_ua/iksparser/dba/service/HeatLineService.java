package com.dp_ua.iksparser.dba.service;

import com.dp_ua.iksparser.dba.dto.HeatLineDto;
import com.dp_ua.iksparser.dba.entity.CoachEntity;
import com.dp_ua.iksparser.dba.entity.HeatLineEntity;
import com.dp_ua.iksparser.dba.repo.HeatLineRepo;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Transactional
public class HeatLineService {
    private final HeatLineRepo repo;

    @Autowired
    public HeatLineService(HeatLineRepo repo) {
        this.repo = repo;
    }

    @Transactional
    public HeatLineEntity save(HeatLineEntity heatLineEntity) {
        return repo.save(heatLineEntity);
    }

    @Transactional
    public HeatLineEntity findById(Long id) {
        return repo.findById(id).orElse(null);
    }

    public HeatLineDto convertToDto(HeatLineEntity heatLineEntity) {
        HeatLineDto dto = new HeatLineDto();
        dto.setId(heatLineEntity.getId());
        dto.setLane(heatLineEntity.getLane());
        dto.setBib(heatLineEntity.getBib());
        dto.setParticipant(heatLineEntity.getParticipant());
        dto.setCoaches(heatLineEntity.getCoaches().stream().map(CoachEntity::getId).toList());
        return dto;
    }

    public List<HeatLineEntity> getHeatLinesInCompetitionWhereCoachIs(Long competitionId, Long coachId) {
        return repo.getHeatLinesInCompetitionWhereCoachIs(competitionId, coachId);
    }

    public List<HeatLineEntity> getHeatLinesInCompetitionByBib(Long competitionId, String bib) {
        return repo.getHeatLinesInCompetitionByBib(competitionId, bib);
    }

    public List<HeatLineEntity> getHeatLinesInCompetitionByParticipantSurname(Long competitionId, String name) {
        return repo.getHeatLinesInCompetitionByParticipantSurname(competitionId, name);
    }

    public List<HeatLineEntity> getHeatLinesInCompetitionByParticipantId(Long competitionId, Long participantId) {
        return repo.getHeatLinesInCompetitionByParticipantId(competitionId, participantId);
    }
}
