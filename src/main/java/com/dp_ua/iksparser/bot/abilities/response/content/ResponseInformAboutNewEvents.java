package com.dp_ua.iksparser.bot.abilities.response.content;

import com.dp_ua.iksparser.bot.Icon;
import com.dp_ua.iksparser.bot.abilities.infoview.CompetitionView;
import com.dp_ua.iksparser.bot.abilities.infoview.ParticipantView;
import com.dp_ua.iksparser.bot.abilities.infoview.SubscriptionView;
import com.dp_ua.iksparser.bot.abilities.response.ResponseContentGenerator;
import com.dp_ua.iksparser.bot.abilities.response.ResponseTypeMarker;
import com.dp_ua.iksparser.dba.entity.CompetitionEntity;
import com.dp_ua.iksparser.dba.entity.HeatLineEntity;
import com.dp_ua.iksparser.dba.entity.ParticipantEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

import static com.dp_ua.iksparser.bot.abilities.response.ResponseType.NEW_EVENT_INFORM;

@Component
@Scope("prototype")
@ResponseTypeMarker(NEW_EVENT_INFORM)
public class ResponseInformAboutNewEvents implements ResponseContentGenerator {
    private static final int ARGS_PARTICIPANT_INDEX = 0;
    private static final int ARGS_COMPETITION_INDEX = 1;
    private static final int ARGS_HEATLINES_INDEX = 2;
    private final SubscriptionView subscriptionView;
    private final ParticipantView participantView;
    private final CompetitionView competitionView;

    public ResponseInformAboutNewEvents(@Autowired SubscriptionView subscriptionView,
                                        @Autowired ParticipantView participantView,
                                        @Autowired CompetitionView competitionView) {
        this.subscriptionView = subscriptionView;
        this.participantView = participantView;
        this.competitionView = competitionView;
    }

    @Override
    public String messageText(Object... args) {
        ParticipantEntity participant = getParticipant(args);
        CompetitionEntity competition = getCompetition(args);
        List<HeatLineEntity> heatLines = getHeatLines(args);

        return subscriptionView.info(participant, heatLines, competition);
    }

    @Override
    public InlineKeyboardMarkup keyboard(Object... args) {
        ParticipantEntity participant = getParticipant(args);
        CompetitionEntity competition = getCompetition(args);

        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboardRows = new ArrayList<>();
        keyboard.setKeyboard(keyboardRows);

        participantView.getParticipantProfileButtonLink(participant)
                .ifPresent(button -> keyboardRows.add(List.of(button)));

        InlineKeyboardButton competitionInfoButton = competitionView.participantInCompetitionButton(
                Icon.COMPETITION + " Інформація по змаганню",
                competition,
                participant);
        keyboardRows.add(List.of(competitionInfoButton));

        return keyboard;
    }

    private CompetitionEntity getCompetition(Object[] args) {
        return (CompetitionEntity) getArgumentObject(ARGS_COMPETITION_INDEX, args).orElseThrow();
    }

    private List<HeatLineEntity> getHeatLines(Object[] args) {
        return (List<HeatLineEntity>) getArgumentObject(ARGS_HEATLINES_INDEX, args).orElseThrow();
    }

    private ParticipantEntity getParticipant(Object[] args) {
        return (ParticipantEntity) getArgumentObject(ARGS_PARTICIPANT_INDEX, args).orElseThrow();
    }
}
