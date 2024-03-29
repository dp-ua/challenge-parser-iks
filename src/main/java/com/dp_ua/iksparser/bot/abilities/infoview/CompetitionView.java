package com.dp_ua.iksparser.bot.abilities.infoview;

import com.dp_ua.iksparser.bot.Icon;
import com.dp_ua.iksparser.bot.command.impl.competition.CommandCompetitions;
import com.dp_ua.iksparser.dba.element.CompetitionEntity;
import com.dp_ua.iksparser.dba.element.CompetitionStatus;
import com.dp_ua.iksparser.dba.element.HeatLineEntity;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.dp_ua.iksparser.bot.Icon.*;
import static com.dp_ua.iksparser.service.MessageCreator.*;

@Component
public class CompetitionView {
    public String info(CompetitionEntity competition) {
        String sb = name(competition) +
                END_LINE +
                date(competition) +
                END_LINE +
                area(competition) +
                link(competition) +
                END_LINE;

        return sb;
    }

    public String notFilledInfo() {
        String sb = WARNING +
                " Детальна інформація про змагання відсутня " +
                WARNING +
                END_LINE;
        return sb;
    }

    public String name(CompetitionEntity competition) {
        String sb = ITALIC +
                competition.getName() +
                ITALIC;
        return sb;
    }

    public String date(CompetitionEntity competition) {
        StringBuilder sb = new StringBuilder();
        Icon iconForStatus = icon(competition);
        sb
                .append(CALENDAR)
                .append(ITALIC)
                .append(" Дата: ")
                .append(ITALIC)
                .append(competition.getBeginDate())
                .append(" - ")
                .append(competition.getEndDate())
                .append(" ")
                .append(iconForStatus);
        return sb.toString();
    }

    public String nameAndDate(CompetitionEntity competition) {
        String sb = name(competition) +
                END_LINE +
                date(competition);
        return sb;
    }

    public String area(CompetitionEntity competition) {
        String sb = AREA +
                ITALIC +
                " Місце проведення: " +
                ITALIC +
                competition.getCountry() +
                ", " +
                BOLD +
                competition.getCity() +
                BOLD;
        return sb;
    }

    public String link(CompetitionEntity competition) {
        StringBuilder sb = new StringBuilder();
        if (competition.getUrl().isEmpty()) return sb.toString();
        sb
                .append(LINK)
                .append(", посилання")
                .append(Icon.URL)
                .append(LINK_END)
                .append(LINK_SEPARATOR)
                .append(competition.getUrl())
                .append(LINK_SEPARATOR_END);
        return sb.toString();
    }

    private Icon icon(CompetitionEntity competition) {
        CompetitionStatus status = CompetitionStatus.getByName(competition.getStatus());
        if (status == null) return null;
        return switch (status) {
            case C_CANCELED -> GRAY_CIRCLE;
            case C_PLANED -> BLUE_CIRCLE;
            case C_NOT_STARTED -> GREEN_CIRCLE;
            case C_IN_PROGRESS -> RED_CIRCLE;
            case C_FINISHED -> LIGHT_GRAY_CIRCLE;
        };
    }

    public String details(CompetitionEntity competition) {
        StringBuilder sb = new StringBuilder();
        sb
                .append(DURATION)
                .append(" Тривалість днів: ")
                .append(BOLD)
                .append(competition.getDays().size())
                .append(BOLD)
                .append(END_LINE);

        int participantsCount = competition.getDays().stream()
                .flatMap(day -> day.getEvents().stream())
                .flatMap(event -> event.getHeats().stream())
                .flatMap(heap -> heap.getHeatLines().stream())
                .map(HeatLineEntity::getParticipant)
                .collect(Collectors.toSet()).size();
        sb
                .append(TEAM)
                .append(" Кількість учасників: ")
                .append(BOLD)
                .append(participantsCount)
                .append(BOLD)
                .append(END_LINE);

        int eventCount = competition.getDays().stream()
                .flatMap(day -> day.getEvents().stream())
                .collect(Collectors.toSet()).size();
        sb
                .append(MEDAL)
                .append(" Кількість змагань: ")
                .append(BOLD)
                .append(eventCount)
                .append(BOLD)
                .append(END_LINE);

        int heatCount = competition.getDays().stream()
                .flatMap(day -> day.getEvents().stream())
                .flatMap(event -> event.getHeats().stream())
                .collect(Collectors.toSet()).size();
        sb
                .append(START)
                .append(" Кількість стартів: ")
                .append(BOLD)
                .append(heatCount)
                .append(BOLD)
                .append(END_LINE);
        return sb.toString();
    }

    public InlineKeyboardMarkup getCompetitionsKeyboard() {
        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        keyboard.setKeyboard(getCompetitionsButtons());
        return keyboard;
    }

    private List<List<InlineKeyboardButton>> getCompetitionsButtons() {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();

        InlineKeyboardButton button = SERVICE.getKeyboardButton(
                COMPETITION + " Переглянути змагання",
                "/" + CommandCompetitions.command);
        row.add(button);
        rows.add(row);
        return rows;
    }

}
