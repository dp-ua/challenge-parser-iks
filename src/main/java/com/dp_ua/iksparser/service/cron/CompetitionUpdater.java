package com.dp_ua.iksparser.service.cron;

import com.dp_ua.iksparser.SpringApp;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.Ordered;
import org.springframework.scheduling.annotation.Scheduled;

//@Component  todo uncomment after implement
public class CompetitionUpdater implements ApplicationListener<ContextRefreshedEvent>, Ordered {
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        // todo check is need to update competition
    }

    // todo every day at 2:00 update all competition
    @Scheduled(cron = "0 0 2 * * *")
    public void updateCompetition() {
        // todo update all competition
    }

    // todo every 1 hour check not filled events
    @Scheduled(cron = "0 0 * * * *")
    public void checkNotFilledEvents() {
        // todo check not filled heats
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
