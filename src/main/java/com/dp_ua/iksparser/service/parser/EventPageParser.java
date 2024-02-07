package com.dp_ua.iksparser.service.parser;

import com.dp_ua.iksparser.dba.element.CoachEntity;
import com.dp_ua.iksparser.dba.element.HeatEntity;
import com.dp_ua.iksparser.dba.element.HeatLineEntity;
import com.dp_ua.iksparser.dba.element.ParticipantEntity;
import com.dp_ua.iksparser.dba.service.CoachService;
import com.dp_ua.iksparser.dba.service.HeatLineService;
import com.dp_ua.iksparser.dba.service.HeatService;
import com.dp_ua.iksparser.dba.service.ParticipantService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Slf4j
public class EventPageParser {
    @Autowired
    private HeatService heatService;
    @Autowired
    private ParticipantService participantService;
    @Autowired
    private CoachService coachService;
    @Autowired
    private HeatLineService heatLineService;

    @Transactional
    public List<HeatEntity> getHeats(Document document) {
        List<HeatEntity> heats = new ArrayList<>();
        AtomicInteger count = new AtomicInteger();

        Elements heatTables = document.select("div.table-responsive table.table");
        heatTables.forEach(heatTable -> {
            String heatName = "Забіг " + count.getAndIncrement();
            HeatEntity heat = new HeatEntity();
            heat.setName(heatName);
            heatService.save(heat);

            Elements rows = heatTable.select("tr");
            rows.stream().map(row -> row.select("td")).filter(cells -> cells.size() == 11).forEach(cells -> {
                HeatLineEntity heatLine = new HeatLineEntity();
                heatLine.setLane(cells.get(0).text());
                heatLine.setBib(cells.get(1).text());
                heatLineService.save(heatLine);

                heat.addHeatLine(heatLine);
                heatLine.setHeat(heat);

                String surname = cells.get(5).childNode(0).childNode(0).toString();
                String name = cells.get(6).childNode(0).childNode(0).toString();
                String team = cells.get(9).text();
                String region = cells.get(8).text();
                String born = cells.get(7).text();
                ParticipantEntity participant = participantService.findBySurnameAndNameAndTeamAndRegionAndBorn(
                        surname,
                        name,
                        team,
                        region,
                        born
                );
                if (participant == null) {
                    participant = new ParticipantEntity();
                    participant.setUrl(cells.get(4).select("a").attr("href"));
                    participant.setSurname(surname);
                    participant.setName(name);
                    participant.setBorn(born);
                    participant.setRegion(region);
                    participant.setTeam(team);
                    participantService.save(participant);
                }

                saveRelationBetweenHeatLineAndParticipant(heatLine, participant);

                String[] coaches = cells.get(10).text().split(",");
                for (String coachName : coaches) {
                    name = coachName.trim();
                    if (name.isEmpty()) {
                        continue;
                    }
                    CoachEntity coach = coachService.findByName(name);
                    if (coach == null) {
                        coach = new CoachEntity();
                        coach.setName(name);
                        coachService.save(coach);

                    }
                    saveRelationsBetweenCoachAndHeatLine(heatLine, coach);
                }
            });

            if (!heat.getHeatLines().isEmpty()) {
                heats.add(heat);
            } else {
                heatService.delete(heat);
            }
        });
        return heats;
    }

    private void saveRelationBetweenHeatLineAndParticipant(HeatLineEntity heatLine, ParticipantEntity participant) {
        heatLine.setParticipant(participant);
        participant.addHeatLine(heatLine);

        heatLineService.save(heatLine);
        participantService.save(participant);
    }

    private void saveRelationsBetweenCoachAndHeatLine(HeatLineEntity heatLine, CoachEntity coach) {
        heatLine.addCoach(coach);
        coach.addHeatLine(heatLine);

        heatLineService.save(heatLine);
        coachService.save(coach);
    }
}