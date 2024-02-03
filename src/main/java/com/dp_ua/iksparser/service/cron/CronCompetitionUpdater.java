package com.dp_ua.iksparser.service.cron;

import com.dp_ua.iksparser.SpringApp;
import com.dp_ua.iksparser.bot.event.UpdateCompetitionEvent;
import com.dp_ua.iksparser.dba.element.CompetitionEntity;
import com.dp_ua.iksparser.dba.element.EventEntity;
import com.dp_ua.iksparser.dba.element.UpdateStatusEntity;
import com.dp_ua.iksparser.dba.service.EventService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.Ordered;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
@Slf4j
public class CronCompetitionUpdater implements ApplicationListener<ContextRefreshedEvent>, Ordered {
    @Autowired
    EventService eventService;
    @Autowired
    ApplicationEventPublisher publisher;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        // todo check is need to update competition
    }

    @Scheduled(cron = "0 0/20 * * * *") // every 20 minutes check not filled events
    public void checkNotFilledEvents() {
        Set<CompetitionEntity> needToUpdateCompetitionIds = new HashSet<>();
        eventService.findAll().stream()
                .filter(EventEntity::isNotFilled)
                .forEach(event -> {
                    needToUpdateCompetitionIds.add(event.getDay().getCompetition());
                });
        needToUpdateCompetitionIds.forEach(competition -> {
            log.info("Need to update competition with id: {}", competition.getId());
            updateCompetition(competition);
        });
    }

    private void updateCompetition(CompetitionEntity competition) {
        // todo move to separate class
        UpdateStatusEntity message = new UpdateStatusEntity();
        message.setCompetitionId(competition.getId());
        message.setChatId("");
        message.setEditMessageId(null);
        UpdateCompetitionEvent updateCompetitionEvent = new UpdateCompetitionEvent(this, message);
        publisher.publishEvent(updateCompetitionEvent);
    }

    // todo every day at 2:00 update all competition
    @Scheduled(cron = "0 0 2 * * *")
    public void updateCompetition() {
        // todo update all competition
    }

    // todo every 2 hours update information about closest competitions
    @Scheduled(cron = "0 0 */2 * * *")
    public void updateClosestCompetitions() {
        // todo update information about closest competitions
    }

    @Override
    public int getOrder() {
        return SpringApp.ORDER_FOR_COMPETITION_UPDATER;
    }
}
