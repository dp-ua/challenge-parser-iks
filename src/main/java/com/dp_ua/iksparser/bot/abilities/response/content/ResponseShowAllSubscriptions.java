package com.dp_ua.iksparser.bot.abilities.response.content;

import com.dp_ua.iksparser.bot.Icon;
import com.dp_ua.iksparser.bot.abilities.infoview.ParticipantView;
import com.dp_ua.iksparser.bot.abilities.response.ResponseContentGenerator;
import com.dp_ua.iksparser.bot.abilities.response.ResponseTypeMarker;
import com.dp_ua.iksparser.bot.command.impl.subscribe.CommandSubscriptionsList;
import com.dp_ua.iksparser.dba.entity.ParticipantEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

import static com.dp_ua.iksparser.bot.abilities.response.ResponseType.SUBSCRIPTIONS_LIST;
import static com.dp_ua.iksparser.service.MessageCreator.*;

@Component
@Scope("prototype")
@ResponseTypeMarker(SUBSCRIPTIONS_LIST)
public class ResponseShowAllSubscriptions implements ResponseContentGenerator {
    @Autowired
    ParticipantView participantView;

    @Override
    public String messageText(Object... args) {
        Page<ParticipantEntity> participants = (Page<ParticipantEntity>) getArgumentObject(0, args).orElseThrow();

        StringBuilder sb = new StringBuilder();

        String participantsInfo = participantView.getParticipantsInfoList(participants);

        sb
                .append(Icon.STAR)
                .append(SPACE)
                .append("Ви підписані на: ")
                .append(END_LINE)
                .append(END_LINE)
                .append(participantsInfo)
                .append(END_LINE);

        sb
                .append(getPageInfoNavigation(participants));

        return sb.toString();
    }

    @Override
    public InlineKeyboardMarkup keyboard(Object... args) {
        Page<ParticipantEntity> participants = (Page<ParticipantEntity>) getArgumentObject(0, args).orElseThrow();

        InlineKeyboardMarkup keyboard = participantView.getParticipantsKeyboard(participants);
        List<InlineKeyboardButton> navigation = getNavigationButtons(participants);
        keyboard.getKeyboard().add(0, navigation);

        return keyboard;
    }

    private List<InlineKeyboardButton> getNavigationButtons(Page<ParticipantEntity> participants) {
        List<InlineKeyboardButton> navigation = new ArrayList<>();
        if (participants.hasPrevious()) {
            InlineKeyboardButton button = SERVICE.getNavPreviousPageButton(
                    CommandSubscriptionsList.getCallBackCommand(participants.getNumber() - 1)
            );
            navigation.add(button);
        }
        if (participants.hasNext()) {
            InlineKeyboardButton button = SERVICE.getNavNextPageButton(
                    CommandSubscriptionsList.getCallBackCommand(participants.getNumber() + 1)
            );
            navigation.add(button);
        }
        return navigation;
    }
}