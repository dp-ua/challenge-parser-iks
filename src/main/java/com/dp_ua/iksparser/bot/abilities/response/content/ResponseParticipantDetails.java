package com.dp_ua.iksparser.bot.abilities.response.content;

import com.dp_ua.iksparser.bot.Icon;
import com.dp_ua.iksparser.bot.abilities.action.ActionType;
import com.dp_ua.iksparser.bot.abilities.infoview.CompetitionView;
import com.dp_ua.iksparser.bot.abilities.infoview.ParticipantView;
import com.dp_ua.iksparser.bot.abilities.infoview.SubscriptionView;
import com.dp_ua.iksparser.bot.abilities.response.ResponseContentGenerator;
import com.dp_ua.iksparser.bot.abilities.response.ResponseTypeMarker;
import com.dp_ua.iksparser.bot.command.impl.CommandMenu;
import com.dp_ua.iksparser.bot.command.impl.participants.CommandParticipantDetails;
import com.dp_ua.iksparser.dba.entity.CompetitionEntity;
import com.dp_ua.iksparser.dba.entity.ParticipantEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import static com.dp_ua.iksparser.bot.abilities.response.ResponseType.PARTICIPANT_DETAILS;
import static com.dp_ua.iksparser.service.MessageCreator.END_LINE;
import static com.dp_ua.iksparser.service.MessageCreator.SERVICE;

@Component
@Scope("prototype")
@ResponseTypeMarker(PARTICIPANT_DETAILS)
@Slf4j
public class ResponseParticipantDetails implements ResponseContentGenerator {
    private static final int ARGS_SIZE = 3;
    private static final int ARGS_PARTICIPANT_INDEX = 0;
    private static final int ARGS_COMPETITIONS_INDEX = 1;
    private static final int ARGS_SUBSCRIBED_INDEX = 2;
    public static final String TEXT_TOOK_PART_IN_COMPETITIONS = "Приймав участь у змаганнях:";
    public static final String TEXT_DIDNT_TAKE_PART_IN_COMPETITIONS = "Не приймав участь у змаганнях";
    private final ParticipantView participantView;
    private final CompetitionView competitionView;
    private final SubscriptionView subscriptionView;

    public ResponseParticipantDetails(ParticipantView participantView, CompetitionView competitionView,
                                      SubscriptionView subscriptionView) {
        this.participantView = participantView;
        this.competitionView = competitionView;
        this.subscriptionView = subscriptionView;
    }

    @Override
    public String messageText(Object... args) {
        validateArgs(args);

        ParticipantEntity participant = getParticipant(args);
        boolean subscribed = getSubscribed(args);
        Page<CompetitionEntity> competitionsPage = getCompetitions(args);

        StringBuilder sb = new StringBuilder();
        sb
                .append(subscribedNoticeLine(subscribed))
                .append(END_LINE)
                .append(participantsInfo(participant))
                .append(END_LINE)
                .append(END_LINE)
                .append(competitionsInfo(competitionsPage))
                .append(END_LINE)
                .append(getPageInfoNavigation(competitionsPage));
        return sb.toString();
    }

    @Override
    public InlineKeyboardMarkup keyboard(Object... args) {
        validateArgs(args);

        ParticipantEntity participant = getParticipant(args);
        boolean subscribed = getSubscribed(args);
        Page<CompetitionEntity> competitionsPage = getCompetitions(args);
        int page = competitionsPage.getNumber();
        List<CompetitionEntity> competitions = competitionsPage.getContent();

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        markup.setKeyboard(rows);

        rows.add(getNavigationButtons(competitionsPage, participant, page));
        rows.add(getProfileButton(participant));
        rows.add(getSubscribeActionButton(subscribed, page, participant));
        rows.add(getCompetitionsNavButtons(competitions, participant));
        rows.add(getBackButton());

        return markup;
    }

    private List<InlineKeyboardButton> getProfileButton(ParticipantEntity participant) {
        List<InlineKeyboardButton> row = new ArrayList<>();

        participantView.getParticipantProfileButtonLink(participant)
                .ifPresent(row::add);

        return row;
    }

    private String competitionsInfo(Page<CompetitionEntity> competitionsPage) {
        StringBuilder sb = new StringBuilder();
        List<CompetitionEntity> competitions = competitionsPage.getContent();
        if (!competitions.isEmpty()) {
            sb
                    .append(TEXT_TOOK_PART_IN_COMPETITIONS)
                    .append(END_LINE)
                    .append(END_LINE)
                    .append(competitionView.listWithNumbers(competitions))
            ;
        } else {
            sb
                    .append(TEXT_DIDNT_TAKE_PART_IN_COMPETITIONS);
        }
        sb
                .append(END_LINE);
        return sb.toString();
    }


