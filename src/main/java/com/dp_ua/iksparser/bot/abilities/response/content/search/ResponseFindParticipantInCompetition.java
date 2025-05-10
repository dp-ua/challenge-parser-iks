package com.dp_ua.iksparser.bot.abilities.response.content.search;

import static com.dp_ua.iksparser.bot.abilities.action.ActionType.SUB;
import static com.dp_ua.iksparser.bot.abilities.action.ActionType.UNS;
import static com.dp_ua.iksparser.bot.abilities.response.ResponseType.FIND_PARTICIPANT_IN_COMPETITION;

import java.util.List;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import com.dp_ua.iksparser.bot.abilities.infoview.SearchView;
import com.dp_ua.iksparser.bot.abilities.infoview.SubscriptionView;
import com.dp_ua.iksparser.bot.abilities.response.ResponseContentGenerator;
import com.dp_ua.iksparser.bot.abilities.response.ResponseTypeMarker;
import com.dp_ua.iksparser.bot.command.impl.participants.CommandShowHeatLinesInCompetitionForParticipant;
import com.dp_ua.iksparser.dba.entity.CompetitionEntity;
import com.dp_ua.iksparser.dba.entity.HeatLineEntity;
import com.dp_ua.iksparser.dba.entity.ParticipantEntity;

import lombok.RequiredArgsConstructor;

@Component
@Scope("prototype")
@ResponseTypeMarker(FIND_PARTICIPANT_IN_COMPETITION)
@RequiredArgsConstructor
public class ResponseFindParticipantInCompetition implements ResponseContentGenerator {
    private static final int ARGS_SIZE = 4;
    private static final int ARGS_PARTICIPANT_INDEX = 0;
    private static final int ARGS_COMPETITION_INDEX = 1;
    private static final int ARGS_HEAT_LINES_INDEX = 2;
    private static final int ARGS_SUBSCRIBED_INDEX = 3;

    private final SearchView searchView;
    private final SubscriptionView subscriptionView;

    @Override
    public String messageText(Object... args) {
        validateArgs(args);

        ParticipantEntity participant = getParticipant(args);
        CompetitionEntity competition = getCompetition(args);
        List<HeatLineEntity> heatLines = getHeatLines(args);

        return searchView.foundParticipantInCompetitionMessage(participant, competition, heatLines);
    }

    @Override
    public InlineKeyboardMarkup keyboard(Object... args) {
        validateArgs(args);

        ParticipantEntity participant = getParticipant(args);
        CompetitionEntity competition = getCompetition(args);
        boolean subscribed = getSubscribed(args);

        InlineKeyboardMarkup result = new InlineKeyboardMarkup();
        InlineKeyboardButton inlineKeyboardButton = getSubscribeButton(subscribed, competition, participant);
        result.setKeyboard(List.of(List.of(inlineKeyboardButton)));

        return result;
    }

    private InlineKeyboardButton getSubscribeButton(boolean subscribed, CompetitionEntity competition, ParticipantEntity participant) {
        return subscribed
                ? subscriptionView.buttonUnsubscribe(
                CommandShowHeatLinesInCompetitionForParticipant.getCallbackCommand(
                        participant.getId(), competition.getId(), UNS))
                : subscriptionView.buttonSubscribe(
                CommandShowHeatLinesInCompetitionForParticipant.getCallbackCommand(
                        participant.getId(), competition.getId(), SUB));
    }

    private ParticipantEntity getParticipant(Object[] args) {
        return getArgumentObject(ARGS_PARTICIPANT_INDEX, args)
                .filter(ParticipantEntity.class::isInstance)
                .map(ParticipantEntity.class::cast)
                .orElseThrow(() -> new IllegalArgumentException("Participant not found or has wrong type"));
    }

    private CompetitionEntity getCompetition(Object[] args) {
        return getArgumentObject(ARGS_COMPETITION_INDEX, args)
                .filter(CompetitionEntity.class::isInstance)
                .map(CompetitionEntity.class::cast)
                .orElseThrow(() -> new IllegalArgumentException("Competition not found or has wrong type"));
    }

    private List<HeatLineEntity> getHeatLines(Object[] args) {
        return getArgumentObject(ARGS_HEAT_LINES_INDEX, args)
                .filter(List.class::isInstance)
                .map(obj -> (List<HeatLineEntity>) obj)
                .orElseThrow(() -> new IllegalArgumentException("Heat lines not found or has wrong type"));
    }

    private boolean getSubscribed(Object[] args) {
        return getArgument(ARGS_SUBSCRIBED_INDEX, args)
                .map(Boolean::parseBoolean)
                .orElseThrow(() -> new IllegalArgumentException("Subscribed flag not found"));
    }

    private void validateArgs(Object[] args) {
        if (args.length != ARGS_SIZE) {
            throw new IllegalArgumentException("Invalid args size: " + args.length + ", expected: " + ARGS_SIZE);
        }
    }

}
