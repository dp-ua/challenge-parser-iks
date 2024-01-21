package com.dp_ua.iksparser;

import com.dp_ua.iksparser.bot.controller.ControllerService;
import com.dp_ua.iksparser.service.CompetitionPageParser;
import com.dp_ua.iksparser.service.Downloader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.generics.BotSession;

@Component
@Slf4j
public class App implements ApplicationListener<ContextRefreshedEvent>, Ordered {
    @Autowired
    private Downloader downloader;
    @Autowired
    private CompetitionPageParser competitionPageParser;

    @Autowired
    ControllerService botController;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        BotSession botSession = botController.botConnect();
        // todo save botSession to memory
        log.info("BotSession: " + botSession);

    }
    @Override
    public int getOrder() {
        return SpringApp.ORDER_FOR_APP_AND_BOT_STARTER;
    }
}
