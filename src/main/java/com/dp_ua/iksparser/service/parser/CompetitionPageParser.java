package com.dp_ua.iksparser.service.parser;

import static com.dp_ua.iksparser.exeption.ExceptionType.CANT_PARSE_DAYS;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import com.dp_ua.iksparser.dba.entity.DayEntity;
import com.dp_ua.iksparser.dba.entity.EventEntity;
import com.dp_ua.iksparser.exeption.ParsingException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class CompetitionPageParser {

    public static final String RELAY_HEAT = "Естафетний";
    protected static final String BUTTON = "button";
    protected static final String ONCLICK = "onclick";

    private final ServiceParser serviceParser;

    public List<EventEntity> getUnsavedEvents(Document document, DayEntity day) {
        List<EventEntity> events = new ArrayList<>();
        Elements rows = document.select("div#timetable-" + day.getDateId() + " table tbody tr");
        checkEvents(rows);
        for (Element row : rows) {
            EventEntity event = parseEvent(row);
            if (event.getEventName().contains(RELAY_HEAT)) {
                log.debug("Ignoring relay Event: {}", event.getEventName());
            } else {
                events.add(event);
            }
        }
        log.info("For day[{}] Events count: {}", day.getDateId(), events.size());
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
        log.info("Days count: {}", result.size());
        return result;
    }

    private void checkDays(Elements days) throws ParsingException {
        if (days.isEmpty()) {
            throw new ParsingException("Wrong number of days", CANT_PARSE_DAYS);
        }
    }

    private void checkEvents(Elements rows) {
        if (rows.isEmpty()) {
            throw new IllegalStateException("No events found. Expected non-empty list.");
        }
    }

    public EventEntity parseEvent(Element row) {
        Elements cells = row.select("td");

        String time = parseTime(cells);
        String eventName = parseEventName(cells);
        String category = parseCategory(cells);
        String round = parseRound(cells);
        String startListUrl = parseStartUrl(cells);
        String resultUrl = parseResultUrl(cells);

        return new EventEntity(time, eventName, category, round, startListUrl, resultUrl);
    }

    /**
     * Парсит время из ячейки 0.
     */
    private String parseTime(Elements cells) {
        try {
            return cells.get(0).text();
        } catch (Exception e) {
            log.error("Time not found.");
            return "";
        }
    }

    /**
     * Парсит название события из ячейки 1.
     */
    private String parseEventName(Elements cells) {
        try {
            Element cell = cells.get(1);
            if (cell.childNodes().get(0).childNodes().size() == 4) {
                return cell.childNodes().get(0).childNode(0).childNode(0).toString().trim() +
                        ". (" +
                        cell.childNodes().get(0).childNode(1).toString().trim() +
                        ")";
            }
            return cell.childNodes().get(0).childNodes().get(0).toString().trim();
        } catch (Exception e) {
            log.error("Event name not found.");
            return "";
        }
    }

    /**
     * Парсит категорию из ячейки 2.
     */
    private String parseCategory(Elements cells) {
        try {
            return cells.get(2).textNodes().get(0).text();
        } catch (Exception e) {
            log.error("Category not found.");
            return "";
        }
    }

    /**
     * Парсит раунд из ячейки 3.
     */
    private String parseRound(Elements cells) {
        try {
            return cells.get(3).text().split(" ")[0].toLowerCase();
        } catch (Exception e) {
            log.error("Round not found.");
            return "";
        }
    }

    /**
     * Парсит URL стартового листа из ячейки 4.
     */
    private String parseStartUrl(Elements cells) {
        try {
            Elements buttons = cells.get(4).select(BUTTON);
            for (Element button : buttons) {
                String onclickAttr = button.attr(ONCLICK);
                if (onclickAttr.contains("start")) {
                    return onclickAttr.split("'")[1].trim();
                }
            }
        } catch (Exception e) {
            log.debug("No start list URL found.");
        }
        return "";
    }

    /**
     * Парсит URL результатов из ячеек 4 и 5.
     */
    private String parseResultUrl(Elements cells) {
        try {
            Elements buttons = cells.get(4).select(BUTTON);
            for (Element button : buttons) {
                String onclickAttr = button.attr(ONCLICK);
                if (onclickAttr.contains("result")) {
                    return onclickAttr.split("'")[1].trim();
                }
            }
            if (cells.size() > 5) {
                Elements resultButtons = cells.get(5).select(BUTTON);
                for (Element button : resultButtons) {
                    String onclickAttr = button.attr(ONCLICK);
                    if (onclickAttr.contains("result")) {
                        return onclickAttr.split("'")[1].trim();
                    }
                }
            }
        } catch (Exception e) {
            log.debug("No result URL found.");
        }
        return "";
    }

}
