package com.dp_ua.iksparser.service;

import com.dp_ua.iksparser.bot.command.impl.CommandCompetition;
import com.dp_ua.iksparser.bot.message.SelfMessage;
import com.dp_ua.iksparser.bot.performer.event.GetMessageEvent;
import com.dp_ua.iksparser.dba.element.CompetitionEntity;
import com.dp_ua.iksparser.dba.element.DayEntity;
import com.dp_ua.iksparser.dba.element.UpdateStatusEntity;
import com.dp_ua.iksparser.dba.service.CompetitionService;
import com.dp_ua.iksparser.dba.service.UpdateStatusService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

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
    private CompetitionPageParser parser;
    @Autowired
    CommandCompetition commandCompetition;
    @Autowired
    ApplicationEventPublisher publisher;
    private static final Set<Long> updating = new ConcurrentSkipListSet<>();
    Lock lock = new ReentrantLock();

    @Override
    @Async("defaultTaskExecutor")
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
        // todo add check. Is need to full update or only black heats
        try {
            downloadDataForCompetition(competition);
            changeStatus(competitionId, STARTED, UPDATED);
        } catch (Exception e) {
            log.error("Error updating competition {}", competitionId, e);
            // todo add send message to user
            changeStatus(competitionId, STARTED, ERROR);
        } finally {
            lock.unlock();
            updating.remove(competitionId);
            sendMessages(competitionId);
        }
    }

    private void sendMessages(long competitionId) {
        List<UpdateStatusEntity> statuses = service.findAllByCompetitionIdAndStatus(competitionId, UPDATED.name());

        statuses.stream()
                .collect(Collectors.toMap(UpdateStatusEntity::getChatId, Function.identity(), (a, b) -> a))
                .values()
                .forEach(entry -> {
                    GetMessageEvent messageEvent = getGetMessageEvent(competitionId, entry);
                    publisher.publishEvent(messageEvent);
                });
        statuses.forEach(updateStatusEntity -> {
            updateStatusEntity.setStatus(FINISHED.name());
            service.save(updateStatusEntity);
        });
        service.flush();
    }

    private GetMessageEvent getGetMessageEvent(long competitionId, UpdateStatusEntity updateStatusEntity) {
        SelfMessage selfMessage = getSelfMessage(competitionId, updateStatusEntity);

        return new GetMessageEvent(this, selfMessage);
    }

    private SelfMessage getSelfMessage(long competitionId, UpdateStatusEntity updateStatusEntity) {
        SelfMessage selfMessage = new SelfMessage();
        selfMessage.setChatId(updateStatusEntity.getChatId());
        selfMessage.setMessageText("/" + commandCompetition.command() + " " + competitionId);
//        selfMessage.setEditMessageId(updateStatusEntity.getEditMessageId());
        return selfMessage;
    }

    @Transactional
    private void downloadDataForCompetition(CompetitionEntity competition) {
        Document document = downloader.getDocument(competition.getUrl());

        List<DayEntity> days = parser.getParsedDays(document);
        days.forEach(day -> {
            day.setCompetition(competition);
            competition.addDay(day);
        });
        competitionService.save(competition);
        competitionService.flush();
    }

    private void changeStatus(long competitionId, UpdateStatus oldStatus, UpdateStatus newStatus) {
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
        ERROR
    }
}
