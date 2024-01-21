package com.dp_ua.iksparser.service;

import com.dp_ua.iksparser.dba.element.CoachEntity;
import com.dp_ua.iksparser.dba.element.HeatEntity;
import com.dp_ua.iksparser.dba.element.HeatLineEntity;
import com.dp_ua.iksparser.dba.element.ParticipantEntity;
import com.dp_ua.iksparser.dba.service.CoachService;
import com.dp_ua.iksparser.dba.service.HeatLineService;
import com.dp_ua.iksparser.dba.service.HeatService;
import com.dp_ua.iksparser.dba.service.ParticipantService;
import jakarta.transaction.Transactional;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Component
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
            String heatName = "Heat " + count.getAndIncrement();
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

                String surname = cells.get(5).text().split(" ")[0];
                String name = cells.get(6).text().split(" ")[0];
                String team = cells.get(9).text();
                String region = cells.get(8).text();
                String born = cells.get(7).text();
                synchronized (participantService) {
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

                    heatLine.setParticipant(participant);
                    participant.addHeatLine(heatLine);
                }
                String[] coaches = cells.get(10).text().split(",");
                for (String coachName : coaches) {
                    synchronized (coachService) {
                        CoachEntity coach = coachService.findByName(coachName.trim());
                        if (coach == null) {
                            coach = new CoachEntity();
                            coach.setName(coachName.trim());
                            coachService.save(coach);
                        }
                        heatLine.addCoach(coach);
                        coach.addHeatLine(heatLine);
                    }
                }
            });

            if (!heat.getHeatLines().isEmpty()) {
                heats.add(heat);
            }
        });
        return heats;
    }
}


//<h2>Забіг | Heat 1</h2>
//<div class="table-responsive">
//<table class="table">
//<thead>
//<tr>
//<th>Дор.<br><small>Lane</small></th>
//<th>Bib<br><small>Номер</small></th>
//<th>SB<br><small>SB</small></th>
//<th>PB<br><small>PB</small></th>
//<th>Фото<br><small>Photo</small></th>
//<th>Прізвище<br><small>Surname</small></th>
//<th>Ім'я<br><small>Name</small></th>
//<th>Дата нар.<br><small>Born</small></th>
//<th>Країна/Область<br><small>Country/Region</small></th>
//<th>Команда<br><small>Team</small></th>
//<th>Тренери<br><small>Coaches</small></th>
//</tr>
//</thead>
//
//<tr bgcolor="#eeeeee">                <td><b>3</b></td>
//<td>310</td>
//<td></td>
//<td></td>
//
//<td><a href="https://statistics.uaf.org.ua/index.php?r=site/card-by-name&amp;lastname=ОСАДЧУК&firstname=Георгій&dob=18.05.2010"><div class="athlete-photo"><img src="/competitions1/images/athlete-photo.png" /></div></a></td>
//
//
//<td style="white-space: nowrap;"><b>ОСАДЧУК<br><small>OSADCHUK</small></b></td>
//<td><b>Георгій<br><small>Heorhii</small></b></td>
//<td>18.05.2010</td>
//<td style="white-space: nowrap;"><img style="display: inline-block" src="/competitions1/images/oblast/odes.png">&nbsp;Одеська</td>
//<td>СК &quot;Подільск&quot; Одс</td>
//<td>Білолипецьких Л., Білолипецьких М.Д.</td>
//</tr>
//<tr>                <td><b>4</b></td>
//<td>1692</td>
//<td></td>
//<td></td>
//
//<td><a href="https://statistics.uaf.org.ua/index.php?r=site/card-by-name&amp;lastname=БУРЯК&firstname=Владислав&dob=20.11.2008"><div class="athlete-photo"><img src="/competitions1/images/athlete-photo.png" /></div></a></td>
//
//
//<td style="white-space: nowrap;"><b>БУРЯК<br><small>BURIAK</small></b></td>
//<td><b>Владислав<br><small>Vladyslav</small></b></td>
//<td>20.11.2008</td>
//<td style="white-space: nowrap;"><img style="display: inline-block" src="/competitions1/images/oblast/odes.png">&nbsp;Одеська</td>
//<td>МОН, ОСДЮСШОР Одеса</td>
//<td>Гарник Ю.С., Дегтяр О.А.</td>
//</tr>
//<tr bgcolor="#eeeeee">                <td><b>5</b></td>
//<td>618</td>
//<td></td>
//<td></td>
//
//<td><a href="https://statistics.uaf.org.ua/index.php?r=site/card-by-name&amp;lastname=ВАЛОВОЙ&firstname=Ілля&dob=29.01.2009"><div class="athlete-photo"><img src="/competitions1/images/athlete-photo.png" /></div></a></td>
//
//
//<td style="white-space: nowrap;"><b>ВАЛОВОЙ<br><small>VALOVOI</small></b></td>
//<td><b>Ілля<br><small>Illia</small></b></td>
//<td>29.01.2009</td>
//<td style="white-space: nowrap;"><img style="display: inline-block" src="/competitions1/images/oblast/odes.png">&nbsp;Одеська</td>
//<td>МОН, ОСДЮСШОР Одеса</td>
//<td>Шпічак О.В.</td>
//</tr>
//<tr>                <td><b>6</b></td>
//<td>608</td>
//<td></td>
//<td></td>
//
//<td><a href="https://statistics.uaf.org.ua/index.php?r=site/card-by-name&amp;lastname=ЗАДОРОЖНІЙ&firstname=Ростислав&dob=05.06.2007"><div class="athlete-photo"><img src="/competitions1/images/athlete-photo.png" /></div></a></td>
//
//
//<td style="white-space: nowrap;"><b>ЗАДОРОЖНІЙ<br><small>ZADOROZHNII</small></b></td>
//<td><b>Ростислав<br><small>Rostyslav</small></b></td>
//<td>05.06.2007</td>
//<td style="white-space: nowrap;"><img style="display: inline-block" src="/competitions1/images/oblast/odes.png">&nbsp;Одеська</td>
//<td>МОН, ОСДЮСШОР Одеса</td>
//<td>Шпічак О.В.</td>
//</tr>
//</table>
//</div>
//