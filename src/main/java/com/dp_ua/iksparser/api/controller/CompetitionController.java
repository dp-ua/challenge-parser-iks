package com.dp_ua.iksparser.api.controller;


import com.dp_ua.iksparser.dba.element.dto.CompetitionDto;
import com.dp_ua.iksparser.dba.service.CompetitionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

    @GetMapping("/competitions")
    public Page<CompetitionDto> getAllCompetitions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) int size) {
        Pageable pageable = PageRequest.of(page, size);
        return competitionService.getAllCompetitions(pageable);
    }
}