package com.example.service;

import com.example.elements.Heat;
import com.example.elements.Participant;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class EventPageParser {

    public List<Heat> getHeats(Document document) {
        List<Heat> heats = new ArrayList<>();
        AtomicInteger count = new AtomicInteger();

        Elements heatTables = document.select("div.table-responsive table.table");
        heatTables.forEach(heatTable -> {
            String heatName = "Heat " + count.getAndIncrement();

            Heat heat = new Heat(heatName);

            Elements rows = heatTable.select("tr");
            rows.forEach(row -> {
                Elements cells = row.select("td");
                if (cells.size() == 11) {
                    Participant participant = new Participant();
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

            if (heat.getParticipants().size() != 0) heats.add(heat);
        });

        return heats;
    }
}