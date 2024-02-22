package com.dp_ua.iksparser.bot.abilities.infoview;

import org.springframework.stereotype.Component;

import static com.dp_ua.iksparser.service.MessageCreator.END_LINE;
import static com.dp_ua.iksparser.service.MessageCreator.ITALIC;

@Component
public class MenuView {
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
}
