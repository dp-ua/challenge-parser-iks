package com.dp_ua.iksparser.service.cron;

import com.dp_ua.iksparser.SpringApp;
import com.dp_ua.iksparser.bot.abilities.competition.CompetitionFacade;
import com.dp_ua.iksparser.bot.event.UpdateCompetitionEvent;
import com.dp_ua.iksparser.dba.entity.CompetitionEntity;
import com.dp_ua.iksparser.dba.entity.EventEntity;
import com.dp_ua.iksparser.dba.entity.UpdateStatusEntity;
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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.dp_ua.iksparser.dba.entity.CompetitionStatus.C_CANCELED;
import static com.dp_ua.iksparser.dba.entity.CompetitionStatus.C_FINISHED;

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
    private final Set<String> finishedOrCanceledStatuses = new HashSet<>(Arrays.asList(C_FINISHED.getName(), C_CANCELED.getName()));


    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        List<CompetitionEntity> competitions = competitionService.findAllOrderByUpdated();
        if (competitions.isEmpty()) {
            log.info("No competitions in DB");
            update(LocalDate.now().getYear());
        }
    }

    @Scheduled(cron = "0 0 0/8 * * *") // update competitions list every 8 hours
    public void updateCompetitionsList() {
        LocalDate now = LocalDate.now();
        update(now.getYear());
        if (now.getMonthValue() == 12 && now.getDayOfMonth() >= 20) {
            update(now.getYear() + 1);
        }
    }

    private void update(int year) {
        try {
            competitionFacade.updateCompetitionsList(year);
        } catch (ParsingException e) {
            log.error("Error while updating competitions list", e);
        }
    }

    @Scheduled(cron = "0 0/9 10-23 * * *") // every 9 minutes check not filled events from 10 to 23
    public void checkNotFilledEvents() {
        Set<CompetitionEntity> needToUpdateCompetitionIds = new HashSet<>();
        eventService.findAll().stream()
                .filter(EventEntity::isNotFilled)
                .forEach(event -> needToUpdateCompetitionIds.add(event.getDay().getCompetition()));
        needToUpdateCompetitionIds.forEach(competition -> {
            log.info("[CNFE] Need to update competition with id: {} begin date: {}", competition.getId(), competition.getBeginDate());
            runEventToUpdateCompetition(competition);
        });
    }

    @Scheduled(cron = "0 15 6 * * *") // every day at 6:15 update all competition
    @Transactional
    public void updateAllCompetitionsDetails() {
        List<CompetitionEntity> competitions = competitionService.findAllOrderByUpdated();
        log.info("Found {} competitions", competitions.size());
        competitions.stream()
                .filter(CompetitionEntity::isNeedToUpdate)
                .forEach(competition -> {
                    log.info("[UACD] Need to update competition with id: {}, begin date: {}", competition.getId(), competition.getBeginDate());
                    runEventToUpdateCompetition(competition);
                });
    }

    @Scheduled(cron = "0 0/20 10-23 * * *") // every 20 minutes check closest competitions
    public void updateClosestCompetitionDetails() {
        log.info("Start update closest competitions");
        List<CompetitionEntity> competitions = competitionService.findAllOrderByBeginDateDesc();
        LocalDate date = LocalDate.now();
        competitions.stream()
                .filter(competition ->
                        !finishedOrCanceledStatuses.contains(competition.getStatus()) && isWithinOneWeekFromBeginDate(competition, date))
                .forEach(competition -> {
                    log.info("[UCCD]Need to update competition with id: {}, begin date: {}", competition.getId(), competition.getBeginDate());
                    runEventToUpdateCompetition(competition);
                });
    }

    private boolean isWithinOneWeekFromBeginDate(CompetitionEntity competition, LocalDate date) {
        LocalDate beginDate = competitionService.getParsedDate(competition.getBeginDate());
        LocalDate oneWeekFromNow = date.plusWeeks(1);

        return !beginDate.isAfter(oneWeekFromNow);
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

    @Override
    public int getOrder() {
        return SpringApp.ORDER_FOR_COMPETITION_UPDATER;
    }
}
