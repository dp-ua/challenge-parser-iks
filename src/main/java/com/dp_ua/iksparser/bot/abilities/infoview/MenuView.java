package com.dp_ua.iksparser.bot.abilities.infoview;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

import static com.dp_ua.iksparser.bot.Icon.*;
import static com.dp_ua.iksparser.service.MessageCreator.*;

@Component
public class MenuView {
    @Autowired
    CompetitionView competitionView;
    @Autowired
    ParticipantView participantView;
    @Autowired
    SubscriptionView subscriptionView;

    public String mainMenu(String competitionsInfo, String participantsInfo, String subscribeInfo) {
        return "В боті ви зможете знайти інформацію по змаганнях, що проводяться Федерацією легкої атлетики України." +
                END_LINE +
                END_LINE +
                ITALIC +
                "Інформація про змагання:" +
                ITALIC +
                END_LINE +
                competitionsInfo +
                END_LINE +
                ITALIC +
                "Інформація про атлетів:" +
                ITALIC +
                END_LINE +
                participantsInfo +
                END_LINE +
                ITALIC +
                "Підписки:" +
                ITALIC +
                END_LINE +
                subscribeInfo +
                END_LINE +
                END_LINE +
                "Виберіть пункт меню для отримання інформації.";
    }

    public InlineKeyboardMarkup menuButtons() {
        List<List<InlineKeyboardButton>> resultKeyboard = new ArrayList<>();

        InlineKeyboardMarkup competitionsKeyboard = competitionView.getCompetitionsKeyboard();
        InlineKeyboardMarkup participantsKeyboard = participantView.getShowParticipantsKeyboard();
        InlineKeyboardMarkup subscriptionsKeyboard = subscriptionView.getSubscriptionsKeyboard();

        resultKeyboard.addAll(competitionsKeyboard.getKeyboard());
        resultKeyboard.addAll(participantsKeyboard.getKeyboard());
        resultKeyboard.addAll(subscriptionsKeyboard.getKeyboard());

        InlineKeyboardMarkup result = new InlineKeyboardMarkup();
        result.setKeyboard(resultKeyboard);
        return result;
    }

    public String startText(String visibleName, String botURL) {
        StringBuilder sb = new StringBuilder();
        sb
                .append("Вітаю, мене звуть ")
                .append(LINK)
                .append(visibleName)
                .append(SPACE)
                .append(ROBOT)
                .append(LINK_END)
                .append(LINK_SEPARATOR)
                .append(botURL)
                .append(LINK_SEPARATOR_END)
                .append(END_LINE)
                .append(END_LINE)
                .append("Я допоможу тобі знайти інформацію про")
                .append(END_LINE)
                .append(SPACE_LONG)
                .append(EVENT)
                .append(SPACE)
                .append("змагання")
                .append(END_LINE)
                .append(SPACE_LONG)
                .append(ATHLETE)
                .append(SPACE)
                .append("спортсменів")
                .append(END_LINE)
                .append(SPACE_LONG)
                .append(COACH)
                .append(SPACE)
                .append("та тренерів")

                .append(END_LINE)
                .append(END_LINE)
                .append("Для початку роботи скористайся кнопкою меню")
                .append(SPACE)
                .append(MENU);

        return sb.toString();
    }

    public InlineKeyboardMarkup startButtons() {
        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        keyboard.setKeyboard(rows);

        rows.add(SERVICE.getMainButton());

        return keyboard;
    }
}
