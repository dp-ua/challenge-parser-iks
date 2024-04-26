package com.dp_ua.iksparser.api.controller;

import com.dp_ua.iksparser.dba.entity.EventEntity;
import com.dp_ua.iksparser.dba.dto.EventDto;
import com.dp_ua.iksparser.dba.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.dp_ua.iksparser.api.v1.Variables.API_V1_URI;
import static com.dp_ua.iksparser.api.v1.Variables.EVENT_URI;

@RestController
@Slf4j
@RequestMapping(API_V1_URI)
@Tag(name = "Event Management")
public class EventController {
    @Autowired
    private EventService eventService;

    @Operation(summary = "Get event info by id",
            description = "Get event info by id")
    @GetMapping(EVENT_URI + "/{id}")
    @Transactional
    public ResponseEntity<EventDto> getEventInfo(
            HttpServletRequest request,
            @Schema(description = "Event id")
            @PathVariable Long id) {

        log.info("URI: {}, id: {} Request from IP: {}, User-Agent: {}",
                request.getRequestURI(),
                id,
                request.getRemoteAddr(),
                request.getHeader("User-Agent"));

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
    public ResponseEntity<List<EventDto>> getEventsByIds(
            HttpServletRequest request,
            @RequestBody List<Long> ids) {

        log.info("URI: {}, ids: {} Request from IP: {}, User-Agent: {}",
                request.getRequestURI(),
                ids,
                request.getRemoteAddr(),
                request.getHeader("User-Agent"));

        List<EventDto> events = ids.stream()
                .map(eventService::findById)
                .filter(Objects::nonNull)
                .map(eventService::convertToDto)
                .collect(Collectors.toList());

        if (events.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(events);
    }
}
