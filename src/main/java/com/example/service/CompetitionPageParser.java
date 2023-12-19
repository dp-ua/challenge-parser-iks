package com.example.service;

import com.example.elements.Day;
import com.example.elements.Event;
import com.example.elements.Heat;
import lombok.extern.java.Log;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

@Log
public class CompetitionPageParser {
    private ServiceParser serviceParser;
    private Downloader downloader;

    public CompetitionPageParser(ServiceParser serviceParser, Downloader downloader) {
        this.serviceParser = serviceParser;
        this.downloader = downloader;
    }

    public List<Day> getParsedDays(Document document) {
        List<Day> days = getUnfilledDays(document);
        days.forEach(day -> {
            List<Event> events = getEvents(document, day.getDateId());
            events.forEach(event -> {
                day.addEvent(event);
            });
        });
        return days;
    }

    public List<Day> getUnfilledDays(Document document) {
        Elements days = document.select("ul#timetable li a");
        checkDays(days);
        List<Day> result = new ArrayList<>();
        for (Element day : days) {
            String dayText = day.text();
            String dayId = day.attr("href").substring(1);
            result.add(serviceParser.parseDay(dayText + " (" + dayId + ")"));
        }
        log.info("Days count: " + result.size());
        return result;
    }

    private void checkDays(Elements days) {
        if (days.size() == 0) {
            throw new RuntimeException("Wrong number of days");
        }
    }

    private List<Event> getUnfilledEvents(Document document, String dayId) {
        List<Event> events = new ArrayList<>();
        Elements rows = document.select("div#timetable-" + dayId + " table tbody tr");
        checkEvents(rows);
        for (Element row : rows) {
            Event event = parseEvent(row);
            events.add(event);
        }
        log.info("For day[" + dayId + "] Events count: " + events.size());
        return events;
    }

    public List<Event> getEvents(Document document, String dayId) {
        EventPageParser pageParser = new EventPageParser();

        List<Event> events = getUnfilledEvents(document, dayId);

        events.forEach(event -> {
            Document eventDocument = downloader.getDocument(event.getStartListUrl());
            List<Heat> heats = pageParser.getHeats(eventDocument);
            heats.forEach(heat -> {
                event.addHeat(heat);
            });
            log.info("For event[" + event.getEventName() + "] Heats count: " + heats.size());
        });

        return events;

    }

    private void checkEvents(Elements rows) {
        if (rows.size() == 0) {
            throw new RuntimeException("Wrong number of events");
        }
    }

    public Event parseEvent(Element row) {
        Elements columns = row.select("td");
        String time = columns.get(0).text();
        String eventName = columns.get(1).childNodes().get(0).childNodes().get(0).toString();
        String category = columns.get(2).textNodes().get(0).text();

        String round = columns.get(3).text();
        String startListUrl = null;
        try {
            startListUrl = columns.get(4).select("button").attr("onclick").split("'")[1];
        } catch (Exception e) {
            log.info("No start list url for event: " + eventName);
        }

        return new Event(time, eventName, category, round, startListUrl);
    }
}
