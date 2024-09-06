package com.dp_ua.iksparser.bot.abilities.response.content;

import com.dp_ua.iksparser.bot.Icon;
import com.dp_ua.iksparser.bot.abilities.infoview.ParticipantView;
import com.dp_ua.iksparser.bot.abilities.response.ResponseContentGenerator;
import com.dp_ua.iksparser.bot.abilities.response.ResponseTypeMarker;
import com.dp_ua.iksparser.bot.command.impl.participants.CommandShowFindAllParticipants;
import com.dp_ua.iksparser.dba.entity.ParticipantEntity;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.dp_ua.iksparser.bot.abilities.response.ResponseType.SHOW_ALL_PARTICIPANTS;
import static com.dp_ua.iksparser.service.MessageCreator.*;

@Component
@Scope("prototype")
@ResponseTypeMarker(SHOW_ALL_PARTICIPANTS)
public class ResponseShowAllParticipants implements ResponseContentGenerator {
    @Autowired
    ParticipantView participantView;

    // todo add STAR to list. Information about subscription to the athlete
    @Override
    public String messageText(Object... args) {
        Page<ParticipantEntity> participants = (Page<ParticipantEntity>) getArgumentObject(0, args).get();
        validate(participants, "Participants not found");
        StringBuilder sb = new StringBuilder();

        String participantsInfo = participantView.getParticipantsInfoList(participants);
        String searchString = getArgument(1, args).orElse(Strings.EMPTY);

        sb
                .append(Icon.FIND)
                .append("Результати пошуку: ")
                .append(BOLD)
                .append(searchString)
                .append(BOLD)
                .append(END_LINE).append(END_LINE)
                .append(participantsInfo)
                .append(END_LINE);
        if (Strings.isEmpty(searchString)) {
            sb
                    .append(ITALIC)
                    .append("Для пошуку по імені або прізвищу введіть їх(або частину) у поле чату та відправте повідомлення")
                    .append(ITALIC)
                    .append(END_LINE).append(END_LINE);
        }
        sb
                .append(getPageInfoNavigation(participants));

        return sb.toString();
    }

    @Override
    public InlineKeyboardMarkup keyboard(Object... args) {
        Page<ParticipantEntity> participants = (Page<ParticipantEntity>) getArgumentObject(0, args).get();
        validate(participants, "Participants not found");

        Optional<String> searchString = getArgument(1, args);

        InlineKeyboardMarkup keyboard = participantView.getParticipantsKeyboard(participants);
        List<InlineKeyboardButton> navigation = getNavigationButtons(participants, searchString.orElse(Strings.EMPTY));
        keyboard.getKeyboard().add(0, navigation);

        return keyboard;
    }

    private List<InlineKeyboardButton> getNavigationButtons(Page<ParticipantEntity> participants, String search) {
        List<InlineKeyboardButton> navigation = new ArrayList<>();
        if (participants.hasPrevious()) {
            InlineKeyboardButton button = SERVICE.getNavPreviousPageButton(
                    CommandShowFindAllParticipants.getCallbackCommand(participants.getNumber() - 1, search)
            );
            navigation.add(button);
        }
        if (participants.hasNext()) {
            InlineKeyboardButton button = SERVICE.getNavNextPageButton(
                    CommandShowFindAllParticipants.getCallbackCommand(participants.getNumber() + 1, search)
            );
            navigation.add(button);
        }
        return navigation;
    }
}
