package com.dp_ua.iksparser.service;

import static com.dp_ua.iksparser.dba.entity.CompetitionStatus.C_IN_PROGRESS;
import static com.dp_ua.iksparser.dba.entity.CompetitionStatus.C_NOT_STARTED;
import static com.dp_ua.iksparser.dba.entity.CompetitionStatus.C_PLANED;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.jsoup.nodes.Document;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import com.dp_ua.iksparser.bot.event.SubscribeEvent;
import com.dp_ua.iksparser.dba.entity.CompetitionEntity;
import com.dp_ua.iksparser.dba.entity.DayEntity;
import com.dp_ua.iksparser.dba.entity.EventEntity;
import com.dp_ua.iksparser.dba.entity.HeatEntity;
import com.dp_ua.iksparser.dba.entity.HeatLineEntity;
import com.dp_ua.iksparser.dba.entity.ParticipantEntity;
import com.dp_ua.iksparser.dba.service.CompetitionService;
import com.dp_ua.iksparser.dba.service.DayService;
import com.dp_ua.iksparser.dba.service.EventService;
import com.dp_ua.iksparser.dba.service.HeatService;
import com.dp_ua.iksparser.exeption.ParsingException;
import com.dp_ua.iksparser.service.parser.CompetitionPageParser;
import com.dp_ua.iksparser.service.parser.EventPageParser;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Component
@RequiredArgsConstructor
public class DataUpdateService {

    private final CompetitionService competitionService;
    private final Downloader downloader;
    private final CompetitionPageParser competitionParser;
    private final EventPageParser eventParser;
    private final ApplicationEventPublisher publisher;
    private final DayService dayService;
    private final EventService eventService;
    private final HeatService heatService;


    /**
     * Updates one competition from the site. Called one by one from {@link CompetitionUpdateQueue}.
     */
    @Transactional
    public void update(long competitionId) {
        CompetitionEntity competition = competitionService.findById(competitionId);
        if (competition == null) {
            log.error("Competition with id {} not found", competitionId);
            return;
        }
        try {
            updateCompetition(competition);
            if (competition.isFilled()) {
                registerUpdateSuccess(competition);
            } else {
                log.info("[{}] Competition page has no days, counted as a failed update day", competitionId);
                registerUpdateFailure(competitionId);
            }
        } catch (Exception e) {
            log.error("[{}]Error updating competition", competitionId, e);
            registerUpdateFailure(competitionId);
        } catch (ParsingException e) {
            log.warn("[{}] Error parsing competition, reason:{}", competitionId, e.getMessage());
            registerUpdateFailure(competitionId);
        } finally {
            log.debug("[{}]Finished updating competition", competitionId);
        }
    }

    private void registerUpdateSuccess(CompetitionEntity competition) {
        if (competition.getFailedUpdateDays() > 0) {
            competition.resetUpdateFailures();
            competitionService.save(competition);
        }
    }

    private void registerUpdateFailure(long competitionId) {
        try {
            CompetitionEntity competition = competitionService.findById(competitionId);
            if (competition != null) {
                competition.registerUpdateFailure(LocalDate.now());
                competitionService.save(competition);
            }
        } catch (Exception e) {
            log.warn("[{}] Can't register update failure: {}", competitionId, e.getMessage());
        }
    }

    @Transactional
    private void updateCompetition(CompetitionEntity competition) throws ParsingException {
        log.debug("[{}] Updating competition", competition.getId());
        Document document = downloader.getDocument(competition.getUrl());

        operateDaysAndAddItToCompetition(competition, document);
        List<EventEntity> newEvents = operateAndGetNewEventsForDays(competition, document);
        Map<ParticipantEntity, List<HeatLineEntity>> participations = operateEventsToParseHeats(newEvents);
        competitionService.save(competition);
        if (participations.isEmpty()) {
            log.debug("[{}]No new participants", competition.getId());
        } else if (isNeedToInformSubscribers(competition)) {
            log.info("[{}] Informing subscribers about new participants: [{}]",
                    competition.getId(), participations.size());
            publisher.publishEvent(new SubscribeEvent(this, participations));
        } else {
            log.debug("[{}] No need to inform subscribers. Status: [{}], end date: [{}]",
                    competition.getId(), competition.getStatus(), competition.getEndDate());
        }
    }

    private boolean isNeedToInformSubscribers(CompetitionEntity competition) {
        LocalDate date = competitionService.getParsedDate(competition.getEndDate());
        String status = competition.getStatus();
        return isDateForUpdate(date) || isStatusForUpdate(status);
    }

    private boolean isStatusForUpdate(String status) {
        return C_IN_PROGRESS.getName().equals(status) ||
                C_NOT_STARTED.getName().equals(status) ||
                C_PLANED.getName().equals(status);
    }

    private boolean isDateForUpdate(LocalDate date) {
        // if date is more than 2 days ago - no need to inform
        LocalDate now = LocalDate.now();
        return date.isEqual(now) ||                                    // today
                date.isEqual(now.minusDays(1)) || // yesterday
                date.isEqual(now.minusDays(2)); // day before yesterday
    }

