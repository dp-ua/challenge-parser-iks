package com.dp_ua.iksparser.service;

import com.dp_ua.iksparser.element.Competition;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class MainPageParser {
    public List<Competition> getParsedCompetitions(Document document) {
        List<Competition> result = new ArrayList<>();
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
                            Competition competition = new Competition();
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


// Main page document structure:
//<div class="row results">
//<div class="col-md-12">
//<div class="table-responsive">
//<table class="table">
//<thead>
//<th>&nbsp;</th>
//<th>Begin</th>
//<th>End</th>
//<th>Competition</th>
//<th>Країна</th>
//<th>Ter.</th>
//<th>City</th>
//<th>Results</th>
//</thead>
//<tbody>
//<tr>
//<td><a href="/competitions1/en/2024.01.16_kyiv" target="_blank" title="Chernihivska Regional Indoor Championships"><img src="competitions/2024.01.16_kyiv/images/logo_competitions.png" alt="Chernihivska Regional Indoor Championships"></a></td>
//<td>16.01.2024</td>
//<td>16.01.2024</td>
//<td>Chernihivska Regional Indoor Championships</td>
//<td style="white-space: nowrap;"><img src="images/country/ukr.png" alt="images/country/ukr.png"/>&nbsp;UKR</td>
//<td><img src="images/oblast/kyiv.png" alt=""/></td>
//<td>Kyiv</td>
//<td>
//<button type="button" class="btn btn-primary" onclick='window.open("/competitions1/en/2024.01.16_kyiv/live?s=E751378B-22B7-4A3A-9E56-42CC1BE0EB97", "_blank")'>
//<span class="glyphicon glyphicon-eye-open" aria-hidden="true"></span>
//</button>
//</td>
//</tr>
//<tr>
//<td><a href="/competitions1/en/2024.01.13_kyiv" target="_blank" title="National Indoor Competitions White Panter Cup on prizes Olha Saladukha"><img src="competitions/2024.01.13_kyiv/images/logo_competitions.png" alt="National Indoor Competitions White Panter Cup on prizes Olha Saladukha"></a></td>
//<td>13.01.2024</td>
//<td>13.01.2024</td>
//<td>National Indoor Competitions White Panter Cup on prizes Olha Saladukha</td>
//<td style="white-space: nowrap;"><img src="images/country/ukr.png" alt="images/country/ukr.png"/>&nbsp;UKR</td>
//<td><img src="images/oblast/kyiv.png" alt=""/></td>
//<td>Kyiv</td>
//<td>
//<button type="button" class="btn btn-primary" onclick='window.open("/competitions1/en/2024.01.13_kyiv/live?s=EA04BF03-9F01-4CCA-B99B-200B8D21C29C", "_blank")'>
//<span class="glyphicon glyphicon-eye-open" aria-hidden="true"></span>
//</button>
//</td>
//</tr>