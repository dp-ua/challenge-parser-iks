package com.example;

import com.example.elements.Day;
import com.example.elements.Match;
import com.example.service.CompetitionPageParser;
import com.example.service.Downloader;
import com.example.service.ServiceParser;
import org.jsoup.nodes.Document;

import java.util.ArrayList;
import java.util.List;

/**
 * Hello world!
 */
public class App {
    private static final Downloader downloader = Downloader.INSTANCE;
    private static final ServiceParser serviceParser = new ServiceParser();
    private static final String surname = "Решетило";
    private static final String URL = "https://iks.org.ua/competitions1/en/2023.12.15-16_kyiv/live?s=333DE691-FB1E-4E01-B46C-1F52A5D9D6CC";

    public static void main(String[] args) {
        Document document = downloader.getDocument(URL);

        CompetitionPageParser competitionPageParser = new CompetitionPageParser(serviceParser, downloader);

        List<Day> days = competitionPageParser.getParsedDays(document);
        printCompetitionInfo(days);
        findEventsWithParticipant(days, surname);
    }

    private static void findEventsWithParticipant(List<Day> days, String surname) {
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
