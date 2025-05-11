package com.dp_ua.iksparser.dba.service;

import java.util.List;

import org.springframework.stereotype.Component;

import com.dp_ua.iksparser.dba.dto.EventDto;
import com.dp_ua.iksparser.dba.entity.EventEntity;
import com.dp_ua.iksparser.dba.repo.EventRepo;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Component
@Transactional
@RequiredArgsConstructor
public class EventService {

    private final EventRepo repo;
    private final HeatService heatService;

    public EventEntity save(EventEntity event) {
        return repo.save(event);
    }

    public List<EventEntity> findAll() {
        return repo.findAll();
    }

    public EventEntity findById(Long id) {
        return repo.findById(id).orElse(null);
    }

    public EventDto toDTO(EventEntity eventEntity) {
        EventDto eventDto = new EventDto();
        eventDto.setId(eventEntity.getId());
        eventDto.setTime(eventEntity.getTime());
        eventDto.setEventName(eventEntity.getEventName());
        eventDto.setCategory(eventEntity.getCategory());
        eventDto.setRound(eventEntity.getRound());
        eventDto.setStartListUrl(eventEntity.getStartListUrl());
        eventDto.setResultUrl(eventEntity.getResultUrl());
        eventDto.setHeats(eventEntity.getHeats()
                .stream()
                .map(heatService::toDto)
                .toList()
        );
        return eventDto;
    }

    public void delete(EventEntity event) {
        repo.delete(event);
    }
}
