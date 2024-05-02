package com.dp_ua.iksparser.service.parser;

import com.dp_ua.iksparser.dba.entity.CoachEntity;
import com.dp_ua.iksparser.dba.entity.HeatEntity;
import com.dp_ua.iksparser.dba.entity.HeatLineEntity;
import com.dp_ua.iksparser.dba.entity.ParticipantEntity;
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

                HeatLineEntity heatLine = getHeatLineFromRow(cells);
                setRelationsBetweenHeatAndHeatLine(heat, heatLine);

                ParticipantEntity participant = getParticipantFromRow(cells);

                saveRelationBetweenHeatLineAndParticipant(heatLine, participant);

                List<String> coaches = getCoachesFromRow(cells);
                coaches.forEach(coachName -> {
                    CoachEntity coach = coachService.findByName(coachName);
                    if (coach == null) {
                        coach = createNewCoach(coachName);
                    }
                    saveRelationsBetweenCoachAndHeatLine(heatLine, coach);
                });
            });

            if (!heat.getHeatLines().isEmpty()) {
                heats.add(heat);
            } else {
                heatService.delete(heat);
            }
        });
        return heats;
    }

    private CoachEntity createNewCoach(String coachName) {
        CoachEntity coach;
        coach = new CoachEntity();
        coach.setName(coachName);
        coachService.save(coach);
        return coach;
    }

    private static List<String> getCoachesFromRow(Elements cells) {
        return List.of(cells.get(10).text().split(","))
                .stream()
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    private ParticipantEntity getParticipantFromRow(Elements cells) {
        String surname = cells.get(5).childNode(0).childNode(0).toString();
        String name = cells.get(6).childNode(0).childNode(0).toString();
        String team = cells.get(9).text();
        String region = cells.get(8).text();
        String born = cells.get(7).text();
        String url = cells.get(4).select("a").attr("href");

        ParticipantEntity participant = participantService.findParticipant(surname, name, born);
        if (participant == null) {
            participant = createNewParticipant(url, surname, name, born, region, team);
        }
        updateParticipantUrl(participant, url);
        return participant;
    }

    private void setRelationsBetweenHeatAndHeatLine(HeatEntity heat, HeatLineEntity heatLine) {
        heat.addHeatLine(heatLine);
        heatLine.setHeat(heat);
    }

    private HeatLineEntity getHeatLineFromRow(Elements cells) {
        HeatLineEntity heatLine = new HeatLineEntity();
        heatLine.setLane(cells.get(0).text());
        heatLine.setBib(cells.get(1).text());
        heatLineService.save(heatLine);
        return heatLine;
    }

    private ParticipantEntity createNewParticipant(String url, String surname, String name, String born, String region, String team) {
        ParticipantEntity participant;
        participant = new ParticipantEntity();
        participant.setUrl(url);
        participant.setSurname(surname);
        participant.setName(name);
        participant.setBorn(born);
        participant.setRegion(region);
        participant.setTeam(team);
        participantService.save(participant);
        return participant;
    }

    private void updateParticipantUrl(ParticipantEntity participant, String url) {
        if ((participant.getUrl() == null || participant.getUrl().isEmpty())
                && url != null && !url.isEmpty()) {
            log.debug("Participant {} has no url. Set url: {}", participant, url);
            participant.setUrl(url);
            participantService.save(participant);
        }
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