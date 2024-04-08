package com.dp_ua.iksparser.api.controller;


import com.dp_ua.iksparser.dba.element.dto.CompetitionDto;
import com.dp_ua.iksparser.dba.service.CompetitionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.dp_ua.iksparser.api.v1.Variables.API_V1;
import static com.dp_ua.iksparser.api.v1.Variables.DEFAULT_PAGE_SIZE;

@RestController
@Slf4j
@RequestMapping(API_V1)
public class CompetitionController {

    private final CompetitionService competitionService;

    @Autowired
    public CompetitionController(CompetitionService competitionService) {
        this.competitionService = competitionService;
    }

    @Operation(summary = "Get all competitions",
            description = "Get all competitions with pagination and search by name")
    @GetMapping("/competitions")
    public Page<CompetitionDto> getAllCompetitions(
            HttpServletRequest request,
            @Schema(description = "Page number for results pagination", defaultValue = "0")
            @RequestParam(defaultValue = "0") int page,
            @Schema(description = "Size of the page for results pagination", defaultValue = DEFAULT_PAGE_SIZE)
            @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) int size,
            @Schema(description = "Text for name search(case-insensitive)")
            @RequestParam(required = false) String text) {

        log.info("URI: {}, page: {}, size: {}, text: {} Request from IP: {}, User-Agent: {}",
                request.getRequestURI(),
                page, size, text,
                request.getRemoteAddr(),
                request.getHeader("User-Agent"));

        return competitionService.getAllCompetitions(text, page, size);
    }
}