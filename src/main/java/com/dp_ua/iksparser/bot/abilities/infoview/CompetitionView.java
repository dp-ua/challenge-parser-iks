package com.dp_ua.iksparser.bot.abilities.infoview;

import com.dp_ua.iksparser.bot.Icon;
import com.dp_ua.iksparser.bot.command.impl.competition.CommandCompetition;
import com.dp_ua.iksparser.bot.command.impl.competition.CommandCompetitions;
import com.dp_ua.iksparser.dba.entity.CompetitionEntity;
import com.dp_ua.iksparser.dba.entity.CompetitionStatus;
import com.dp_ua.iksparser.dba.entity.HeatLineEntity;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.dp_ua.iksparser.bot.Icon.*;
import static com.dp_ua.iksparser.service.MessageCreator.*;

@Component
public class CompetitionView {

    public String getTextForCompetitionsPage(Page<CompetitionEntity> page) {
        List<CompetitionEntity> competitions = page.getContent();
        StringBuilder result = new StringBuilder();
        result
                .append(CHAMPIONSHIP)
                .append(BOLD)
                .append("Список змагань:")
                .append(BOLD)
                .append(END_LINE)
                .append(END_LINE);

        result.append(
                IntStream.range(0, competitions.size())
                        .mapToObj(i -> {
                            int count = i + 1;
                            StringBuilder sb = new StringBuilder();
                            sb.append(BOLD)
                                    .append(Icon.getIconForNumber(count))
                                    .append(" ")
                                    .append(BOLD); // number
                            CompetitionEntity competition = competitions.get(i);
                            sb
                                    .append(info(competition))
                                    .append(END_LINE);

                            return sb.toString();
                        })
                        .reduce((s1, s2) -> s1 + END_LINE + s2)
                        .orElse("Список пустий")
        );

        result.append(END_LINE)
                .append(END_LINE);
        result
                .append(PAGE_WITH_CURL)
                .append(" Сторінка ")
                .append("(")
                .append(BOLD)
                .append(page.getNumber() + 1)
                .append(BOLD)
                .append(")")
                .append(" з ")
                .append(BOLD)
                .append(page.getTotalPages())
                .append(BOLD);

        result
                .append("     ")
                .append(ITALIC)
                .append("[оберіть змагання за його номером]")
                .append(ITALIC);

        return result.toString();
    }

    public String listWithNumbers(List<CompetitionEntity> competitions) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < competitions.size(); i++) {
            CompetitionEntity competition = competitions.get(i);
            sb
                    .append(Icon.getIconicNumber(i + 1)).append(SPACE)
                    .append(info(competition));
            if (i < competitions.size() - 1) {
                sb.append(END_LINE);
            }
        }
        return sb.toString();
    }

    public InlineKeyboardMarkup getKeyboardForCompetitionsPage(Page<CompetitionEntity> page) {
        List<CompetitionEntity> competitions = page.getContent();
        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        rows.add(getRowWithPages(page));

        List<InlineKeyboardButton> row = new ArrayList<>();
        IntStream.range(0, competitions.size()).forEach(i -> {
            CompetitionEntity competition = competitions.get(i);
            int count = i + 1;
            InlineKeyboardButton button = SERVICE.getKeyboardButton(
                    Icon.getIconForNumber(count).toString(),
                    "/" + CommandCompetition.command + " " + competition.getId()
            );
            row.add(button);
        });
        rows.add(row);

        keyboard.setKeyboard(rows);
        return keyboard;
    }

    private List<InlineKeyboardButton> getRowWithPages(Page<CompetitionEntity> page) {
        List<InlineKeyboardButton> row = new ArrayList<>();

        int number = page.getNumber();
        int totalPages = page.getTotalPages();
        if (number > 0) {
            InlineKeyboardButton leftPage = SERVICE.getKeyboardButton(
                    PREVIOUS + " Свіжіші",
                    "/" + CommandCompetitions.command + " " + (number - 1)
            );
            row.add(leftPage);
        }
        if (number < totalPages - 1) {
            InlineKeyboardButton rightPage = SERVICE.getKeyboardButton(
                    "Старіші " + NEXT,
                    "/" + CommandCompetitions.command + " " + (number + 1)
            );
            row.add(rightPage);
        }
        return row;
    }

    public String info(CompetitionEntity competition) {
        return name(competition) +
                END_LINE +
                date(competition) +
                END_LINE +
                area(competition) +
                SPACE +
                link(competition) +
                END_LINE;
    }

    public String notFilledInfo() {
        return WARNING +
                END_LINE +
                " Детальна інформація про змагання відсутня " +
                WARNING +
                END_LINE;
    }

    public String name(CompetitionEntity competition) {
        return ITALIC +
                competition.getName() +
                ITALIC;
    }

    public String date(CompetitionEntity competition) {
        StringBuilder sb = new StringBuilder();
        Icon iconForStatus = icon(competition);
        sb
                .append(SPACE)
                .append(SPACE)
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
        return name(competition) +
                END_LINE +
                date(competition);
    }

    public String area(CompetitionEntity competition) {
        return SPACE +
                SPACE +
                AREA +
                ITALIC +
                " Місце проведення: " +
                ITALIC +
                competition.getCountry() +
                ", " +
                BOLD +
                competition.getCity() +
                BOLD;
    }

    public String link(CompetitionEntity competition) {
        StringBuilder sb = new StringBuilder();
        if (competition.getUrl().isEmpty()) return sb.toString();
        sb
                .append(SPACE)
                .append(LINK)
                .append("посилання")
                .append(Icon.URL)
                .append(LINK_END)
                .append(LINK_SEPARATOR)
                .append(competition.getUrl())
                .append(LINK_SEPARATOR_END);
        return sb.toString();
    }

    public Icon icon(CompetitionEntity competition) {
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

    public String getCompetitionsInfo(List<CompetitionEntity> competitions, List<String> years) {
        long filledCompetitionsCount = competitions.stream().filter(CompetitionEntity::isFilled).count();
        int competitionsCount = competitions.size();
        String yearStart = years.get(0);
        String yearFinish = years.get(years.size() - 1);

        String sb = COMPETITION +
                "Всього змагань в базі: " +
                BOLD +
                competitionsCount +
                BOLD +
                END_LINE +
                CALENDAR +
                "Дата : з " +
                BOLD +
                yearStart +
                BOLD +
                " по " +
                BOLD +
                yearFinish +
                BOLD +
                " роки" +
                END_LINE +
                RESULT +
                "Змагань, по яким заповнена інформація: " +
                BOLD +
                filledCompetitionsCount +
                BOLD +
                END_LINE;
        return sb;
    }
}
