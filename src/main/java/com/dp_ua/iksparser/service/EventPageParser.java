package com.dp_ua.iksparser.service;

import com.dp_ua.iksparser.element.HeatEntity;
import com.dp_ua.iksparser.element.ParticipantEntity;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class EventPageParser {

    public List<HeatEntity> getHeats(Document document) {
        List<HeatEntity> heats = new ArrayList<>();
        AtomicInteger count = new AtomicInteger();

        Elements heatTables = document.select("div.table-responsive table.table");
        heatTables.forEach(heatTable -> {
            String heatName = "Heat " + count.getAndIncrement();

            HeatEntity heat = new HeatEntity(heatName);

            Elements rows = heatTable.select("tr");
            rows.forEach(row -> {
                Elements cells = row.select("td");
                if (cells.size() == 11) {
                    ParticipantEntity participant = new ParticipantEntity();
                    participant.setLane(cells.get(0).text());
                    participant.setBib(cells.get(1).text());
//                    participant.setSb(cells.get(2).text());
//                    participant.setPb(cells.get(3).text());
//                    participant.setPhotoUrl(cells.get(4).select("img").attr("src"));
                    participant.setSurname(cells.get(5).text().split(" ")[0]);
                    participant.setName(cells.get(6).text().split(" ")[0]);
                    participant.setBorn(cells.get(7).text());
//                    participant.setCountry(cells.get(8).text());
//                    participant.setTeam(cells.get(9).text());
//                    participant.setCoaches(cells.get(10).text());
                    participant.setUrl(cells.get(4).select("a").attr("href"));

                    heat.addParticipant(participant);
                }
            });

            if (!heat.getParticipants().isEmpty()) heats.add(heat);
        });

        return heats;
    }
}