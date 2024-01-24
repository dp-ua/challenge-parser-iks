package com.dp_ua.iksparser.service.cron;

import com.dp_ua.iksparser.App;
import com.dp_ua.iksparser.bot.abilities.StateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.generics.BotSession;

//@Component
@Slf4j
public class BotChecker {
    @Autowired
    StateService stateService;
    @Autowired
    App app;

    // не работает. После выхода из спячки пишет, что проверяет подключение, и не перезапускает его.
    // При этом, если запустить приложение, то бот подключается.
//    @Scheduled(cron = "0 * * * * *")
    public void checkBot() {
        log.info("Checking bot connection");
        BotSession botSession = stateService.getBotSession();
        if (botSession == null) {
            log.warn("BotSession is null");
            app.connectBot();
            return;
        }
        if (!botSession.isRunning()) {
            log.warn("BotSession is not running. RECONNECTING");
            app.connectBot();
        }

    }
}
