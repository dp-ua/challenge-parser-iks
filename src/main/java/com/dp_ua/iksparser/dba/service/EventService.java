package com.dp_ua.iksparser.dba.service;

import com.dp_ua.iksparser.dba.entity.EventEntity;
import com.dp_ua.iksparser.dba.dto.EventDto;
import com.dp_ua.iksparser.dba.repo.EventRepo;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Transactional
public class EventService {
    private final EventRepo repo;

    @Autowired
    public EventService(EventRepo repo) {
        this.repo = repo;
    }

    public EventEntity save(EventEntity event) {
        return repo.save(event);
    }

    public List<EventEntity> findAll() {
        return repo.findAll();
    }

    public EventEntity findById(Long id) {
        return repo.findById(id).orElse(null);
    }

    public List<EventDto> convertToDtoList(List<EventEntity> events) {
        return events.stream().map(this::convertToDto).toList();
    }

    public EventDto convertToDto(EventEntity eventEntity) {
        EventDto eventDto = new EventDto();
        eventDto.setTime(eventEntity.getTime());
        eventDto.setEventName(eventEntity.getEventName());
        eventDto.setCategory(eventEntity.getCategory());
        eventDto.setRound(eventEntity.getRound());
        eventDto.setStartListUrl(eventEntity.getStartListUrl());
        eventDto.setResultUrl(eventEntity.getResultUrl());
        eventDto.setHeats(eventEntity.getHeats().stream().map(heat -> heat.getId()).toList());
        return eventDto;
    }
}
