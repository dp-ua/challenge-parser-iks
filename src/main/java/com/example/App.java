package com.example;

import com.example.elements.Day;
import com.example.elements.Match;
import com.example.service.CompetitionPageParser;
import com.example.service.Downloader;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Hello world!
 */
@Component
public class App {
    @Autowired
    private Downloader downloader;
    @Autowired
    private CompetitionPageParser competitionPageParser;

    public void start(String url, String surname) {
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
