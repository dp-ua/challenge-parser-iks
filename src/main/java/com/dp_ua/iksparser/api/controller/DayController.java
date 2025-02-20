package com.dp_ua.iksparser.api.controller;

import static com.dp_ua.iksparser.api.v1.Variables.API_V1_URI;
import static com.dp_ua.iksparser.api.v1.Variables.DAY_URI;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dp_ua.iksparser.dba.dto.DayDto;
import com.dp_ua.iksparser.dba.entity.DayEntity;
import com.dp_ua.iksparser.dba.service.DayService;
import com.dp_ua.iksparser.monitor.LogRequestDetails;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping(API_V1_URI)
@Tag(name = "Day Management")
public class DayController {
    private final DayService dayService;

    public DayController(DayService dayService) {
        this.dayService = dayService;
    }

    @Operation(summary = "Get day info by id",
            description = "Get day info by id")
    @GetMapping(DAY_URI + "/{id}")
    @Transactional
    @LogRequestDetails(parameters = {"id"})
    public ResponseEntity<DayDto> getDayInfo(
            @Schema(description = "Day id")
            @PathVariable Long id) {

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
    @LogRequestDetails(parameters = {"ids"})
    public ResponseEntity<List<DayDto>> getDaysByIds(
            @RequestBody List<Long> ids) {

        List<DayEntity> days = ids.stream().map(dayService::findById).toList();
        List<DayDto> dayDtos = dayService.convertToDtoList(days);
        return ResponseEntity.ok(dayDtos);
    }
}
