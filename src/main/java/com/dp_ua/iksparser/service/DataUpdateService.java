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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    @Async("taskExecutor")
    @Transactional
    public void onApplicationEvent(UpdateCompetitionEvent event) {
        UpdateStatusEntity message = event.getMessage();

        message.setStatus(STARTED.name());
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
            message.setStatus(ERROR.name());
            service.save(message);
            return;
        }
        updating.add(competitionId);
        lock.lock();
        try {
            updateCompetition(competition);
            changeStatus(competitionId, STARTED, UPDATED);
        } catch (Exception e) {
            log.error("Error updating competition {}", competitionId, e);
            changeStatus(competitionId, STARTED, ERROR);
        } catch (ParsingException e) {
            changeStatus(competitionId, STARTED, INFORM_ERROR);
        } finally {
            lock.unlock();
            updating.remove(competitionId);
            List.of(UPDATED, INFORM_ERROR).forEach(status -> sendMessages(competitionId, status));
        }
    }

    @Transactional
    private void updateCompetition(CompetitionEntity competition) throws ParsingException {
        Document document = downloader.getDocument(competition.getUrl());

        operateDaysAndAddItToCompetition(competition, document);
        List<EventEntity> newEvents = operateAndGetNewEventsForDays(competition, document);
        operateEventsToParseHeats(newEvents);

        competitionService.save(competition); // save all cascade
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
            updateStatusEntity.setStatus(FINISHED.name());
            service.save(updateStatusEntity);
        });
        service.flush();
    }

    private GetMessageEvent getGetMessageEvent(long competitionId, UpdateStatusEntity updateStatusEntity, UpdateStatus status) {
        SelfMessage selfMessage = null;
        if (status == UPDATED) {
            selfMessage = getUpdateMessage(competitionId, updateStatusEntity);
        }
        if (status == INFORM_ERROR) {
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


    private void operateEventsToParseHeats(List<EventEntity> newEvents) throws ParsingException{
        newEvents.forEach(event -> {
            if (event.getStartListUrl().isEmpty()) {
                log.info("Event {} has no start list url", event.getEventName());
                return;
            }
            Document eventDocument = null;
            try {
                eventDocument = downloader.getDocument(event.getStartListUrl());
            } catch (ParsingException e) {
                throw new RuntimeException(e);
            }
            List<HeatEntity> heats = eventParser.getHeats(eventDocument);
            heats.forEach(heat -> {
                heat.getHeatLines().forEach(heatLine -> {
                    // todo operations around subscribing to participant
                    // heatLine.getParticipant();
                });
                heatService.save(heat);

                heat.setEvent(event);
                event.addHeat(heat);
            });
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
        log.info("Updating competition {}", competition.getId());
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
        STARTED,
        UPDATED,
        FINISHED,
        INFORM_ERROR, ERROR
    }
}
