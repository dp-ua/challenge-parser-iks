package com.dp_ua.iksparser.bot.abilities.infoview;

import com.dp_ua.iksparser.bot.Icon;
import com.dp_ua.iksparser.bot.command.impl.CommandParticipants;
import com.dp_ua.iksparser.dba.element.ParticipantEntity;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

import static com.dp_ua.iksparser.bot.Icon.*;
import static com.dp_ua.iksparser.service.MessageCreator.*;

@Component
public class ParticipantView {
    public String info(ParticipantEntity participant) {
        StringBuilder sb = new StringBuilder();
        sb
                .append(ATHLETE)
                .append(participant.getSurname())
                .append(" ")
                .append(participant.getName());
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
                .append(AREA)
                .append(participant.getRegion())
                .append(", ")
                .append(participant.getTeam());
        return sb.toString();
    }

    public InlineKeyboardMarkup getParticipantsKeyboard() {
        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        InlineKeyboardButton button = SERVICE.getKeyboardButton(
                ATHLETE + " Переглянути спортсменів",
                "/" + CommandParticipants.command
        );
        row.add(button);
        rows.add(row);
        keyboard.setKeyboard(rows);
        return keyboard;
    }

    public String participantsInfo(Iterable<ParticipantEntity> participants) {
        return Icon.ATHLETE +
                "Всього атлетів в базі: " +
                BOLD +
                participants.spliterator().getExactSizeIfKnown() +
                BOLD +
                END_LINE;
    }
}
