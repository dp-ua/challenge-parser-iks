package com.dp_ua.iksparser.api.controller;

import static com.dp_ua.iksparser.api.v1.Variables.API_V1_URI;
import static com.dp_ua.iksparser.api.v1.Variables.EVENT_URI;

import java.util.List;
import java.util.Objects;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dp_ua.iksparser.dba.dto.EventDto;
import com.dp_ua.iksparser.dba.entity.EventEntity;
import com.dp_ua.iksparser.dba.service.EventService;
import com.dp_ua.iksparser.monitor.LogRequestDetails;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping(API_V1_URI)
@Tag(name = "Event Management")
public class EventController {
    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @Operation(summary = "Get event info by id",
            description = "Get event info by id")
    @GetMapping(EVENT_URI + "/{id}")
    @Transactional
    @LogRequestDetails(parameters = {"id"})
    public ResponseEntity<EventDto> getEventInfo(
            @Schema(description = "Event id")
            @PathVariable Long id) {

        EventEntity event = eventService.findById(id);
        if (event == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(eventService.convertToDto(event));
    }

    @Operation(summary = "Get events by id list",
            description = "Get events by provided list of ids")
    @PostMapping(EVENT_URI + "/list")
    @Transactional
    @LogRequestDetails(parameters = {"ids"})
    public ResponseEntity<List<EventDto>> getEventsByIds(
            @RequestBody List<Long> ids) {

        List<EventDto> events = ids.stream()
                .map(eventService::findById)
                .filter(Objects::nonNull)
                .map(eventService::convertToDto)
                .toList();

        if (events.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(events);
    }
}
