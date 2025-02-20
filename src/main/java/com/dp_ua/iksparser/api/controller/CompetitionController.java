package com.dp_ua.iksparser.api.controller;


import static com.dp_ua.iksparser.api.v1.Variables.API_V1_URI;
import static com.dp_ua.iksparser.api.v1.Variables.COMPETITIONS_URI;
import static com.dp_ua.iksparser.api.v1.Variables.DEFAULT_PAGE_SIZE;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dp_ua.iksparser.dba.dto.CompetitionDto;
import com.dp_ua.iksparser.dba.entity.CompetitionEntity;
import com.dp_ua.iksparser.dba.service.CompetitionService;
import com.dp_ua.iksparser.monitor.LogRequestDetails;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping(API_V1_URI)
@Tag(name = "Competition Management")
@RequiredArgsConstructor
public class CompetitionController {
    private final CompetitionService competitionService;

    @Operation(summary = "Get all competitions",
            description = "Get all competitions with pagination and search by name")
    @GetMapping(COMPETITIONS_URI)
    @LogRequestDetails(parameters = {"page", "size", "text", "status"})
    public Page<CompetitionDto> getAllCompetitions(
            @Schema(description = "Page number for results pagination", defaultValue = "0")
            @RequestParam(defaultValue = "0") int page,
            @Schema(description = "Size of the page for results pagination", defaultValue = DEFAULT_PAGE_SIZE)
            @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) int size,
            @Schema(description = "Text for name search(case-insensitive)")
            @RequestParam(required = false) String text,
            @Schema(description = "Status of competition, can be several separated by space(case-insensitive)")
            @RequestParam(required = false) String status) {

        return competitionService.getCompetitions(text, status, page, size);
    }

    @Operation(summary = "Get competition by id",
            description = "Get competition by id")
    @GetMapping(COMPETITIONS_URI + "/{id}")
    @Transactional
    @LogRequestDetails(parameters = {"id"})
    public ResponseEntity<CompetitionDto> getCompetitionById(
            @Schema(description = "Competition id")
            @PathVariable Long id) {

        CompetitionEntity competition = competitionService.findById(id);
        if (competition == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(competitionService.convertToDto(competition));
    }

    @Operation(summary = "Get all statuses",
            description = "Get all statuses of competitions")
    @GetMapping(COMPETITIONS_URI + "/statuses")
    @LogRequestDetails
    public ResponseEntity<List<String>> getAllStatuses() {
        return ResponseEntity.ok(competitionService.getAllStatuses());
    }
}