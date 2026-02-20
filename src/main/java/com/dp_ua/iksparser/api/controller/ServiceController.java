package com.dp_ua.iksparser.api.controller;

import static com.dp_ua.iksparser.api.v1.Variables.API_V1_URI;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Conditional;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dp_ua.iksparser.bot.abilities.competition.CompetitionFacade;
import com.dp_ua.iksparser.bot.event.UpdateCompetitionEvent;
import com.dp_ua.iksparser.configuration.IsServiceEnable;
import com.dp_ua.iksparser.dba.entity.CompetitionEntity;
import com.dp_ua.iksparser.dba.entity.EventEntity;
import com.dp_ua.iksparser.dba.entity.UpdateStatusEntity;
import com.dp_ua.iksparser.dba.service.CompetitionService;
import com.dp_ua.iksparser.dba.service.EventService;
import com.dp_ua.iksparser.exeption.ParsingException;
import com.dp_ua.iksparser.monitor.LogRequestDetails;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping(API_V1_URI)
@Tag(name = "ONLY FOR LOCAL TEST") // Only for test!!
@RequiredArgsConstructor
@Conditional(IsServiceEnable.class)
public class ServiceController {

    private final CompetitionService competitionService;
    private final EventService eventService;
    private final CompetitionFacade competitionFacade;
    private final ApplicationEventPublisher publisher;

    @Operation(summary = "Delete an event by ID", description = "Deletes an event from the database by its ID.")
    @DeleteMapping("/event/{eventId}")
    @LogRequestDetails(parameters = {"eventId"})
    public ResponseEntity<String> removeEvent(
            @Parameter(description = "ID of the event to be deleted") @PathVariable long eventId) {
        log.info("Trying to delete event: {}", eventId);
        EventEntity event = eventService.findById(eventId);
        if (event == null) {
            return ResponseEntity.notFound().build();
        }
        log.info("Deleting event: {}", event);
        eventService.delete(event);
        return ResponseEntity.ok("Event deleted. id:" + eventId);
    }

    @GetMapping("/competition/update")
    @LogRequestDetails(parameters = {"year"})
    public ResponseEntity<String> updateCompetitionList(@RequestParam(required = false) Integer year) {
        if (year == null) {
            year = LocalDate.now().getYear();
        }
        log.info("Going to update competition list for year: {}", year);
        try {
            competitionFacade.updateCompetitionsList(year);
            return ResponseEntity.ok("Going to update competition list at " + DateTimeFormatter.ISO_INSTANT.format(Instant.now()));
        } catch (ParsingException e) {
            log.error("Error while updating competition list", e);
            return ResponseEntity.badRequest().body("Error while updating competition list: " + e.getMessage());
        }
    }

    @GetMapping("/competition/update/{id}")
    @LogRequestDetails(parameters = {"id"})
    public ResponseEntity<String> updateCompetitionById(@PathVariable Long id) {
        CompetitionEntity competition = competitionService.findById(id);
        if (competition == null) {
            return ResponseEntity.notFound().build();
        }
        runEventToUpdateCompetition(competition);
        log.info("Update competition by id " + id);
        return ResponseEntity.ok("Going to update competition: " + id +
                " at " + DateTimeFormatter.ISO_INSTANT.format(Instant.now()));
    }

    private void runEventToUpdateCompetition(CompetitionEntity competition) {
        if (competition.isURLEmpty()) {
            log.warn("Can't update. Competition URL is empty, id: {}", competition.getId());
            return;
        }
        if (competition.isUrlNotValid()) {
            log.warn("Can't update. Competition URL is not valid, id: {}", competition.getId());
            return;
        }
        UpdateStatusEntity message = new UpdateStatusEntity();
        message.setCompetitionId(competition.getId());
        message.setChatId("");
        message.setEditMessageId(null);
        UpdateCompetitionEvent updateCompetitionEvent = new UpdateCompetitionEvent(this, message);
        publisher.publishEvent(updateCompetitionEvent);
    }

    @GetMapping("competition/full/{id}")
    @Transactional
    @LogRequestDetails(parameters = {"id"})
    public ResponseEntity<CompetitionEntity> getFullCompetitionById(
            HttpServletRequest request,
            @Schema(description = "Competition id")
            @PathVariable Long id) {

        log.info("URI: {}, id: {} Request from IP: {}, User-Agent: {}",
                request.getRequestURI(),
                id,
                request.getRemoteAddr(),
                request.getHeader("User-Agent"));

        CompetitionEntity competition = competitionService.findById(id);
        if (competition == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(competition);
    }

}
