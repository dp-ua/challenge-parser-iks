package com.dp_ua.iksparser.api.controller;

import com.dp_ua.iksparser.dba.dto.HeatDto;
import com.dp_ua.iksparser.dba.entity.HeatEntity;
import com.dp_ua.iksparser.dba.service.HeatService;
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
import static com.dp_ua.iksparser.api.v1.Variables.HEAT_URI;

@RestController
@Slf4j
@RequestMapping(API_V1_URI + HEAT_URI)
@Tag(name = "Heat Management")
public class HeatController {
    @Autowired
    private HeatService heatService;

    @Operation(summary = "Get heat info by id",
            description = "Get heat info by id")
    @GetMapping("/{id}")
    @Transactional
    public ResponseEntity<HeatDto> getHeatInfo(
            HttpServletRequest request,
            @Schema(description = "Heat id")
            @PathVariable Long id) {

        log.info("URI: {}, id: {} Request from IP: {}, User-Agent: {}",
                request.getRequestURI(),
                id,
                request.getRemoteAddr(),
                request.getHeader("User-Agent"));

        Optional<HeatEntity> heat = heatService.findById(id);
        return heat.map(heatEntity ->
                        ResponseEntity.ok(heatService.convertToDto(heatEntity)))
                .orElseGet(() ->
                        ResponseEntity.notFound().build());
    }

    @Operation(summary = "Get heats by id list",
            description = "Get heats by provided list of ids")
    @PostMapping("/list")
    @Transactional
    public ResponseEntity<List<HeatDto>> getHeatsByIds(
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

        List<HeatDto> heats = ids.stream()
                .map(heatService::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(heatService::convertToDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(heats);
    }
}
