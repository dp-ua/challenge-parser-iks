package com.dp_ua.iksparser.bot.abilities.response.content.search;

import com.dp_ua.iksparser.bot.abilities.action.ActionType;
import com.dp_ua.iksparser.bot.abilities.infoview.SearchView;
import com.dp_ua.iksparser.bot.abilities.infoview.SubscriptionView;
import com.dp_ua.iksparser.bot.abilities.response.ResponseContentGenerator;
import com.dp_ua.iksparser.bot.abilities.response.ResponseTypeMarker;
import com.dp_ua.iksparser.bot.command.impl.participants.CommandShowHeatLinesInCompetitionForParticipant;
import com.dp_ua.iksparser.dba.entity.CompetitionEntity;
import com.dp_ua.iksparser.dba.entity.HeatLineEntity;
import com.dp_ua.iksparser.dba.entity.ParticipantEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.List;
import java.util.Optional;

import static com.dp_ua.iksparser.bot.abilities.action.ActionType.UNSUB;
import static com.dp_ua.iksparser.bot.abilities.response.ResponseType.FIND_PARTICIPANT_IN_COMPETITION;

@Component
@Scope("prototype")
@ResponseTypeMarker(FIND_PARTICIPANT_IN_COMPETITION)
public class ResponseFindParticipantInCompetition implements ResponseContentGenerator {
    private static final int ARGS_SIZE = 4;
    private static final int ARGS_PARTICIPANT_INDEX = 0;
    private static final int ARGS_COMPETITION_INDEX = 1;
    private static final int ARGS_HEAT_LINES_INDEX = 2;
    private static final int ARGS_SUBSCRIBED_INDEX = 3;
    @Autowired
    SearchView searchView;
    @Autowired
    SubscriptionView subscriptionView;


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
        boolean subscribed = getSuscribed(args);

        InlineKeyboardMarkup result = new InlineKeyboardMarkup();

        InlineKeyboardButton inlineKeyboardButton = getSubscribeButton(subscribed, competition, participant);
        result.setKeyboard(List.of(List.of(inlineKeyboardButton)));

        return result;
    }

    private InlineKeyboardButton getSubscribeButton(boolean subscribed, CompetitionEntity competition, ParticipantEntity participant) {
        return subscribed ?
                subscriptionView.buttonUnsubscribe(
                        CommandShowHeatLinesInCompetitionForParticipant.getCallbackCommand(participant.getId(), competition.getId(), UNSUB))
                :
                subscriptionView.buttonSubscribe(
                        CommandShowHeatLinesInCompetitionForParticipant.getCallbackCommand(participant.getId(), competition.getId(), ActionType.SUB));
    }

    private ParticipantEntity getParticipant(Object[] args) {
        return (ParticipantEntity) getArgumentObject(ARGS_PARTICIPANT_INDEX, args).orElseThrow();
    }

    private CompetitionEntity getCompetition(Object[] args) {
        return (CompetitionEntity) getArgumentObject(ARGS_COMPETITION_INDEX, args).orElseThrow();
    }

    private List<HeatLineEntity> getHeatLines(Object[] args) {
        return (List<HeatLineEntity>) getArgumentObject(ARGS_HEAT_LINES_INDEX, args).orElseThrow();
    }

    private boolean getSuscribed(Object[] args) {
        return Boolean.parseBoolean(getArgument(ARGS_SUBSCRIBED_INDEX, args).orElseThrow());
    }

    private void validateArgs(Object[] args) {
        if (args.length != ARGS_SIZE) {
            throw new IllegalArgumentException("Invalid args size: " + args.length + ", expected: " + ARGS_SIZE);
        }
        Optional<?> participant = getArgumentObject(ARGS_PARTICIPANT_INDEX, args);
        if (!(participant.orElseThrow() instanceof ParticipantEntity)) {
            throw new IllegalArgumentException("Invalid argument type: " + participant.get().getClass().getName() +
                    ", expected: " + ParticipantEntity.class.getName());
        }
        Optional<?> competition = getArgumentObject(ARGS_COMPETITION_INDEX, args);
        if (!(competition.orElseThrow() instanceof CompetitionEntity)) {
            throw new IllegalArgumentException("Invalid argument type: " + competition.get().getClass().getName() +
                    ", expected: " + CompetitionEntity.class.getName());
        }
        Optional<?> heatLines = getArgumentObject(ARGS_HEAT_LINES_INDEX, args);
        if (!(heatLines.orElseThrow() instanceof List)) {
            throw new IllegalArgumentException("Invalid argument type: List of HeatLineEntity not found");
        }
        Optional<String> subscribed = getArgument(ARGS_SUBSCRIBED_INDEX, args);
        if (subscribed.isEmpty()) {
            throw new IllegalArgumentException("Invalid argument type: subscribed not found");
        }
    }
}
