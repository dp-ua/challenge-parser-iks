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
        // First start application. Check if competitions in DB and update if not
        long count = competitionService.count();
        if (count == 0) {
            log.info("No competitions in DB");
            update(LocalDate.now().getYear());
        }
    }

    @Scheduled(cron = "0 0 0/1 * * *") // update competitions list every 1 hour
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

    private void updateCompetitions(List<CompetitionEntity> competitions) {
        log.info("Going to update competitions. Found {} ", competitions.size());
        competitions.stream()
                .filter(CompetitionEntity::isCanBeUpdated)
                .forEach(competition -> {
                    log.info("Update competition with id: {} begin date: {}", competition.getId(), competition.getBeginDate());
                    runEventToUpdateCompetition(competition);
                });
    }

    @Scheduled(cron = "0 0/9 10-23 * * *") // every 9 minutes check not filled events from 10 to 23
    public void checkNotFilledEvents() {
        List<CompetitionEntity> competitions = eventService.findAll().stream()
                .filter(EventEntity::isNotFilled)
                .map(eventEntity -> eventEntity.getDay().getCompetition())
                .distinct()
                .toList();
        log.info("Update competitions with not filled events. Found {} ", competitions.size());
        updateCompetitions(competitions.stream().toList());
    }

    @Scheduled(cron = "0 15 6 * * *") // every day at 6:15 update all competition
    @Transactional
    public void updateAllCompetitionsDetails() {
        List<CompetitionEntity> competitions = competitionService.findAllOrderByUpdated()
                .stream()
                .filter(CompetitionEntity::isCanBeUpdated)
                .toList();
        log.info("Update All competitions. Found {} ", competitions.size());
        updateCompetitions(competitions);
    }

    @Scheduled(cron = "0 0/20 10-23 * * *") // every 20 minutes check closest competitions
    public void updateClosestCompetitionDetails() {
        LocalDate date = LocalDate.now();
        List<CompetitionEntity> competitions = competitionService.findAllOrderByBeginDateDesc()
                .stream()
                .filter(competition ->
                        !finishedOrCanceledStatuses.contains(competition.getStatus()) && isWithinOneWeekFromBeginDate(competition, date))
                .toList();
        log.info("Update closest competitions. Found {} ", competitions.size());
        updateCompetitions(competitions);
    }

    private boolean isWithinOneWeekFromBeginDate(CompetitionEntity competition, LocalDate date) {
        LocalDate beginDate = competitionService.getParsedDate(competition.getBeginDate());
        LocalDate oneWeekFromNow = date.plusWeeks(1);

        return !beginDate.isAfter(oneWeekFromNow);
    }

    private void runEventToUpdateCompetition(CompetitionEntity competition) {
        if (competition.isURLEmpty()) {
            log.debug("Can't update. Competition URL is empty, id: {}", competition.getId());
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
