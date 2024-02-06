package com.dp_ua.iksparser.service;

import com.dp_ua.iksparser.bot.command.impl.CommandCompetition;
import com.dp_ua.iksparser.bot.command.impl.CommandCompetitionNotLoaded;
import com.dp_ua.iksparser.bot.event.GetMessageEvent;
import com.dp_ua.iksparser.bot.event.UpdateCompetitionEvent;
import com.dp_ua.iksparser.bot.message.SelfMessage;
import com.dp_ua.iksparser.dba.element.*;
import com.dp_ua.iksparser.dba.service.*;
import com.dp_ua.iksparser.exeption.ParsingException;
import com.dp_ua.iksparser.service.parser.CompetitionPageParser;
import com.dp_ua.iksparser.service.parser.EventPageParser;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.dp_ua.iksparser.dba.element.CompetitionStatus.*;
import static com.dp_ua.iksparser.service.DataUpdateService.UpdateStatus.*;


@Slf4j
@Component
public class DataUpdateService implements ApplicationListener<UpdateCompetitionEvent> {
    @Autowired
    private UpdateStatusService service;
    @Autowired
    CompetitionService competitionService;
    @Autowired
    private Downloader downloader;
    @Autowired
    private CompetitionPageParser competitionParser;
    @Autowired
    EventPageParser eventParser;
    @Autowired
    ApplicationEventPublisher publisher;
    @Autowired
    private DayService dayService;
    @Autowired
    private EventService eventService;
    @Autowired
    private HeatService heatService;
    private static final Set<Long> updating = new ConcurrentSkipListSet<>();
    Lock lock = new ReentrantLock();

    @Override
    @Async("updateExecutor")
    @Transactional
    public void onApplicationEvent(UpdateCompetitionEvent event) {
        UpdateStatusEntity message = event.getMessage();

        message.setStatus(U_STARTED.name());
        service.save(message);

        long competitionId = message.getCompetitionId();
        if (updating.contains(competitionId)) {
            log.info("Already updating competition {}", competitionId);
            return;
        }
        CompetitionEntity competition = competitionService.findById(competitionId);
        if (competition == null) {
            String s1 = String.format("Competition with id %d not found", competitionId);
            log.error(s1);
            message.setReason(s1);
            message.setStatus(U_ERROR.name());
            service.save(message);
            return;
        }
        updating.add(competitionId);
        lock.lock();
        try {
            updateCompetition(competition);
            changeStatus(competitionId, U_STARTED, U_UPDATED);
        } catch (Exception e) {
            log.error("Error updating competition {}", competitionId, e);
            changeStatus(competitionId, U_STARTED, U_ERROR);
        } catch (ParsingException e) {
            log.warn("Error parsing competition {}, reason:{}", competitionId, e.getMessage());
            changeStatus(competitionId, U_STARTED, U_INFORM_ERROR);
        } finally {
            log.info("Finished updating competition {}", competitionId);
            lock.unlock();
            updating.remove(competitionId);
            List.of(U_UPDATED, U_INFORM_ERROR).forEach(status -> sendMessages(competitionId, status));
        }
    }

    @Transactional
    private void updateCompetition(CompetitionEntity competition) throws ParsingException {
        log.info("Updating competition {}", competition.getId());
        Document document = downloader.getDocument(competition.getUrl());

        operateDaysAndAddItToCompetition(competition, document);
        List<EventEntity> newEvents = operateAndGetNewEventsForDays(competition, document);
        Map<ParticipantEntity, List<HeatLineEntity>> participations = operateEventsToParseHeats(newEvents);
        competitionService.save(competition); // save all cascade

        log.info("OPERATE PARTICIPANTS!!!! SUBSCRIBE LIST. new participants count: {}", participations.size());
        if (isNeedToInformSubscribers(competition)) {
            log.info("WE WILL INFORM SUBSCRIBERS");
            // todo operate participants
        } else {
            log.info("WE WILL NOT INFORM SUBSCRIBERS");
        }
    }

    private boolean isNeedToInformSubscribers(CompetitionEntity competition) {
        String status = competition.getStatus();
        return C_IN_PROGRESS.name().equals(status) ||
                C_NOT_STARTED.name().equals(status) ||
                C_PLANED.name().equals(status);
    }

    private void sendMessages(long competitionId, UpdateStatus status) {

        List<UpdateStatusEntity> statuses = service.findAllByCompetitionIdAndStatus(competitionId, status.name());

        statuses.stream()
                .collect(Collectors.toMap(UpdateStatusEntity::getChatId, Function.identity(), (a, b) -> a))
                .values()
                .forEach(entry -> {
                    if (!entry.getChatId().isEmpty()) {
                        GetMessageEvent messageEvent = getGetMessageEvent(competitionId, entry, status);
                        publisher.publishEvent(messageEvent);
                    }
                });
        statuses.forEach(updateStatusEntity -> {
            updateStatusEntity.setStatus(U_FINISHED.name());
            service.save(updateStatusEntity);
        });
        service.flush();
    }

