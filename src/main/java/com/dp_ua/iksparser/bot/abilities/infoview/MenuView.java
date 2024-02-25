package com.dp_ua.iksparser.bot.abilities.infoview;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

import static com.dp_ua.iksparser.service.MessageCreator.END_LINE;
import static com.dp_ua.iksparser.service.MessageCreator.ITALIC;

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
        InlineKeyboardMarkup participantsKeyboard = participantView.getParticipantsKeyboard();
        InlineKeyboardMarkup subscriptionsKeyboard = subscriptionView.getSubscriptionsKeyboard();

        resultKeyboard.addAll(competitionsKeyboard.getKeyboard());
        resultKeyboard.addAll(participantsKeyboard.getKeyboard());
        resultKeyboard.addAll(subscriptionsKeyboard.getKeyboard());

        InlineKeyboardMarkup result = new InlineKeyboardMarkup();
        result.setKeyboard(resultKeyboard);
        return result;
    }
}
