package com.dp_ua.iksparser.service.parser;

import com.dp_ua.iksparser.dba.element.CompetitionEntity;
import com.dp_ua.iksparser.service.Downloader;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

//@Component  //no need more
public class IksPageParser implements MainParserService {
    public static final String URL = "https://iks.org.ua";
    public static final String COMPETITIONS_PAGE = "/competitions1/";

    @Autowired
    Downloader downloader;

    @Override
    public List<CompetitionEntity> parseCompetitions() {
        return getParsedCompetitions(downloadDocument());
    }

    private Document downloadDocument() {
        return downloader.getDocument(URL + COMPETITIONS_PAGE);
    }

    public List<CompetitionEntity> getParsedCompetitions(Document document) {
        List<CompetitionEntity> result = new ArrayList<>();
        Elements results = document.select("div.row.results");
        results.forEach(row -> {
            Elements cols = row.select("div.col-md-12");
            cols.forEach(col -> {
                Elements tables = col.select("div.table-responsive table.table");
                tables.forEach(table -> {
                    Elements rows = table.select("tbody tr");
                    rows.forEach(tr -> {
                        Elements cells = tr.select("td");
                        if (cells.size() == 8) {
                            CompetitionEntity competition = new CompetitionEntity();
                            competition.setUrl(cells.get(0).select("a").attr("href"));
                            competition.setBeginDate(parseDate(cells.get(1).text()).toString());
                            competition.setEndDate(parseDate(cells.get(2).text()).toString());
                            competition.setName(cells.get(3).text());
                            competition.setCountry(cells.get(4).text());
                            competition.setCity(cells.get(6).text());

                            result.add(competition);
                        }
                    });
                });
            });
        });
        return result;
    }

    private static LocalDate parseDate(String text) {
        String[] parts = text.split("\\.");
        return LocalDate.of(Integer.parseInt(parts[2]), Integer.parseInt(parts[1]), Integer.parseInt(parts[0]));
    }
}