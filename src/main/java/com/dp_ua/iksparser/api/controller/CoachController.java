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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.dp_ua.iksparser.api.v1.Variables.API_V1_URI;
import static com.dp_ua.iksparser.api.v1.Variables.COACH_URI;

@RestController
@Slf4j
@RequestMapping(API_V1_URI)
@Tag(name = "Coach Management")
public class CoachController {
    @Autowired
    private CoachService coachService;

    @Operation(summary = "Get coach info by id",
            description = "Get coach info by id")
    @GetMapping(COACH_URI + "/{id}")
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
    @PostMapping(COACH_URI + "/list")
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

}
