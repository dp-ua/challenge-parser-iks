package com.dp_ua.iksparser;

import com.dp_ua.iksparser.bot.command.CommandProvider;
import com.dp_ua.iksparser.bot.controller.ControllerService;
import com.dp_ua.iksparser.element.Day;
import com.dp_ua.iksparser.element.Match;
import com.dp_ua.iksparser.service.CompetitionPageParser;
import com.dp_ua.iksparser.service.Downloader;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.generics.BotSession;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class App {

    @Autowired
    private Downloader downloader;
    @Autowired
    private CompetitionPageParser competitionPageParser;
    @Autowired
    private CommandProvider commandProvider;

    @Autowired
    ControllerService botController;


    public void start(String url, String surname) {
//        parseURL(url, surname);
        BotSession botSession = botController.botConnect();
        // todo save botSession to memory
        log.info("BotSession: " + botSession);
    }


    private void parseURL(String url, String surname) {
        Document document = downloader.getDocument(url);

        List<Day> days = competitionPageParser.getParsedDays(document);
        printCompetitionInfo(days);
        findEventsWithParticipant(days, surname);
    }

    private void findEventsWithParticipant(List<Day> days, String surname) {
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

    private static void printCompetitionInfo(List<Day> days) {
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
