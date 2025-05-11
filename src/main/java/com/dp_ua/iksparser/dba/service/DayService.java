package com.dp_ua.iksparser.dba.service;

import java.util.List;

import org.springframework.stereotype.Component;

import com.dp_ua.iksparser.dba.dto.DayDto;
import com.dp_ua.iksparser.dba.entity.DayEntity;
import com.dp_ua.iksparser.dba.repo.DayRepo;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Component
@Transactional
@RequiredArgsConstructor
public class DayService {

    private final DayRepo repo;
    private final EventService eventService;

    public DayEntity save(DayEntity day) {
        return repo.save(day);
    }

    public DayDto toDTO(DayEntity day) {
        DayDto dayDto = new DayDto();
        dayDto.setId(day.getId());
        dayDto.setDate(day.getDate());
        dayDto.setDayName(day.getDayName());
        dayDto.setDayNameEn(day.getDayNameEn());
        dayDto.setEvents(day.getEvents()
                .stream()
                .map(eventService::toDTO)
                .toList()
        );
        return dayDto;
    }

    public List<DayDto> convertToDtoList(List<DayEntity> days) {
        return days.stream().map(this::toDTO).toList();
    }

    public DayEntity findById(Long id) {
        return repo.findById(id).orElse(null);
    }

}