    private String participantsInfo(ParticipantEntity participant) {
        return participantView.info(participant);
    }

    private String subscribedNoticeLine(boolean subscribed) {
        return subscribed ? participantView.subscribedNoticeLine() : "";
    }

    private List<InlineKeyboardButton> getCompetitionsNavButtons(List<CompetitionEntity> competitions, ParticipantEntity participant) {
        if (competitions.isEmpty()) {
            return new ArrayList<>();
        }
        List<InlineKeyboardButton> row = new ArrayList<>();

        for (int i = 0; i < competitions.size(); i++) {
            CompetitionEntity competition = competitions.get(i);
            String buttonText = Icon.getIconicNumber(i + 1) + " " + competitionView.icon(competition).orElse(Icon.LOOK) + " " + competition.getName();
            row.add(getCompetitionDetailsButton(buttonText, competition, participant));
        }
        return row;
    }

    private InlineKeyboardButton getCompetitionDetailsButton(String buttonText, CompetitionEntity competition, ParticipantEntity participant) {
        return competitionView.participantInCompetitionButton(buttonText, competition, participant);
    }

    private List<InlineKeyboardButton> getBackButton() {
        return SERVICE.getBackButton(CommandMenu.getCallBackCommand());
    }

    private List<InlineKeyboardButton> getSubscribeActionButton(boolean subscribed, int page, ParticipantEntity participant) {
        List<InlineKeyboardButton> row2 = new ArrayList<>();

        InlineKeyboardButton subscribeAction = subscribed ?
                unsubscribeButton(page, participant) : subscribeButton(page, participant);
        row2.add(subscribeAction);
        return row2;
    }

    private List<InlineKeyboardButton> getNavigationButtons(Page<CompetitionEntity> competitionsPage, ParticipantEntity participant, int page) {
        List<InlineKeyboardButton> row1 = new ArrayList<>();

        if (competitionsPage.getTotalPages() > 1) {

            Long participantsId = participant.getId();
            if (competitionsPage.hasPrevious()) {
                row1.add(SERVICE.getNavPreviousPageButton(
                        CommandParticipantDetails.getCallbackCommand(page - 1, participantsId))
                );
            }
            if (competitionsPage.hasNext()) {
                row1.add(SERVICE.getNavNextPageButton(
                        CommandParticipantDetails.getCallbackCommand(page + 1, participantsId))
                );
            }
        }
        return row1;
    }

    private InlineKeyboardButton subscribeButton(int page, ParticipantEntity participant) {
        return subscriptionView.buttonSubscribe(CommandParticipantDetails.getCallbackCommand(page, participant.getId(), ActionType.SUB));
    }

    private InlineKeyboardButton unsubscribeButton(int page, ParticipantEntity participant) {
        return subscriptionView.buttonUnsubscribe(CommandParticipantDetails.getCallbackCommand(page, participant.getId(), ActionType.UNSUB));
    }

    private void validateArgs(Object[] args) {
        if (args.length != ARGS_SIZE) {
            throw new IllegalArgumentException("Invalid args size: " + args.length + ", expected: " + ARGS_SIZE);
        }

        validateArgument(
                getArgumentObject(ARGS_PARTICIPANT_INDEX, args),
                ParticipantEntity.class::isInstance,
                "Expected ParticipantEntity"
        );

        validateArgument(
                getArgumentObject(ARGS_COMPETITIONS_INDEX, args),
                value -> value instanceof Page<?>,
                "Expected Page<?>"
        );

        validateArgument(
                getArgument(ARGS_SUBSCRIBED_INDEX, args),
                value -> value.matches("true|false"),
                "Expected 'true' or 'false'"
        );
    }


    private <T> void validateArgument(Optional<T> argument, Predicate<T> predicate, String errorMessage) {
        if (argument.isEmpty()) {
            throw new IllegalArgumentException("Argument is missing: " + errorMessage);
        }
        T value = argument.get();
        if (!predicate.test(value)) {
            throw new IllegalArgumentException("Invalid argument: " + errorMessage + ", got: " + value.getClass().getName());
        }
    }

    private Page<CompetitionEntity> getCompetitions(Object[] args) {
        return (Page<CompetitionEntity>) getArgumentObject(ARGS_COMPETITIONS_INDEX, args).orElseThrow();
    }

    private boolean getSubscribed(Object[] args) {
        return Boolean.parseBoolean(getArgument(ARGS_SUBSCRIBED_INDEX, args).orElseThrow());
    }

    private ParticipantEntity getParticipant(Object[] args) {
        return (ParticipantEntity) getArgumentObject(ARGS_PARTICIPANT_INDEX, args).orElseThrow();
    }
}
