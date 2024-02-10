package com.dp_ua.iksparser.bot.abilities.infoview;

import com.dp_ua.iksparser.bot.command.impl.subscribe.CommandSubscribe;
import com.dp_ua.iksparser.bot.command.impl.subscribe.CommandUnsubscribe;
import com.dp_ua.iksparser.dba.element.CompetitionEntity;
import com.dp_ua.iksparser.dba.element.HeatLineEntity;
import com.dp_ua.iksparser.dba.element.ParticipantEntity;
import com.dp_ua.iksparser.dba.element.SubscriberEntity;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

import static com.dp_ua.iksparser.bot.Icon.*;
import static com.dp_ua.iksparser.service.MessageCreator.*;

public class SubscriptionView {
    public static String info(ParticipantEntity participant, List<HeatLineEntity> heatLines, CompetitionEntity competition) {
        StringBuilder sb = new StringBuilder()
                .append(SUBSCRIBE)
                .append("Ви підписані на учасника: ")
                .append(END_LINE)
                .append(END_LINE)
                .append(ParticipantView.info(participant))
                .append(END_LINE).append("Приймає участь у змаганнях: ")
                .append(CompetitionView.nameAndDate(competition))
                .append(END_LINE)
                .append(END_LINE)
                .append("Нова заявка в івентах:")
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
        InlineKeyboardButton button;
        if (subscribed) {
            button = SERVICE.getKeyboardButton(
                    UNSUBSCRIBE + " Відписатись",
                    "/" + CommandUnsubscribe.command + " " + participant.getId()
            );
        } else {
            button = SERVICE.getKeyboardButton(
                    SUBSCRIBE + " Підписатись ",
                    "/" + CommandSubscribe.command + " " + participant.getId()
            );
        }
        row.add(button);
        rows.add(row);

        keyboard.setKeyboard(rows);
        return keyboard;
    }

    public static String subscriptions(List<SubscriberEntity> subscriptions) {
        String sb = whatIsSubscriptions() +
                END_LINE +
                subscriptionsInfo(subscriptions);
        return sb;
    }

    public static String whatIsSubscriptions() {
        String sb = SUBSCRIBE +
                BOLD +
                " Підписка" +
                BOLD +
                END_LINE +
                END_LINE +
                "Дозволяє отримувати" +
                SPACE +
                MESSAGE +
                ITALIC +
                SPACE +
                "сповіщення" +
                ITALIC +
                END_LINE +
                "про нові заявки в івентах, в яких бере участь обраний спортсмен." +
                END_LINE;
        return sb;
    }

    public static String subscriptionsInfo(List<SubscriberEntity> subscriptions) {
        int size = subscriptions.size();
        String sb = SUBSCRIBE +
                " Ви підписані на " +
                BOLD +
                size +
                BOLD +
                SPACE +
                ITALIC +
                (size == 1 ? "атлета" : "атлетів") +
                ITALIC +
                ATHLETE +
                END_LINE;
        return sb;
    }
}
