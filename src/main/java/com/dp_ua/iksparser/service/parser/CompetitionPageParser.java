package com.dp_ua.iksparser.service.parser;

import com.dp_ua.iksparser.dba.element.DayEntity;
import com.dp_ua.iksparser.dba.element.EventEntity;
import com.dp_ua.iksparser.dba.element.HeatEntity;
import com.dp_ua.iksparser.dba.service.DayService;
import com.dp_ua.iksparser.dba.service.EventService;
import com.dp_ua.iksparser.exeption.ParsingException;
import com.dp_ua.iksparser.service.Downloader;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static com.dp_ua.iksparser.exeption.ExceptionType.CANT_PARSE_DAYS;

@Slf4j
@Component
public class CompetitionPageParser {
    private final ServiceParser serviceParser;
    private final Downloader downloader;
    private final EventPageParser pageParser;
    @Autowired
    private EventService eventService;
    @Autowired
    private DayService dayService;

    public CompetitionPageParser(ServiceParser serviceParser, Downloader downloader, EventPageParser pageParser) {
        this.serviceParser = serviceParser;
        this.downloader = downloader;
        this.pageParser = pageParser;
    }

    public List<DayEntity> getParsedDays(Document document) throws ParsingException {
        List<DayEntity> days = getUnfilledDays(document);
        days.forEach(day -> {
            List<EventEntity> events = getEvents(document, day.getDateId());
            events.forEach(event -> {
                day.addEvent(event);
                event.setDay(day);
                dayService.save(day);
            });
        });
        return days;
    }

    public List<DayEntity> getUnfilledDays(Document document) throws ParsingException {
        Elements days = document.select("ul#timetable li a");
        checkDays(days);
        List<DayEntity> result = new ArrayList<>();
        for (Element day : days) {
            String dayText = day.text();
            String dayId = day.attr("href").substring(1);
            DayEntity dayEntity = serviceParser.parseDay(dayText + " (" + dayId + ")");
            dayService.save(dayEntity);
            result.add(dayEntity);
        }
        log.info("Days count: " + result.size());
        return result;
    }

    private void checkDays(Elements days) throws ParsingException {
        if (days.isEmpty()) {
            throw new ParsingException("Wrong number of days", CANT_PARSE_DAYS);
        }
    }

    private List<EventEntity> getUnfilledEvents(Document document, String dayId) {
        List<EventEntity> events = new ArrayList<>();
        Elements rows = document.select("div#timetable-" + dayId + " table tbody tr");
        checkEvents(rows);
        for (Element row : rows) {
            EventEntity event = parseEvent(row);
            eventService.save(event);
            events.add(event);
        }
        log.debug("For day[" + dayId + "] Events count: " + events.size());
        return events;
    }

    public List<EventEntity> getEvents(Document document, String dayId) {

        List<EventEntity> events = getUnfilledEvents(document, dayId);

        events.forEach(event -> {
            String url = event.getStartListUrl();
            if (url == null || url.isEmpty()) {
                log.info("No start list url for event: " + event.getEventName());
                return;
            }
            Document eventDocument = downloader.getDocument(url);
            List<HeatEntity> heats = pageParser.getHeats(eventDocument);
            heats.forEach(heat -> {
                event.addHeat(heat);
                heat.setEvent(event);
                eventService.save(event);
            });
            log.debug("For event[" + event.getEventName() + "] Heats count: " + heats.size());
        });
        return events;
    }

    private void checkEvents(Elements rows) {
        if (rows.isEmpty()) {
            throw new RuntimeException("Wrong number of events");
        }
    }

    public EventEntity parseEvent(Element row) {
        Elements columns = row.select("td");
        String time = columns.get(0).text();
        String eventName = removeTags(columns.get(1).childNodes().get(0).childNodes().get(0).toString());

        String category = columns.get(2).textNodes().get(0).text();

        String round = columns.get(3).text().split(" ")[0].toLowerCase();
        String startListUrl = null;
        try {
            startListUrl = columns.get(4).select("button").attr("onclick").split("'")[1];
        } catch (Exception e) {
            log.info("No start list url for event:{} {}", eventName, category);
        }

        return new EventEntity(time, eventName, category, round, startListUrl);
    }

    private String removeTags(String string) {
        return string.replaceAll("<.*?>", "");
    }
}
