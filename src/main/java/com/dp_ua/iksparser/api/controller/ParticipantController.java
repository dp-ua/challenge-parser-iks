package com.dp_ua.iksparser.api.controller;


import static com.dp_ua.iksparser.api.v1.Variables.API_V1_URI;
import static com.dp_ua.iksparser.api.v1.Variables.DEFAULT_PAGE_SIZE;
import static com.dp_ua.iksparser.api.v1.Variables.PARTICIPANT_URI;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dp_ua.iksparser.bot.abilities.competition.CompetitionFacade;
import com.dp_ua.iksparser.dba.dto.CompetitionDto;
import com.dp_ua.iksparser.dba.dto.ParticipantDto;
import com.dp_ua.iksparser.dba.entity.ParticipantEntity;
import com.dp_ua.iksparser.dba.service.ParticipantService;
import com.dp_ua.iksparser.monitor.LogRequestDetails;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping(API_V1_URI + PARTICIPANT_URI)
@Tag(name = "Participant Management")
public class ParticipantController {

    private final ParticipantService participantService;
    private final CompetitionFacade competitionFacade;

    public ParticipantController(ParticipantService service, CompetitionFacade competitionFacade) {
        this.participantService = service;
        this.competitionFacade = competitionFacade;
    }

    @Operation(summary = "Get all participants",
            description = "Get all participants with pagination. Filtered by name parts. Ordered by [Surname,Name")
    @GetMapping()
    @LogRequestDetails(parameters = {"page", "size", "text"})
    public Page<ParticipantDto> getAllParticipants(
            @Schema(description = "Page number for results pagination", defaultValue = "0")
            @RequestParam(defaultValue = "0") int page,
            @Schema(description = "Size of the page for results pagination", defaultValue = DEFAULT_PAGE_SIZE)
            @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) int size,
            @Schema(description = "Text for surname and name search(case-insensitive)", example = "Віктор Павлік")
            @RequestParam(required = false) String text) {

        List<String> safeNameParts = convertTextToList(text);

        return participantService.findAllBySurnameAndNameParts(safeNameParts, page, size)
                .map(participantService::convertToDto);
    }

    private List<String> convertTextToList(String text) {
        return text != null ? List.of(text.split(" ")) : Collections.emptyList();
    }

    @Operation(summary = "Get participant by ID",
            description = "Get participant by ID")
    @GetMapping("/{id}")
    @LogRequestDetails(parameters = {"id"})
    public ResponseEntity<ParticipantDto> getParticipantById(
            @Schema(description = "Participant ID")
            @PathVariable Long id) {

        Optional<ParticipantEntity> participant = participantService.findById(id);
        return participant
                .map(participantService::convertToDto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Get participants by list ids",
            description = "Get participants by list ids")
    @PostMapping("/list")
    @LogRequestDetails(parameters = {"ids"})
    public ResponseEntity<List<ParticipantDto>> getParticipantsByIds(
            @RequestBody List<Long> ids) {

        List<ParticipantDto> participants = ids.stream()
                .map(participantService::findById)
                .filter(Optional::isPresent)
                .map(p -> participantService.convertToDto(p.get()))
                .toList();
        return ResponseEntity.ok(participants);
    }

    @Operation(summary = "Get competitions for participant",
            description = "Get competitions for participant")
    @GetMapping("/{id}/competitions")
    @Transactional
    @LogRequestDetails(parameters = {"id"})
    public ResponseEntity<List<CompetitionDto>> getCompetitionsForParticipant(
            @Schema(description = "Participant ID")
            @PathVariable Long id) {

        Optional<ParticipantEntity> participant = participantService.findById(id);
        if (participant.isPresent()) {
            List<CompetitionDto> competitions = competitionFacade.getCompetitionsForParticipant(participant.get());
            return competitions.isEmpty() ?
                    ResponseEntity.noContent().build()
                    :
                    ResponseEntity.ok(competitions);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}