package com.dp_ua.iksparser.dba.service;

import com.dp_ua.iksparser.dba.entity.HeatEntity;
import com.dp_ua.iksparser.dba.entity.HeatLineEntity;
import com.dp_ua.iksparser.dba.dto.HeatDto;
import com.dp_ua.iksparser.dba.repo.HeatRepo;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

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

    public HeatEntity findById(Long id) {
        return repo.findById(id).orElse(null);
    }

    public HeatDto convertToDto(HeatEntity heat) {
        HeatDto dto = new HeatDto();
        dto.setId(heat.getId());
        dto.setName(heat.getName());
        dto.setHeatLines(heat.getHeatLines().stream().map(HeatLineEntity::getId).toList());
        return dto;
    }

    public List<HeatDto> convertToDtoList(List<HeatEntity> heats) {
        return heats.stream().map(this::convertToDto).toList();
    }
}
