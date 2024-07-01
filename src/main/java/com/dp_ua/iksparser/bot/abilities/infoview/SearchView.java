package com.dp_ua.iksparser.bot.abilities.infoview;

import com.dp_ua.iksparser.dba.entity.CoachEntity;
import com.dp_ua.iksparser.dba.entity.CompetitionEntity;
import com.dp_ua.iksparser.dba.entity.HeatLineEntity;
import com.dp_ua.iksparser.dba.entity.ParticipantEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.dp_ua.iksparser.bot.Icon.*;
import static com.dp_ua.iksparser.service.MessageCreator.*;

@Component
public class SearchView {
    @Autowired
    CompetitionView competitionView;
    @Autowired
    ParticipantView participantView;
    @Autowired
    HeatLineView heatLineView;


    public String findByBibNumber(CompetitionEntity competition) {
        return "Напишіть в чат нагрудний номер спортсмена, якого хочете знайти" +
                LOOK +
                END_LINE +
                END_LINE +
                inCompetition(competition);
    }

    public String findByName(CompetitionEntity competition) {
        return "Напишіть в чат прізвище спортсмена(або частину), якого шукаєте" +
                LOOK +
                END_LINE +
                END_LINE +
                inCompetition(competition);
    }

    public String findByCoach(CompetitionEntity competition) {
        return LOOK +
                "Напишіть в чат прізвище тренера(або частину), якого шукаєте" +
                END_LINE +
                END_LINE +
                inCompetition(competition);
    }

    private String inCompetition(CompetitionEntity competition) {
        return FIND +
                "Пошук участі спортсмена буде проводитись в івенті: " +
                END_LINE +
                competitionView.info(competition);
    }

    public String foundParticipantHeader(CoachEntity coach, List<HeatLineEntity> coachHeatLines, CompetitionEntity competition) {
        StringBuilder header = new StringBuilder();
        header
                .append(LOOK)
                .append(BOLD)
                .append("Знайдено тренера: ")
                .append(BOLD)
                .append(END_LINE)
                .append(COACH)
                .append(BOLD)
                .append(coach.getName())
                .append(BOLD)
                .append(END_LINE)
                .append(END_LINE);

        header
                .append(BOLD)
                .append("В змаганні: ")
                .append(BOLD)
                .append(competitionView.nameAndDate(competition))
                .append(END_LINE)
                .append(END_LINE);

        if (!coachHeatLines.isEmpty()) {
            header
                    .append(ITALIC)
                    .append("Заявлені такі спортсмени: ")
                    .append(ITALIC)
                    .append(END_LINE)
                    .append(END_LINE);
        } else {
            header
                    .append(ITALIC)
                    .append("Тренер не заявив спортсменів")
                    .append(ITALIC)
                    .append(END_LINE)
                    .append(END_LINE);
        }
        return header.toString();
    }

    public String foundParticipantInCompetitionMessage(
            ParticipantEntity participant,
            CompetitionEntity competition,
            List<HeatLineEntity> heatLines) {

        StringBuilder message = new StringBuilder();

        message.append(LOOK)
                .append(BOLD)
                .append("Знайдено спортсмена: ")
                .append(BOLD)
                .append(END_LINE)
                .append(participantView.info(participant))
                .append(END_LINE)
                .append(END_LINE)
                .append(competitionView.nameAndDate(competition))
                .append(END_LINE)
                .append(END_LINE)
                .append(HEAT)
                .append("Приймає участь у змаганнях: ")
                .append(END_LINE);

        heatLineView.heatLinesListInfo(participant, heatLines)
                .forEach(message::append);

        return message.toString();
    }
}
