package com.dp_ua.iksparser.api.controller;

import com.dp_ua.iksparser.dba.entity.DayEntity;
import com.dp_ua.iksparser.dba.dto.DayDto;
import com.dp_ua.iksparser.dba.service.DayService;
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
import static com.dp_ua.iksparser.api.v1.Variables.DAY_URI;

@RestController
@Slf4j
@RequestMapping(API_V1_URI)
@Tag(name = "Day Management")
public class DayController {
    @Autowired
    private DayService dayService;

    @Operation(summary = "Get day info by id",
            description = "Get day info by id")
    @GetMapping(DAY_URI + "/{id}")
    @Transactional
    public ResponseEntity<DayDto> getDayInfo(
            HttpServletRequest request,
            @Schema(description = "Day id")
            @PathVariable Long id) {

        log.info("URI: {}, id: {} Request from IP: {}, User-Agent: {}",
                request.getRequestURI(),
                id,
                request.getRemoteAddr(),
                request.getHeader("User-Agent"));

        DayEntity day = dayService.findById(id);
        if (day == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(dayService.convertToDto(day));
    }

    @Operation(summary = "Get days by id list",
            description = "Get days by provided list of ids")
    @PostMapping(DAY_URI + "/list")
    @Transactional
    public ResponseEntity<List<DayDto>> getDaysByIds(
            HttpServletRequest request,
            @RequestBody List<Long> ids) {

        log.info("URI: {}, ids: {} Request from IP: {}, User-Agent: {}",
                request.getRequestURI(),
                ids,
                request.getRemoteAddr(),
                request.getHeader("User-Agent"));

        List<DayEntity> days = ids.stream().map(dayService::findById).toList();
        List<DayDto> dayDtos = dayService.convertToDtoList(days);
        return ResponseEntity.ok(dayDtos);
    }
}
