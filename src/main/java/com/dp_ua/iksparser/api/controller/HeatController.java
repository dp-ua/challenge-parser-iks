package com.dp_ua.iksparser.api.controller;

import static com.dp_ua.iksparser.api.v1.Variables.API_V1_URI;
import static com.dp_ua.iksparser.api.v1.Variables.HEAT_URI;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dp_ua.iksparser.dba.dto.HeatDto;
import com.dp_ua.iksparser.dba.entity.HeatEntity;
import com.dp_ua.iksparser.dba.service.HeatService;
import com.dp_ua.iksparser.monitor.LogRequestDetails;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping(API_V1_URI + HEAT_URI)
@Tag(name = "Heat Management")
@RequiredArgsConstructor
public class HeatController {

    private final HeatService heatService;

    @Operation(summary = "Get heat info by id",
            description = "Get heat info by id")
    @GetMapping("/{id}")
    @Transactional
    @LogRequestDetails(parameters = {"id"})
    public ResponseEntity<HeatDto> getHeatInfo(
            @Schema(description = "Heat id")
            @PathVariable Long id) {

        Optional<HeatEntity> heat = heatService.findById(id);
        return heat.map(heatEntity ->
                        ResponseEntity.ok(heatService.toDto(heatEntity)))
                .orElseGet(() ->
                        ResponseEntity.notFound().build());
    }

    @Operation(summary = "Get heats by id list",
            description = "Get heats by provided list of ids")
    @PostMapping("/list")
    @Transactional
    @LogRequestDetails(parameters = {"ids"})
    public ResponseEntity<List<HeatDto>> getHeatsByIds(
            @RequestBody List<Long> ids) {
        if (Objects.isNull(ids) || ids.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        List<HeatDto> heats = ids.stream()
                .map(heatService::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(heatService::toDto)
                .toList();

        return ResponseEntity.ok(heats);
    }
}
