package com.dp_ua.iksparser.api.controller;


import com.dp_ua.iksparser.bot.abilities.competition.CompetitionFacade;
import com.dp_ua.iksparser.dba.dto.CompetitionDto;
import com.dp_ua.iksparser.dba.dto.ParticipantDto;
import com.dp_ua.iksparser.dba.entity.ParticipantEntity;
import com.dp_ua.iksparser.dba.service.ParticipantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

import static com.dp_ua.iksparser.api.v1.Variables.API_V1_URI;
import static com.dp_ua.iksparser.api.v1.Variables.DEFAULT_PAGE_SIZE;

@RestController
@Slf4j
@RequestMapping(API_V1_URI)
@Tag(name = "Participant Management")
public class ParticipantController {

    private final ParticipantService participantService;
    @Autowired
    CompetitionFacade competitionFacade;

    @Autowired
    public ParticipantController(ParticipantService service) {
        this.participantService = service;
    }

    @Operation(summary = "Get all participants",
            description = "Get all participants with pagination. Filtred by name parts. Ordered by [Surname,Name")
    @GetMapping("/participants")
    public Page<ParticipantDto> getAllParticipants(
            HttpServletRequest request,
            @Schema(description = "Page number for results pagination", defaultValue = "0")
            @RequestParam(defaultValue = "0") int page,
            @Schema(description = "Size of the page for results pagination", defaultValue = DEFAULT_PAGE_SIZE)
            @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) int size,
            @RequestParam(required = false) List<String> nameParts) {

        log.info("URI: {}, page: {}, size: {}, nameParts: [{}], Request from IP: {}, User-Agent: {}",
                request.getRequestURI(),
                page, size, nameParts,
                request.getRemoteAddr(),
                request.getHeader("User-Agent"));

        Pageable pageable = PageRequest.of(page, size);
        return participantService.findAllBySurnameAndNameParts(nameParts, pageable)
                .map(participantService::convertToDto);
    }

    @Operation(summary = "Get participant by ID",
            description = "Get participant by ID")
    @GetMapping("/participants/{id}")
    public ResponseEntity<ParticipantDto> getParticipantById(
            HttpServletRequest request,
            @Schema(description = "Participant ID")
            @RequestParam Long id) {

        log.info("URI: {}, id: {} Request from IP: {}, User-Agent: {}",
                request.getRequestURI(),
                id,
                request.getRemoteAddr(),
                request.getHeader("User-Agent"));

        return participantService.findById(id)
                .map(p -> participantService.convertToDto(p))
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Get participants by list ids",
            description = "Get participants by list ids")
    @PostMapping("/participants/list")
    public ResponseEntity<List<ParticipantDto>> getParticipantsByIds(
            HttpServletRequest request,
            @RequestBody List<Long> ids) {

        log.info("URI: {}, ids: {} Request from IP: {}, User-Agent: {}",
                request.getRequestURI(),
                ids,
                request.getRemoteAddr(),
                request.getHeader("User-Agent"));

        List<ParticipantDto> participants = ids.stream()
                .map(participantService::findById)
                .filter(p -> p.isPresent())
                .map(p -> participantService.convertToDto(p.get()))
                .toList();
        return ResponseEntity.ok(participants);
    }

    @Operation(summary = "Get competitions for participant",
            description = "Get competitions for participant")
    @GetMapping("/participants/{id}/competitions")
    @Transactional
    public ResponseEntity<CompetitionDto> getCompetitionsForParticipant(
            HttpServletRequest request,
            @Schema(description = "Participant ID")
            @RequestParam Long id) {

        log.info("URI: {}, id: {} Request from IP: {}, User-Agent: {}",
                request.getRequestURI(),
                id,
                request.getRemoteAddr(),
                request.getHeader("User-Agent"));

        Optional<ParticipantEntity> participant = participantService.findById(id);
        if (participant.isPresent()) {
            List<CompetitionDto> competitions = competitionFacade.getCompetitionsForParticipant(participant.get());
            return ResponseEntity.ok(competitions.get(0));
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}