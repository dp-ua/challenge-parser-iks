package com.dp_ua.iksparser.bot.abilities.infoview;

import com.dp_ua.iksparser.bot.Icon;
import com.dp_ua.iksparser.bot.command.impl.participants.CommandParticipantDetails;
import com.dp_ua.iksparser.bot.command.impl.participants.CommandParticipants;
import com.dp_ua.iksparser.bot.command.impl.participants.CommandShowFindAllParticipants;
import com.dp_ua.iksparser.dba.entity.ParticipantEntity;
import org.apache.logging.log4j.util.Strings;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.dp_ua.iksparser.bot.Icon.*;
import static com.dp_ua.iksparser.service.MessageCreator.*;

@Component
public class ParticipantView {
    public static final int ROW_LIMIT_BUTTON = 5; //todo move to properties

    public String info(ParticipantEntity participant) {
        StringBuilder sb = new StringBuilder();
        sb
                .append(ATHLETE)
                .append(getSurname(participant))
                .append(" ")
                .append(getName(participant));
        if (!participant.getUrl().isEmpty()) {
            sb
                    .insert(0, LINK);
            sb
                    .append(" ")
                    .append(LINK_END)
                    .append(LINK_SEPARATOR)
                    .append(participant.getUrl())
                    .append(LINK_SEPARATOR_END);
        }
        sb
                .append(" ")
                .append(participant.getBorn())
                .append(BIRTHDAY);
        sb
                .append(END_LINE)
                .append(SPACE)
                .append(SPACE)
                .append(SPACE)
                .append(AREA)
                .append(getRegion(participant))
                .append(", ")
                .append(getTeam(participant));
        return sb.toString();
    }

    public String subscribedNoticeLine() {
        return STAR +
                SPACE +
                "Ви підписані на цього спортсмена" +
                END_LINE;
    }

    public Optional<InlineKeyboardButton> getParticipantProfileButtonLink(ParticipantEntity participant) {
        String url = participant.getUrl();
        if (url != null && !url.isEmpty()) {
            return Optional.of(SERVICE.getKeyboardButton(
                    ATHLETE + " Профіль спортсмена",
                    participant.getUrl()
            ));
        }
        return Optional.empty();
    }

    public InlineKeyboardMarkup getShowParticipantsKeyboard() {
        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        InlineKeyboardButton button = SERVICE.getKeyboardButton(
                ATHLETE + " Переглянути спортсменів",
                CommandParticipants.getCallbackCommand()
        );
        row.add(button);
        rows.add(row);
        keyboard.setKeyboard(rows);
        return keyboard;
    }

    public String participantsInfo(long participantsCount) {
        return Icon.ATHLETE +
                "Всього атлетів в базі: " +
                BOLD +
                participantsCount +
                BOLD +
                END_LINE;
    }

    public List<InlineKeyboardButton> getParticipantsFindKeyboard() {
        List<InlineKeyboardButton> row = new ArrayList<>();
        InlineKeyboardButton button = SERVICE.getKeyboardButton(
                FIND + " Шукати спортсменів в базі",
                CommandShowFindAllParticipants.getCallbackCommand(0, Strings.EMPTY)
        );
        row.add(button);
        return row;
    }

    public String getFindInfoText() {
        StringBuilder sb = new StringBuilder();
        sb
                .append(FIND)
                .append(SPACE)
                .append("Натиснувши кнопку пошуку, ви зможете знайти спортсмена в базі за прізвищем та ім'ям")
                .append(END_LINE)
                .append(END_LINE)
                .append(ITALIC)
                .append(INFO)
                .append(SPACE)
                .append(" Або введіть частину прізвища або імені в поле чату та відправте повідомлення")
                .append(ITALIC);
        return sb.toString();
    }

    public String getParticipantsInfoList(Page<ParticipantEntity> participants) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < participants.getContent().size(); i++) {
            ParticipantEntity participant = participants.getContent().get(i);
            sb
                    .append(getIconicNumber(i + 1))
                    .append(info(participant))
                    .append(END_LINE);
        }
        return sb.toString();
    }

    public InlineKeyboardMarkup getParticipantsKeyboard(Page<ParticipantEntity> participants) {
        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        keyboard.setKeyboard(rows);

        List<ParticipantEntity> content = participants.getContent();
        if (content.isEmpty()) {
            return keyboard;
        }
        for (int i = 0; i < content.size(); i++) {
            if (i % ROW_LIMIT_BUTTON == 0) {
                rows.add(new ArrayList<>());
            }
            List<InlineKeyboardButton> row = rows.get(rows.size() - 1);
            ParticipantEntity participant = content.get(i);
            InlineKeyboardButton button = SERVICE.getKeyboardButton(
                    getIconicNumber(i + 1) + " " + getSurname(participant) + " " + getName(participant),
                    CommandParticipantDetails.getCallbackCommand(0, participant.getId())
            );
            row.add(button);
        }

        return keyboard;
    }

    private String getSurname(ParticipantEntity participant) {
        return SERVICE.cleanMarkdown(participant.getSurname());
    }

    private String getName(ParticipantEntity participant) {
        return SERVICE.cleanMarkdown(participant.getName());
    }

    private String getTeam(ParticipantEntity participant) {
        return SERVICE.cleanMarkdown(participant.getTeam());
    }

    private String getRegion(ParticipantEntity participant) {
        return SERVICE.cleanMarkdown(participant.getRegion());
    }
}
