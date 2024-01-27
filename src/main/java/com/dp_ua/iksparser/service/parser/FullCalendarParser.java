package com.dp_ua.iksparser.service.parser;

import com.dp_ua.iksparser.dba.element.CompetitionEntity;
import com.dp_ua.iksparser.service.Downloader;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class FullCalendarParser implements MainParserService {
    @Autowired
    Downloader downloader;

    private static final String URL = "https://www.iks.org.ua/calendar/?search_year={$year}" +
            "&search_month=&search_country=%D0%A3%D0%BA%D1%80%D0%B0%D1%97%D0%BD%D0%B0" +
            "&search_territory=&search_city=&search_name=" +
            "&search_filter=%D0%9F%D0%BE%D1%88%D1%83%D0%BA";

    @Override
    public List<CompetitionEntity> parseCompetitions() {
        Document document = downloadDocument();
        List<CompetitionEntity> result = new ArrayList<>();
        if (document != null) {
            result = getParsedCompetitions(document);
        }
        return result;
    }

    private List<CompetitionEntity> getParsedCompetitions(Document document) {
        Elements allRows = document
                .select("table")
                .get(0)
                .select("tbody tr");

        return getFilteredCells(allRows)
                .stream()
                .map(FullCalendarParser::getCompetitionEntity)
                .collect(Collectors.toList());
    }

    private static CompetitionEntity getCompetitionEntity(Element row) {
        Elements cells = row.getElementsByTag("td");
        CompetitionEntity competition = new CompetitionEntity();
        competition.setStatus(cells.get(0).text());
        competition.setBeginDate(cells.get(1).text());
        competition.setEndDate(cells.get(2).text());
        competition.setName(cells.get(3).text());
        competition.setCountry("UKR");
        competition.setCity(cells.get(5).text());
        competition.setUrl(cells.get(8).select("a").attr("href"));
        return competition;
    }

    private Elements getFilteredCells(Elements rows) {
        Elements result = new Elements();
        boolean ignore = true;
        for (Element row : rows) {
            Elements tds = row.getElementsByTag("td");
            if (ignore) {
                if ("Легка атлетика" .contains(tds.get(0).text())) {
                    ignore = false;
                }
            } else {
                if (tds.size() == 2) {
                    break;
                }
                result.add(row);
            }
        }
        return result;
    }

    private Document downloadDocument() {
        String url = getURL(getYearNow());
        log.info("Start parsing competitions from url: {}", url);
        return downloader.getDocument(url);
    }

    private String getURL(int year) {
        return URL.replace("{$year}", String.valueOf(year));
    }

    private int getYearNow() {
        return LocalDate.now().getYear();
    }
}