    private GetMessageEvent getGetMessageEvent(long competitionId, UpdateStatusEntity updateStatusEntity, UpdateStatus status) {
        SelfMessage selfMessage = null;
        if (status == U_UPDATED) {
            selfMessage = getUpdateMessage(competitionId, updateStatusEntity);
        }
        if (status == U_INFORM_ERROR) {
            selfMessage = getErrorMessage(competitionId, updateStatusEntity);
        }
        return new GetMessageEvent(this, selfMessage);
    }

    private SelfMessage getUpdateMessage(long competitionId, UpdateStatusEntity updateStatusEntity) {
        SelfMessage selfMessage = new SelfMessage();
        selfMessage.setChatId(updateStatusEntity.getChatId());
        selfMessage.setMessageText("/" + CommandCompetition.command + " " + competitionId);
        return selfMessage;
    }

    private SelfMessage getErrorMessage(long competitionId, UpdateStatusEntity updateStatusEntity) {
        SelfMessage selfMessage = new SelfMessage();
        selfMessage.setChatId(updateStatusEntity.getChatId());
        selfMessage.setMessageText("/" + CommandCompetitionNotLoaded.command + " " + competitionId);
        return selfMessage;
    }


    private Map<ParticipantEntity, List<HeatLineEntity>> operateEventsToParseHeats(List<EventEntity> events) throws ParsingException {
        Map<ParticipantEntity, List<HeatLineEntity>> participations = new HashMap<>();
        for (EventEntity event : events) {
            if (event.getStartListUrl().isEmpty()) {
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

    private static void extractParticipants(HeatEntity heat, Map<ParticipantEntity, List<HeatLineEntity>> participations) {
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
            List<EventEntity> updatedEvents = competitionParser.getUnsavedEvents(document, day);
            List<EventEntity> oldEvents = day.getEvents();

            if (oldEvents.isEmpty()) {
                updatedEvents.forEach(event -> {
                    if (!event.getStartListUrl().isEmpty()) {
                        newEventResults.add(event);
                    }
                    eventService.save(event);
                    event.setDay(day);
                    day.addEvent(event);
                });
            } else {
                updatedEvents.forEach(newEvent -> oldEvents.stream()
                        .filter(newEvent::isTheSame)
                        .findFirst()
                        .ifPresentOrElse(oldEvent -> {
                                    if (isHaveNewResults(newEvent, oldEvent)) {
                                        newEventResults.add(oldEvent);
                                    }
                                    if (isNeedToUpdateEvent(newEvent, oldEvent)) {
                                        oldEvent.updateEventDetails(newEvent);
                                    }
                                },
                                () -> {
                                    eventService.save(newEvent);
                                    newEvent.setDay(day);
                                    day.addEvent(newEvent);
                                    if (!newEvent.getStartListUrl().isEmpty()) {
                                        newEventResults.add(newEvent);
                                    }
                                }
                        ));
            }
        });
        return newEventResults;
    }

    private static boolean isHaveNewResults(EventEntity newEvent, EventEntity oldEvent) {
        return oldEvent.getStartListUrl().isEmpty() && !newEvent.getStartListUrl().isEmpty();
    }

    private boolean isNeedToUpdateEvent(EventEntity newEvent, EventEntity oldEvent) {
        return oldEvent.getStartListUrl().isEmpty() && !newEvent.getStartListUrl().isEmpty() ||
                oldEvent.getResultUrl().isEmpty() && !newEvent.getResultUrl().isEmpty();
    }

    private void operateDaysAndAddItToCompetition(CompetitionEntity competition, Document document) throws ParsingException {
        List<DayEntity> updatedDays = competitionParser.getUnsavedUnfilledDays(document);
        List<DayEntity> oldDays = competition.getDays();
        if (oldDays.isEmpty()) {
            updatedDays.forEach(day -> {
                dayService.save(day);
                day.setCompetition(competition);
                competition.addDay(day);
            });
        } else {
            updatedDays.forEach(newDay -> oldDays.stream()
                    .filter(newDay::isTheSame)
                    .findFirst()
                    .ifPresentOrElse(oldDay -> {
                                dayService.save(oldDay);
                            },
                            () -> {
                                dayService.save(newDay);
                                newDay.setCompetition(competition);
                                competition.addDay(newDay);
                            }
                    ));
        }
    }

    private void changeStatus(long competitionId, UpdateStatus oldStatus, UpdateStatus
            newStatus) {
        List<UpdateStatusEntity> allByCompetitionId = service.findAllByCompetitionIdAndStatus(competitionId, oldStatus.name());
        allByCompetitionId.forEach(updateStatusEntity -> {
            updateStatusEntity.setStatus(newStatus.name());
            service.save(updateStatusEntity);
        });
        service.saveAll(allByCompetitionId);
    }

    public enum UpdateStatus {
        U_STARTED,
        U_UPDATED,
        U_FINISHED,
        U_INFORM_ERROR,
        U_ERROR
    }
}
