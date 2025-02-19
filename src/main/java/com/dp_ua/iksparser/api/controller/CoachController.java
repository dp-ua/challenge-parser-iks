package com.dp_ua.iksparser.api.controller;

import com.dp_ua.iksparser.dba.dto.CoachDto;
import com.dp_ua.iksparser.dba.service.CoachService;
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
@RequestMapping(API_V1_URI + COACH_URI)
@Tag(name = "Coach Management")
public class CoachController {
    @Autowired
    private CoachService coachService;

    @Operation(summary = "Get coach info by id",
            description = "Get coach info by id")
    @GetMapping("/{id}")
    @Transactional
    public ResponseEntity<CoachDto> getCoachInfo(
            HttpServletRequest request,
            @Schema(description = "Coach id")
            @PathVariable Long id) {

        log.info("URI: {}, id: {} Request from IP: {}, User-Agent: {}",
                request.getRequestURI(),
                id,
                request.getRemoteAddr(),
                request.getHeader("User-Agent"));

        CoachDto coach = coachService.getCoachDto(id);
        if (coach == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(coach);
    }

    @Operation(summary = "Get coaches info by id list",
            description = "Get coaches info by provided list of ids")
    @PostMapping("/list")
    @Transactional
    public ResponseEntity<List<CoachDto>> getCoachesByIds(
            HttpServletRequest request,
            @RequestBody List<Long> ids) {
        log.info("URI: {}, ids: {} Request from IP: {}, User-Agent: {}",
                request.getRequestURI(),
                ids,
                request.getRemoteAddr(),
                request.getHeader("User-Agent"));

        return ResponseEntity.ok(coachService.getCoachesDtoList(ids));
    }

    @Operation(summary = "Get coach by name",
            description = "Get coach by name")
    @GetMapping()
    @Transactional
    public ResponseEntity<Page<CoachDto>> getCoachByName(
            HttpServletRequest request,
            @Schema(description = "Page number for results pagination", defaultValue = "0")
            @RequestParam(defaultValue = "0") int page,
            @Schema(description = "Size of the page for results pagination", defaultValue = DEFAULT_PAGE_SIZE)
            @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) int size,
            @Schema(description = "Coach name(or part) to search by. Case insensitive.")
            @RequestParam String text) {

        log.info("URI: {}, text: {} Request from IP: {}, User-Agent: {}",
                request.getRequestURI(),
                text,
                request.getRemoteAddr(),
                request.getHeader("User-Agent"));

        return ResponseEntity.ok(coachService.getByNamePartialMatch(text, page, size));
    }
}
