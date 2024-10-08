package com.dp_ua.iksparser.api.controller;

import com.dp_ua.iksparser.dba.dto.HeatLineDto;
import com.dp_ua.iksparser.dba.entity.HeatLineEntity;
import com.dp_ua.iksparser.dba.service.HeatLineService;
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
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.dp_ua.iksparser.api.v1.Variables.API_V1_URI;
import static com.dp_ua.iksparser.api.v1.Variables.HEAT_LINE_URI;

@RestController
@Slf4j
@RequestMapping(API_V1_URI + HEAT_LINE_URI)
@Tag(name = "HeatLine Management")
public class HeatLineController {
    @Autowired
    private HeatLineService heatLineService;

    @Operation(summary = "Get heatLine info by id",
            description = "Get heatLine info by id")
    @GetMapping("/{id}")
    @Transactional
    public ResponseEntity<HeatLineDto> getHeatLineInfo(
            HttpServletRequest request,
            @Schema(description = "HeatLine id")
            @PathVariable Long id) {

        log.info("URI: {}, id: {} Request from IP: {}, User-Agent: {}",
                request.getRequestURI(),
                id,
                request.getRemoteAddr(),
                request.getHeader("User-Agent"));

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
    public ResponseEntity<List<HeatLineDto>> getHeatLinesByIds(
            HttpServletRequest request,
            @RequestBody List<Long> ids) {
        log.info("URI: {}, ids: {} Request from IP: {}, User-Agent: {}",
                request.getRequestURI(),
                ids,
                request.getRemoteAddr(),
                request.getHeader("User-Agent"));

        if (Objects.isNull(ids) || ids.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        List<HeatLineDto> heatLines = ids.stream()
                .map(heatLineService::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(heatLineService::convertToDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(heatLines);
    }
}