    private Map<ParticipantEntity, List<HeatLineEntity>> operateEventsToParseHeats(List<EventEntity> events) throws ParsingException {
        Map<ParticipantEntity, List<HeatLineEntity>> participations = new HashMap<>();
        for (EventEntity event : events) {
            if (!event.hasStartListUrl()) {
                log.debug("Event {} has no start list url", event.getEventName());
            } else {
                Document eventDocument = downloader.getDocument(event.getStartListUrl());
                List<HeatEntity> heats = eventParser.getHeats(eventDocument);
                heats.forEach(heat -> {
                    heatService.save(heat);
                    heat.setEvent(event);
                    event.addHeat(heat);
                    extractParticipants(heat, participations);
                });
            }
        }
        return participations;
    }

    private void extractParticipants(HeatEntity heat, Map<ParticipantEntity, List<HeatLineEntity>> participations) {
        heat.getHeatLines().forEach(heatLine -> {
            ParticipantEntity participant = heatLine.getParticipant();
            if (participations.containsKey(participant)) {
                participations.get(participant).add(heatLine);
            } else {
                List<HeatLineEntity> heatLines = new ArrayList<>();
                heatLines.add(heatLine);
                participations.put(participant, heatLines);
            }
        });
    }

    private List<EventEntity> operateAndGetNewEventsForDays(CompetitionEntity competition, Document document) {
        List<EventEntity> newEventResults = new ArrayList<>();

        competition.getDays().forEach(day -> {
            List<EventEntity> updatedEvents = getUpdatedEvents(competition, document, day);
            List<EventEntity> oldEvents = day.getEvents();

            if (oldEvents.isEmpty()) {
                newEventResults.addAll(handleNewEvents(day, updatedEvents));
            } else {
                newEventResults.addAll(processUpdatedEvents(day, updatedEvents, oldEvents));
            }
        });

        return newEventResults;
    }

    private List<EventEntity> getUpdatedEvents(CompetitionEntity competition, Document document, DayEntity day) {
        try {
            return competitionParser.getUnsavedEvents(document, day);
        } catch (Exception e) {
            log.warn("Error parsing events for competition[{}], day[{}]: {}",
                    competition.getId(), day.getDateId(), e.getMessage());
            return new ArrayList<>();
        }
    }

    private List<EventEntity> handleNewEvents(DayEntity day, List<EventEntity> updatedEvents) {
        List<EventEntity> newResults = new ArrayList<>();
        updatedEvents.forEach(event -> {
            if (event.hasStartListUrl()) {
                newResults.add(event);
            }
            saveEventAndSetRelationsToDay(event, day);
        });
        return newResults;
    }

    private List<EventEntity> processUpdatedEvents(DayEntity day, List<EventEntity> updatedEvents, List<EventEntity> oldEvents) {
        return updatedEvents.stream()
                .map(newEvent -> oldEvents.stream()
                        .filter(newEvent::isTheSame)
                        .findFirst()
                        .map(oldEvent -> handleExistingEvent(newEvent, oldEvent))
                        .orElseGet(() -> handleNewEvent(day, newEvent))
                )
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    private Optional<EventEntity> handleExistingEvent(EventEntity newEvent, EventEntity oldEvent) {
        if (isHasNewInformation(newEvent, oldEvent)) {
            oldEvent.updateEventDetails(newEvent);
            return Optional.of(oldEvent);
        }
        return Optional.empty();
    }

    private Optional<EventEntity> handleNewEvent(DayEntity day, EventEntity newEvent) {
        saveEventAndSetRelationsToDay(newEvent, day);
        if (newEvent.hasStartListUrl() || newEvent.hasResultUrl()) {
            return Optional.of(newEvent);
        }
        return Optional.empty();
    }

    private void saveEventAndSetRelationsToDay(EventEntity event, DayEntity day) {
        eventService.save(event);
        event.setDay(day);
        day.addEvent(event);
    }

    private boolean isHasNewInformation(EventEntity newEvent, EventEntity oldEvent) {
        return newEvent.hasResultUrl() && !oldEvent.hasResultUrl() ||
                newEvent.hasStartListUrl() && !oldEvent.hasStartListUrl();
    }

    private void operateDaysAndAddItToCompetition(CompetitionEntity competition, Document document) throws ParsingException {
        List<DayEntity> updatedDays = competitionParser.getUnsavedUnfilledDays(document);
        List<DayEntity> oldDays = competition.getDays();
        if (oldDays.isEmpty()) {
            updatedDays.forEach(day -> saveDayAndSetRelationToCompetition(competition, day));
        } else {
            updatedDays.forEach(newDay -> oldDays.stream()
                    .filter(newDay::isTheSame)
                    .findFirst()
                    .ifPresentOrElse(oldDay -> dayService.save(oldDay),
                            () -> saveDayAndSetRelationToCompetition(competition, newDay)
                    ));
        }
    }

    private void saveDayAndSetRelationToCompetition(CompetitionEntity competition, DayEntity day) {
        dayService.save(day);
        day.setCompetition(competition);
        competition.addDay(day);
    }

}
