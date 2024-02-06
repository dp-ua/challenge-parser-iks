package com.dp_ua.iksparser.service.cron;

import com.dp_ua.iksparser.SpringApp;
import com.dp_ua.iksparser.bot.abilities.competition.CompetitionFacade;
import com.dp_ua.iksparser.bot.event.UpdateCompetitionEvent;
import com.dp_ua.iksparser.dba.element.CompetitionEntity;
import com.dp_ua.iksparser.dba.element.EventEntity;
import com.dp_ua.iksparser.dba.element.UpdateStatusEntity;
import com.dp_ua.iksparser.dba.service.CompetitionService;
import com.dp_ua.iksparser.dba.service.EventService;
import com.dp_ua.iksparser.exeption.ParsingException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.Ordered;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.dp_ua.iksparser.dba.element.CompetitionStatus.C_FINISHED;

@Component
@Slf4j
public class CronCompetitionUpdater implements ApplicationListener<ContextRefreshedEvent>, Ordered {
    @Autowired
    EventService eventService;
    @Autowired
    CompetitionService competitionService;
    @Autowired
    CompetitionFacade competitionFacade;
    @Autowired
    ApplicationEventPublisher publisher;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        List<CompetitionEntity> competitions = competitionService.findAllOrderByUpdated();
        if (competitions.isEmpty()) {
            log.info("No competitions in DB");
            try {
                competitionFacade.updateCompetitionsList();
            } catch (ParsingException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Scheduled(cron = "0 0/16 10-23 * * *") // every 16 minutes check not filled events from 10 to 23
    public void checkNotFilledEvents() {
        Set<CompetitionEntity> needToUpdateCompetitionIds = new HashSet<>();
        eventService.findAll().stream()
                .filter(EventEntity::isNotFilled)
                .forEach(event -> needToUpdateCompetitionIds.add(event.getDay().getCompetition()));
        needToUpdateCompetitionIds.forEach(competition -> {
            log.info("Need to update competition with id: {}", competition.getId());
            runEventToUpdateCompetition(competition);
        });
    }

    @Scheduled(cron = "0 15 6 * * *") // every day at 6:15 update all competition
    @Transactional
    public void updateAllCompetitions() {
        List<CompetitionEntity> competitions = competitionService.findAllOrderByUpdated();
        log.info("Found {} competitions", competitions.size());
        competitions.stream()
                .filter(CompetitionEntity::isNeedToUpdate)
                .forEach(competition -> {
                    log.info("Need to update competition with id: {}", competition.getId());
                    runEventToUpdateCompetition(competition);
                });
    }

    @Scheduled(cron = "0 15 10,12,14,16,18,20,22 * * *") // from 10 to 00
    public void updateClosestCompetitions() {
        log.info("Start update closest competitions");
        List<CompetitionEntity> competitions = competitionService.findAllOrderByBeginDate(true);
        LocalDate now = LocalDate.now();
        competitions.stream()
                .filter(competition -> {
                    if (C_FINISHED.getName().equals(competition.getStatus())) {
                        return false;
                    }
                    return !LocalDate.parse(competition.getBeginDate(), CompetitionService.FORMATTER).isAfter(now.plusWeeks(1));
                })
                .forEach(competition -> {
                    log.info("Need to update competition with id: {}, begin date: {}", competition.getId(), competition.getBeginDate());
                    runEventToUpdateCompetition(competition);
                });
    }

    private void runEventToUpdateCompetition(CompetitionEntity competition) {
        UpdateStatusEntity message = new UpdateStatusEntity();
        message.setCompetitionId(competition.getId());
        message.setChatId("");
        message.setEditMessageId(null);
        UpdateCompetitionEvent updateCompetitionEvent = new UpdateCompetitionEvent(this, message);
        publisher.publishEvent(updateCompetitionEvent);
    }

    @Override
    public int getOrder() {
        return SpringApp.ORDER_FOR_COMPETITION_UPDATER;
    }
}
