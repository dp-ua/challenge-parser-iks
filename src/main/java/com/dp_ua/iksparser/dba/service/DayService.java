package com.dp_ua.iksparser.dba.service;

import com.dp_ua.iksparser.dba.element.DayEntity;
import com.dp_ua.iksparser.dba.element.dto.DayDto;
import com.dp_ua.iksparser.dba.repo.DayRepo;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Transactional
public class DayService {
    private final DayRepo repo;

    @Autowired
    public DayService(DayRepo repo) {
        this.repo = repo;
    }

    public DayEntity save(DayEntity day) {
        return repo.save(day);
    }

    public DayDto convertToDto(DayEntity day) {
        DayDto dayDto = new DayDto();
        dayDto.setId(day.getId());
        dayDto.setDate(day.getDate());
        dayDto.setDayName(day.getDayName());
        dayDto.setDayNameEn(day.getDayNameEn());
        dayDto.setEvents(day.getEvents().stream().map(event -> event.getId()).toList());
        return dayDto;
    }

    public List<DayDto> convertToDtoList(List<DayEntity> days) {
        return days.stream().map(this::convertToDto).toList();
    }

    public DayEntity findById(Long id) {
        return repo.findById(id).orElse(null);
    }
}
