package com.dp_ua.iksparser.service.cron;

import static com.dp_ua.iksparser.dba.entity.CompetitionStatus.C_CANCELED;
import static com.dp_ua.iksparser.dba.entity.CompetitionStatus.C_FINISHED;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.Ordered;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.dp_ua.iksparser.SpringApp;
import com.dp_ua.iksparser.bot.abilities.competition.CompetitionFacade;
import com.dp_ua.iksparser.dba.entity.CompetitionEntity;
import com.dp_ua.iksparser.dba.service.CompetitionService;
import com.dp_ua.iksparser.exeption.ParsingException;
import com.dp_ua.iksparser.service.CompetitionUpdatePolicy;
import com.dp_ua.iksparser.service.CompetitionUpdateQueue;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class CronCompetitionUpdater implements ApplicationListener<ContextRefreshedEvent>, Ordered {

    private final CompetitionService competitionService;
    private final CompetitionFacade competitionFacade;
    private final CompetitionUpdateQueue updateQueue;
    private final CompetitionUpdatePolicy updatePolicy;

    private final Set<String> finishedOrCanceledStatuses = Set.of(C_FINISHED.getName(), C_CANCELED.getName());

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

    private void updateCompetitions(String job, List<CompetitionEntity> competitions) {
        List<CompetitionEntity> canBeUpdated = competitions.stream()
                .filter(CompetitionEntity::isCanBeUpdated)
                .toList();
        List<CompetitionEntity> toUpdate = canBeUpdated.stream()
                .filter(competition -> !updatePolicy.isStale(competition))
                .toList();
        log.info("[{}] Competitions: {}, stale (manual update only): {}, queued for update: {}",
                job, competitions.size(), canBeUpdated.size() - toUpdate.size(), toUpdate.size());
        toUpdate.forEach(this::requestCompetitionUpdate);
    }

    @Scheduled(cron = "0 0/9 10-23 * * *") // every 9 minutes check not filled events from 10 to 23
    public void checkNotFilledEvents() {
        LocalDate today = LocalDate.now();
        List<CompetitionEntity> competitions = competitionService.findCompetitionsWithNotFilledEvents().stream()
                .filter(competition -> updatePolicy.isInFrequentWindow(competition, today))
                .toList();
        updateCompetitions("not filled events", competitions);
    }

    @Scheduled(cron = "0 15 6 * * *") // every day at 6:15 update all competition
    @Transactional
    public void updateAllCompetitionsDetails() {
        LocalDate today = LocalDate.now();
        Set<Long> filledIds = competitionService.findFilledCompetitionIds();
        List<CompetitionEntity> all = competitionService.findAllOrderByUpdated();
        List<CompetitionEntity> competitions = all.stream()
                .filter(competition -> !updatePolicy.isFrozen(competition, today, filledIds.contains(competition.getId())))
                .toList();
        log.info("[all] Frozen competitions (old and already loaded, manual update only): {}", all.size() - competitions.size());
        updateCompetitions("all", competitions);
    }

    @Scheduled(cron = "0 0/20 10-23 * * *") // every 20 minutes check closest competitions
    public void updateClosestCompetitionDetails() {
        LocalDate date = LocalDate.now();
        List<CompetitionEntity> competitions = competitionService.findAllOrderByBeginDateDesc()
                .stream()
                .filter(competition ->
                        !finishedOrCanceledStatuses.contains(competition.getStatus()) && isWithinOneWeekFromBeginDate(competition, date))
                .filter(competition -> updatePolicy.isInFrequentWindow(competition, date))
                .toList();
        updateCompetitions("closest", competitions);
    }

    private boolean isWithinOneWeekFromBeginDate(CompetitionEntity competition, LocalDate date) {
        LocalDate beginDate = competitionService.getParsedDate(competition.getBeginDate());
        LocalDate oneWeekFromNow = date.plusWeeks(1);

        return !beginDate.isAfter(oneWeekFromNow);
    }

    private void requestCompetitionUpdate(CompetitionEntity competition) {
        if (competition.isURLEmpty()) {
            log.debug("Can't update. Competition URL is empty, id: {}", competition.getId());
            return;
        }
        if (competition.isUrlNotValid()) {
            log.warn("Can't update. Competition URL is not valid, id: {}", competition.getId());
            return;
        }
        updateQueue.requestUpdate(competition.getId());
    }

    @Override
    public int getOrder() {
        return SpringApp.ORDER_FOR_COMPETITION_UPDATER;
    }

}
