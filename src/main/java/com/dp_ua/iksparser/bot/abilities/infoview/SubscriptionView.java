package com.dp_ua.iksparser.bot.abilities.infoview;

import com.dp_ua.iksparser.bot.command.impl.CommandSubscribe;
import com.dp_ua.iksparser.bot.command.impl.CommandUnsubscribe;
import com.dp_ua.iksparser.dba.element.CompetitionEntity;
import com.dp_ua.iksparser.dba.element.HeatLineEntity;
import com.dp_ua.iksparser.dba.element.ParticipantEntity;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

import static com.dp_ua.iksparser.bot.Icon.SUBSCRIBE;
import static com.dp_ua.iksparser.bot.Icon.UNSUBSCRIBE;
import static com.dp_ua.iksparser.service.MessageCreator.END_LINE;
import static com.dp_ua.iksparser.service.MessageCreator.SERVICE;

public class SubscriptionView {
    public static String info(ParticipantEntity participant, List<HeatLineEntity> heatLines, CompetitionEntity competition) {
        StringBuilder sb = new StringBuilder()
                .append(SUBSCRIBE)
                .append("Ви підписані на учасника: ")
                .append(END_LINE)
                .append(ParticipantView.info(participant))
                .append(END_LINE).append("Приймає участь у змаганнях: ")
                .append(CompetitionView.nameAndDate(competition))
                .append(END_LINE)
                .append("Нова заявка в забігах:")
                .append(END_LINE);
        for (HeatLineEntity heatLine : heatLines) {
            sb
                    .append(HeatLineView.info(heatLine))
                    .append(END_LINE);
        }
        return sb.toString();
    }

    public static InlineKeyboardMarkup button(ParticipantEntity participant, boolean subscribed) {
        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        List<InlineKeyboardButton> row = new ArrayList<>();
        if (subscribed) {
            InlineKeyboardButton button = SERVICE.getKeyboardButton(
                    UNSUBSCRIBE + " Відписатись",
                    "/" + CommandUnsubscribe.command + " " + participant.getId()
            );
            row.add(button);
        } else {
            InlineKeyboardButton button = SERVICE.getKeyboardButton(
                    SUBSCRIBE + " Підписатись ",
                    "/" + CommandSubscribe.command + " " + participant.getId()
            );
            row.add(button);
        }
        rows.add(row);

        keyboard.setKeyboard(rows);
        return keyboard;
    }
}
