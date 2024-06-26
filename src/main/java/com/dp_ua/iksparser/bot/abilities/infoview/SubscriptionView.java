package com.dp_ua.iksparser.bot.abilities.infoview;

import com.dp_ua.iksparser.bot.command.impl.subscribe.CommandSubscribe;
import com.dp_ua.iksparser.bot.command.impl.subscribe.CommandSubscriptionsList;
import com.dp_ua.iksparser.bot.command.impl.subscribe.CommandUnsubscribe;
import com.dp_ua.iksparser.dba.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static com.dp_ua.iksparser.bot.Icon.*;
import static com.dp_ua.iksparser.service.MessageCreator.*;

@Component
public class SubscriptionView {
    public static final String STATUS_NEW_ENROLL = "Нова заявка в івентах";
    public static final String STATUS_HAS_RESULT = "Є результати змагання";
    @Autowired
    CompetitionView competitionView;
    @Autowired
    HeatLineView heatLineView;
    @Autowired
    ParticipantView participantView;

    public String info(ParticipantEntity participant, List<HeatLineEntity> heatLines, CompetitionEntity competition) {
        StringBuilder sb = new StringBuilder()
                .append(SUBSCRIBE)
                .append("Ви підписані на учасника: ")
                .append(END_LINE)
                .append(END_LINE)
                .append(participantView.info(participant))
                .append(END_LINE).append("Приймає участь у змаганнях: ")
                .append(competitionView.info(competition))
                .append(END_LINE);

        separateHeatLinesByStatuses(heatLines).forEach((status, lines) -> {
            if (!lines.isEmpty()) {
                sb.append(status).append(":").append(END_LINE);
                lines.forEach(heatLine -> sb.append(heatLineView.info(heatLine)));
                sb.append(END_LINE);
            }
        });

        return sb.toString();
    }

    private Map<String, List<HeatLineEntity>> separateHeatLinesByStatuses(List<HeatLineEntity> heatLines) {
        Map<String, List<HeatLineEntity>> result = getPreparedMap();
        for (HeatLineEntity heatLine : heatLines) {
            EventEntity event = heatLine.getHeat().getEvent();
            if (event.hasResultUrl()) {
                result.get(STATUS_HAS_RESULT).add(heatLine);
            } else {
                result.get(STATUS_NEW_ENROLL).add(heatLine);
            }
        }
        return result;
    }

    private static Map<String, List<HeatLineEntity>> getPreparedMap() {
        Map<String, List<HeatLineEntity>> result = new TreeMap<>();
        result.put(STATUS_NEW_ENROLL, new ArrayList<>());
        result.put(STATUS_HAS_RESULT, new ArrayList<>());
        return result;
    }


    public String subscriptionText(ParticipantEntity participant, boolean subscribed) {
        String text = participantView.info(participant);
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

    public InlineKeyboardMarkup getSubscriptionsKeyboard() {
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
