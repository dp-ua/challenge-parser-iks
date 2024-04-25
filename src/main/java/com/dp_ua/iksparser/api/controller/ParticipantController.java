package com.dp_ua.iksparser.api.controller;


import com.dp_ua.iksparser.dba.element.dto.ParticipantDto;
import com.dp_ua.iksparser.dba.service.ParticipantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.dp_ua.iksparser.api.v1.Variables.API_V1_URI;
import static com.dp_ua.iksparser.api.v1.Variables.DEFAULT_PAGE_SIZE;

@RestController
@Slf4j
@RequestMapping(API_V1_URI)
@Tag(name = "Participant Management")
public class ParticipantController {

    private final ParticipantService service;

    @Autowired
    public ParticipantController(ParticipantService service) {
        this.service = service;
    }

    @Operation(summary = "Get all participants",
            description = "Get all participants with pagination. Ordered by Surname and Name")
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

        Pageable pageable = PageRequest.of(page, size, Sort.by("surname", "name"));
        return service.getAll(pageable);
    }
}