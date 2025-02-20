package com.dp_ua.iksparser.api.controller;

import static com.dp_ua.iksparser.api.v1.Variables.API_V1_URI;
import static com.dp_ua.iksparser.api.v1.Variables.HEAT_LINE_URI;

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

import com.dp_ua.iksparser.dba.dto.HeatLineDto;
import com.dp_ua.iksparser.dba.entity.HeatLineEntity;
import com.dp_ua.iksparser.dba.service.HeatLineService;
import com.dp_ua.iksparser.monitor.LogRequestDetails;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping(API_V1_URI + HEAT_LINE_URI)
@Tag(name = "HeatLine Management")
@RequiredArgsConstructor
public class HeatLineController {
    private final HeatLineService heatLineService;

    @Operation(summary = "Get heatLine info by id",
            description = "Get heatLine info by id")
    @GetMapping("/{id}")
    @Transactional
    @LogRequestDetails(parameters = {"id"})
    public ResponseEntity<HeatLineDto> getHeatLineInfo(
            @Schema(description = "HeatLine id")
            @PathVariable Long id) {

        Optional<HeatLineEntity> heatLine = heatLineService.findById(id);

        return heatLine.map(heatLineEntity ->
                        ResponseEntity.ok(heatLineService.convertToDto(heatLineEntity)))
                .orElseGet(() ->
                        ResponseEntity.notFound().build());
    }

    @Operation(summary = "Get heatLines by id list",
            description = "Get heatLines by provided list of ids")
    @PostMapping("/list")
    @Transactional
    @LogRequestDetails(parameters = {"ids"})
    public ResponseEntity<List<HeatLineDto>> getHeatLinesByIds(
            @RequestBody List<Long> ids) {

        if (Objects.isNull(ids) || ids.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        List<HeatLineDto> heatLines = ids.stream()
                .map(heatLineService::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(heatLineService::convertToDto)
                .toList();

        return ResponseEntity.ok(heatLines);
    }
}
