package com.dp_ua.iksparser.service.parser;

import com.dp_ua.iksparser.dba.element.DayEntity;
import com.dp_ua.iksparser.dba.element.EventEntity;
import com.dp_ua.iksparser.exeption.ParsingException;
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
    @Autowired
    private ServiceParser serviceParser;

    public List<EventEntity> getUnsavedEvents(Document document, DayEntity day) {
        List<EventEntity> events = new ArrayList<>();
        Elements rows = document.select("div#timetable-" + day.getDateId() + " table tbody tr");
        checkEvents(rows);
        for (Element row : rows) {
            EventEntity event = parseEvent(row);
            events.add(event);
        }
        log.info("For day[" + day.getDateId() + "] Events count: " + events.size());
        return events;
    }

    public List<DayEntity> getUnsavedUnfilledDays(Document document) throws ParsingException {
        Elements days = document.select("ul#timetable li a");
        checkDays(days);
        List<DayEntity> result = new ArrayList<>();
        for (Element day : days) {
            String dayText = day.text();
            String dayId = day.attr("href").substring(1);
            DayEntity dayEntity = serviceParser.parseDay(dayText + " (" + dayId + ")");
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

    private void checkEvents(Elements rows) {
        if (rows.isEmpty()) {
            throw new RuntimeException("Wrong number of events");
        }
    }

    public EventEntity parseEvent(Element row) {
        Elements cells = row.select("td");
        String time = cells.get(0).text();
        String eventName = getEventName(cells.get(1));
        String category = cells.get(2).textNodes().get(0).text();
        String round = cells.get(3).text().split(" ")[0].toLowerCase();
        String startListUrl = "";
        try {
            startListUrl = cells.get(4).select("button").attr("onclick").split("'")[1].trim();
        } catch (Exception e) {
            log.debug("No start list url for event:{} {}", eventName, category);
        }
        String resultUrl = "";
        try {
            resultUrl = cells.get(5).select("button").attr("onclick").split("'")[1].trim();
        } catch (Exception e) {
            log.debug("No result url for event:{} {}", eventName, category);
        }

        return new EventEntity(time, eventName, category, round, startListUrl, resultUrl);
    }

    private static String getEventName(Element cell) {
        if (cell.childNodes().get(0).childNodes().size() == 4) {
            return cell.childNodes().get(0).childNode(0).childNode(0).toString().trim() +
                    ". (" +
                    cell.childNodes().get(0).childNode(1).toString().trim() +
                    ")";
        }
        return cell.childNodes().get(0).childNodes().get(0).toString().trim();
    }
}
