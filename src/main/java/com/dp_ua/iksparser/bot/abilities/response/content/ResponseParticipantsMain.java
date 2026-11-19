package com.dp_ua.iksparser.bot.abilities.response.content;

import static com.dp_ua.iksparser.bot.abilities.response.ResponseType.PARTICIPANTS_VIEW_MAIN;
import static com.dp_ua.iksparser.service.MessageCreator.END_LINE;
import static com.dp_ua.iksparser.service.MessageCreator.SERVICE;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import com.dp_ua.iksparser.bot.abilities.infoview.ParticipantView;
import com.dp_ua.iksparser.bot.abilities.response.ResponseContentGenerator;
import com.dp_ua.iksparser.bot.abilities.response.ResponseTypeMarker;
import com.dp_ua.iksparser.dba.service.ParticipantService;

import lombok.RequiredArgsConstructor;

@Component
@Scope("prototype")
@ResponseTypeMarker(PARTICIPANTS_VIEW_MAIN)
@RequiredArgsConstructor
public class ResponseParticipantsMain implements ResponseContentGenerator {

    private final ParticipantView participantView;
    private final ParticipantService participantService;

    @Override
    public String messageText(Object... args) {
        String sb = participantView.participantsInfo(participantService.getCount()) +
                END_LINE +
                END_LINE +
                participantView.getFindInfoText();
        return sb;
    }

    @Override
    public InlineKeyboardMarkup keyboard(Object... args) {
        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        keyboard.setKeyboard(rows);

        rows.add(participantView.getParticipantsFindKeyboard());
        rows.add(SERVICE.getMainButton());

        return keyboard;
    }

}
