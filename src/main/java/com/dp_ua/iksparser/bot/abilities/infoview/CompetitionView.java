package com.dp_ua.iksparser.bot.abilities.infoview;

import com.dp_ua.iksparser.bot.Icon;
import com.dp_ua.iksparser.bot.command.impl.competition.CommandCompetition;
import com.dp_ua.iksparser.bot.command.impl.competition.CommandCompetitions;
import com.dp_ua.iksparser.bot.command.impl.participants.CommandShowHeatLinesInCompetitionForParticipant;
import com.dp_ua.iksparser.dba.entity.CompetitionEntity;
import com.dp_ua.iksparser.dba.entity.CompetitionStatus;
import com.dp_ua.iksparser.dba.entity.HeatLineEntity;
import com.dp_ua.iksparser.dba.entity.ParticipantEntity;
import com.dp_ua.iksparser.service.YearRange;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
                    .append(Icon.getIconicNumber(i + 1))
                    .append(SPACE)
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
                    CommandCompetition.getCallbackCommand(competition.getId())
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
                    CommandCompetitions.getCallbackCommand(number - 1)
            );
            row.add(leftPage);
        }
        if (number < totalPages - 1) {
            InlineKeyboardButton rightPage = SERVICE.getKeyboardButton(
                    "Старіші " + NEXT,
                    CommandCompetitions.getCallbackCommand(number + 1)
            );
            row.add(rightPage);
        }
        return row;
    }

    public String info(CompetitionEntity competition) {
        return new StringBuilder()
                .append(name(competition))
                .append(END_LINE)
                .append(date(competition))
                .append(END_LINE)
                .append(area(competition))
                .append(SPACE)
                .append(link(competition))
                .append(END_LINE)
                .toString();
    }

    public String notFilledInfo() {
        return new StringBuilder()
                .append(WARNING)
                .append(SPACE)
                .append(END_LINE)
                .append("Детальна інформація про змагання відсутня")
                .append(SPACE)
                .append(WARNING)
                .append(END_LINE)
                .toString();
    }

    public String name(CompetitionEntity competition) {
        return ITALIC +
                competition.getName() +
                ITALIC;
    }

    public String date(CompetitionEntity competition) {
        StringBuilder sb = new StringBuilder();
        sb
                .append(SPACE)
                .append(SPACE)
                .append(CALENDAR)
                .append(ITALIC)
                .append(" Дата: ")
                .append(ITALIC)
                .append(competition.getBeginDate())
                .append(" - ")
                .append(competition.getEndDate());

        icon(competition).ifPresent(icon ->
                sb
                        .append(" ")
                        .append(icon)
        );

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

    public Optional<Icon> icon(CompetitionEntity competition) {
        CompetitionStatus status = CompetitionStatus.getByName(competition.getStatus());
        return switch (status) {
            case C_CANCELED -> Optional.of(GRAY_CIRCLE);
            case C_PLANED -> Optional.of(BLUE_CIRCLE);
            case C_NOT_STARTED -> Optional.of(GREEN_CIRCLE);
            case C_IN_PROGRESS -> Optional.of(RED_CIRCLE);
            case C_FINISHED -> Optional.of(LIGHT_GRAY_CIRCLE);
            default -> Optional.empty();
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
                CommandCompetitions.getCallbackCommand());
        row.add(button);
        rows.add(row);
        return rows;
    }

    public String getCompetitionsInfo(long allCompetitions, long filledCompetitions, YearRange years) {
        StringBuilder sb = new StringBuilder();
        sb
                .append(COMPETITION)
                .append("Всього змагань в базі: ")
                .append(BOLD)
                .append(allCompetitions)
                .append(BOLD)
                .append(END_LINE)
                .append(CALENDAR)
                .append("Дата : з ")
                .append(BOLD)
                .append(years.getMinYear())
                .append(BOLD)
                .append(" по ")
                .append(BOLD)
                .append(years.getMaxYear())
                .append(BOLD)
                .append(" роки")
                .append(END_LINE)
                .append(RESULT)
                .append("Змагань, по яким заповнена інформація: ")
                .append(BOLD)
                .append(filledCompetitions)
                .append(BOLD)
                .append(END_LINE);
        return sb.toString();
    }

    public InlineKeyboardMarkup getBackToCompetitionKeyboard(CompetitionEntity competition) {
        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        List<InlineKeyboardButton> row = SERVICE.getBackButton(
                CommandCompetition.getCallbackCommand(competition.getId())
        );
        rows.add(row);

        keyboard.setKeyboard(rows);
        return keyboard;
    }

    public InlineKeyboardButton participantInCompetitionButton(String buttonText, CompetitionEntity competition, ParticipantEntity participant) {
        return SERVICE.getKeyboardButton(
                buttonText,
                CommandShowHeatLinesInCompetitionForParticipant.getCallbackCommand(participant.getId(), competition.getId())
        );
    }
}
