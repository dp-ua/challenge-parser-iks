package com.dp_ua.iksparser.dba.service;

import com.dp_ua.iksparser.dba.element.HeatLineEntity;
import com.dp_ua.iksparser.dba.element.dto.HeatLineDto;
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
        dto.setLane(heatLineEntity.getLane());
        dto.setBib(heatLineEntity.getBib());
        dto.setParticipant(heatLineEntity.getParticipant());
        dto.setCoaches(heatLineEntity.getCoaches().stream().map(coach -> coach.getId()).toList());
        return dto;
    }

    public List<HeatLineDto> convertToDtoList(List<HeatLineEntity> heatLineEntities) {
        return heatLineEntities.stream().map(this::convertToDto).toList();
    }
}
