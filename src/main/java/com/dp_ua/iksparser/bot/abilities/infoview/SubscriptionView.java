package com.dp_ua.iksparser.bot.abilities.infoview;

import com.dp_ua.iksparser.bot.command.impl.subscribe.CommandSubscribe;
import com.dp_ua.iksparser.bot.command.impl.subscribe.CommandSubscriptionsList;
import com.dp_ua.iksparser.bot.command.impl.subscribe.CommandUnsubscribe;
import com.dp_ua.iksparser.dba.element.CompetitionEntity;
import com.dp_ua.iksparser.dba.element.HeatLineEntity;
import com.dp_ua.iksparser.dba.element.ParticipantEntity;
import com.dp_ua.iksparser.dba.element.SubscriberEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

import static com.dp_ua.iksparser.bot.Icon.*;
import static com.dp_ua.iksparser.service.MessageCreator.*;

@Component
public class SubscriptionView {
    @Autowired
    CompetitionView competitionView;
    @Autowired
    HeatLineView heatLineView;

    public String info(ParticipantEntity participant, List<HeatLineEntity> heatLines, CompetitionEntity competition) {
        StringBuilder sb = new StringBuilder()
                .append(SUBSCRIBE)
                .append("Ви підписані на учасника: ")
                .append(END_LINE)
                .append(END_LINE)
                .append(ParticipantView.info(participant))
                .append(END_LINE).append("Приймає участь у змаганнях: ")
                .append(competitionView.nameAndDate(competition))
                .append(END_LINE)
                .append(END_LINE)
                .append("Нова заявка в івентах:")
                .append(END_LINE);
        for (HeatLineEntity heatLine : heatLines) {
            sb
                    .append(heatLineView.info(heatLine))
                    .append(END_LINE);
        }
        return sb.toString();
    }

    public String subscriptionText(ParticipantEntity participant, boolean subscribed) {
        String text = ParticipantView.info(participant);
        if (subscribed) {
            text = SUBSCRIBE + " Ви підписані на спортсмена: " + END_LINE + text;
        } else {
            text = UNSUBSCRIBE + " Ви відписані від спортсмена: " + END_LINE + text;
        }
        return text;
    }

    public InlineKeyboardMarkup button(ParticipantEntity participant, boolean subscribed) {
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

    public String subscriptions(List<SubscriberEntity> subscriptions) {
        String sb = whatIsSubscriptions() +
                END_LINE +
                subscriptionsDetails(subscriptions);
        return sb;
    }

    public String whatIsSubscriptions() {
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

    public String subscriptionsDetails(List<SubscriberEntity> subscriptions) {
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

    public InlineKeyboardMarkup getSubscriptionsKeyboard(List<SubscriberEntity> subscribers) {
        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        InlineKeyboardButton button = SERVICE.getKeyboardButton(
                SUBSCRIBE + " Переглянути підписки",
                "/" + CommandSubscriptionsList.command
        );
        row.add(button);
        rows.add(row);
        keyboard.setKeyboard(rows);
        return keyboard;
    }
}
