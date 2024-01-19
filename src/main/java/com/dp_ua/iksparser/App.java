package com.dp_ua.iksparser;

import com.dp_ua.iksparser.bot.controller.ControllerService;
import com.dp_ua.iksparser.element.DayEntity;
import com.dp_ua.iksparser.element.Match;
import com.dp_ua.iksparser.service.CompetitionPageParser;
import com.dp_ua.iksparser.service.Downloader;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.generics.BotSession;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class App implements ApplicationListener<ContextRefreshedEvent>, Ordered {
    @Autowired
    private Downloader downloader;
    @Autowired
    private CompetitionPageParser competitionPageParser;

    @Autowired
    ControllerService botController;
    private final static String surname = "Решетило";
    private final static String URL = "https://iks.org.ua/competitions1/en/2024.01.13_kyiv/live?s=0257D292-2228-4316-9141-DF185D18CDCF";

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        //        parseURL(URL, surname);
        BotSession botSession = botController.botConnect();
        // todo save botSession to memory
        log.info("BotSession: " + botSession);
    }

    @Override
    public int getOrder() {
        return SpringApp.ORDER_FOR_APP_AND_BOT_STARTER;
    }

    private void parseURL(String url, String surname) {
        Document document = downloader.getDocument(url);

        List<DayEntity> days = competitionPageParser.getParsedDays(document);
        printCompetitionInfo(days);
        findEventsWithParticipant(days, surname);
    }

    private void findEventsWithParticipant(List<DayEntity> days, String surname) {
        List<Match> matches = new ArrayList<>();

        days.forEach(day -> matches.addAll(day.findMatchesBySurname(surname)));

        System.out.printf("Для участника [%s] найдено видов для участия: %s\n", surname, matches.size());
        matches.forEach(match -> {
            System.out.printf("%s %s:\n", match.getParticipant().getSurname(), match.getParticipant().getName());
            System.out.printf("%s: %s [%-12s] %s(%s) [%s]\n",
                    match.getDay().getDayName(),
                    match.getEvent().getTime(),
                    match.getEvent().getCategory(),
                    match.getHeat().getName(),
                    match.getEvent().getHeats().size(),
                    match.getEvent().getEventName());
        });
    }

    private static void printCompetitionInfo(List<DayEntity> days) {
        days.forEach(day -> {
            String dayString = "(" + day.getDayName() + ") " + day.getDate();
            System.out.println("Day: " + dayString);

            day.getEvents().forEach(event -> System.out.printf("%s: %s %-14s heats: %-2s %s\n",
                    day.getDayName(),
                    event.getTime(),
                    event.getCategory(),
                    event.getHeats().size(),
                    event.getEventName()));
        });
    }
}
