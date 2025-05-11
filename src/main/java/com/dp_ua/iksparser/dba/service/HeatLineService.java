package com.dp_ua.iksparser.dba.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.dp_ua.iksparser.dba.dto.HeatLineDto;
import com.dp_ua.iksparser.dba.entity.DomainElement;
import com.dp_ua.iksparser.dba.entity.HeatLineEntity;
import com.dp_ua.iksparser.dba.repo.HeatLineRepo;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Component
@Transactional
@RequiredArgsConstructor
public class HeatLineService {

    private final HeatLineRepo repo;
    private final ParticipantService participantService;

    @Transactional
    public HeatLineEntity save(HeatLineEntity heatLineEntity) {
        return repo.save(heatLineEntity);
    }

    @Transactional
    public Optional<HeatLineEntity> findById(Long id) {
        return repo.findById(id);
    }

    public Iterable<HeatLineEntity> findAll() {
        return repo.findAll();
    }

    public HeatLineDto toDTO(HeatLineEntity heatLineEntity) {
        HeatLineDto dto = new HeatLineDto();
        dto.setId(heatLineEntity.getId());
        dto.setLane(heatLineEntity.getLane());
        dto.setBib(heatLineEntity.getBib());
        dto.setParticipant(participantService.toDTO(heatLineEntity.getParticipant()));
        dto.setCoaches(heatLineEntity.getCoaches()
                .stream()
                .map(DomainElement::getId)
                .toList()
        );
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
