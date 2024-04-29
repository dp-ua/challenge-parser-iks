package com.dp_ua.iksparser.api.controller;


import com.dp_ua.iksparser.dba.dto.CompetitionDto;
import com.dp_ua.iksparser.dba.entity.CompetitionEntity;
import com.dp_ua.iksparser.dba.service.CompetitionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.dp_ua.iksparser.api.v1.Variables.*;

@RestController
@Slf4j
@RequestMapping(API_V1_URI)
@Tag(name = "Competition Management")
public class CompetitionController {

    @Autowired
    private CompetitionService competitionService;

    @Operation(summary = "Get all competitions",
            description = "Get all competitions with pagination and search by name")
    @GetMapping(COMPETITIONS_URI)
    public Page<CompetitionDto> getAllCompetitions(
            HttpServletRequest request,
            @Schema(description = "Page number for results pagination", defaultValue = "0")
            @RequestParam(defaultValue = "0") int page,
            @Schema(description = "Size of the page for results pagination", defaultValue = DEFAULT_PAGE_SIZE)
            @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) int size,
            @Schema(description = "Text for name search(case-insensitive)")
            @RequestParam(required = false) String text,
            @Schema(description = "Status of competition, can be several separated by space(case-insensitive)")
            @RequestParam(required = false) String status) {

        log.info("URI: {}, page: {}, size: {}, text: {} Request from IP: {}, User-Agent: {}",
                request.getRequestURI(),
                page, size, text,
                request.getRemoteAddr(),
                request.getHeader("User-Agent"));

        return competitionService.getCompetitions(text, status, page, size);
    }

    @Operation(summary = "Get competition by id",
            description = "Get competition by id")
    @GetMapping(COMPETITIONS_URI + "/{id}")
    @Transactional
    public ResponseEntity<CompetitionDto> getCompetitionById(
            HttpServletRequest request,
            @Schema(description = "Competition id")
            @PathVariable Long id) {

        log.info("URI: {}, id: {} Request from IP: {}, User-Agent: {}",
                request.getRequestURI(),
                id,
                request.getRemoteAddr(),
                request.getHeader("User-Agent"));

        CompetitionEntity competition = competitionService.findById(id);
        if (competition == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(competitionService.convertToDto(competition));
    }

    @Operation(summary = "Get all statuses",
            description = "Get all statuses of competitions")
    @GetMapping(COMPETITIONS_URI + "/statuses")
    public ResponseEntity<List<String>> getAllStatuses(
            HttpServletRequest request) {

        log.info("URI: {} Request from IP: {}, User-Agent: {}",
                request.getRequestURI(),
                request.getRemoteAddr(),
                request.getHeader("User-Agent"));

        return ResponseEntity.ok(competitionService.getAllStatuses());
    }
}