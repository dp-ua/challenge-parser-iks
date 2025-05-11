package com.dp_ua.iksparser.dba.service;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.dp_ua.iksparser.dba.dto.HeatDto;
import com.dp_ua.iksparser.dba.entity.DomainElement;
import com.dp_ua.iksparser.dba.entity.HeatEntity;
import com.dp_ua.iksparser.dba.repo.HeatRepo;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Component
@Transactional
@RequiredArgsConstructor
public class HeatService {

    private final HeatRepo repo;

    public HeatEntity save(HeatEntity heat) {
        return repo.save(heat);
    }

    public void delete(HeatEntity heat) {
        repo.delete(heat);
    }

    public Optional<HeatEntity> findById(Long id) {
        return repo.findById(id);
    }

    public HeatDto toDto(HeatEntity heat) {
        HeatDto dto = new HeatDto();
        dto.setId(heat.getId());
        dto.setName(heat.getName());
        dto.setHeatLines(heat.getHeatLines()
                .stream()
                .map(DomainElement::getId)
                .toList()
        );
        return dto;
    }

}
